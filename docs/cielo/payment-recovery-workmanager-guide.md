# Recuperação de pagamentos e WorkManager

Este documento explica as classes adicionadas na branch
`fix/durable-cielo-callback`, o fluxo completo do checkout e o papel dos dois
trabalhos duráveis executados pelo WorkManager.

## Visão geral

```text
Checkout
   |
   +-- salva tentativa no Room
   +-- muda CREATED -> PROCESSING
   +-- agenda timeout no WorkManager
   +-- abre o app Cielo
              |
              v
        Cielo realiza pagamento
              |
              v
        order://payment
              |
              v
    CieloResponseActivity
       +-- interpreta callback
       +-- resolve referência
       +-- agenda worker durável
       +-- avisa a UI ativa
       +-- abre a tela de resultado
              |
              v
    Worker atualiza o Room
              |
              v
    UI observa o Room
```

A ideia central é que a interface pode desaparecer, mas a operação financeira
e seu estado persistido não podem depender dela.

## Classes de correlação da compra ativa

### `CieloActivePaymentStore`

É o contrato para guardar qual compra está atualmente aberta no aplicativo da
Cielo:

```kotlin
interface CieloActivePaymentStore {
    fun claim(reference: String): Boolean
    fun currentReference(): String?
    fun replace(expectedReference: String, newReference: String): Boolean
    fun clear(reference: String)
}
```

Ele existe principalmente porque alguns retornos de erro da Cielo podem chegar
sem a referência da compra.

### `CieloActivePaymentStoreImpl`

Implementa o contrato usando `SharedPreferences` privadas:

```text
cielo_active_payment
+-- reference = "pedido-123"
```

Operações:

- `claim`: registra uma referência se não houver outra diferente.
- `currentReference`: consulta a referência ativa.
- `replace`: substitui somente se a referência atual ainda for a esperada.
- `clear`: remove somente se a referência recebida for a ativa.

O `replace` funciona como compare-and-set:

```text
referência armazenada == referência esperada?
    sim -> substitui
    não -> não altera
```

Isso evita que operações concorrentes substituam a compra errada.

Foi usado `commit()` porque, antes de abrir a Cielo, é necessário garantir que
a referência foi gravada. Com `apply()`, a escrita seria assíncrona e o processo
poderia morrer antes da persistência.

### `CieloCallbackReferenceResolver`

Se a Cielo forneceu uma referência, ela é mantida. Caso contrário, o resolver
usa a referência guardada no `CieloActivePaymentStore`.

```text
Callback possui referência?
   +-- Sim -> utiliza a referência recebida
   +-- Não -> consulta SharedPreferences
```

### `ActivePaymentCoordinator`

É o contrato usado pelo gateway para ativar uma nova sessão externa sem
conhecer `SharedPreferences`, Room ou as regras de rollover.

### `CieloActivePaymentCoordinatorImpl`

Coordena o início de uma nova compra. Quando já existe uma referência ativa
diferente:

1. Tenta mudar a compra anterior para `TIMED_OUT`.
2. Verifica se ela pode ser substituída com segurança.
3. Troca atomicamente a referência anterior pela nova.

```text
Compra A está PROCESSING
        |
Nova compra B será aberta
        |
        v
A: PROCESSING -> TIMED_OUT
        |
        v
Referência ativa: A -> B
        |
        v
Cielo é aberta para B
```

Se a tentativa anterior estiver em um estado inseguro que não possa ser
encerrado, a nova ativação é recusada.

## Classes do callback durável

### `CieloResponseActivity`

É a Activity registrada para receber `order://payment`. Ela não apresenta
interface visual.

Responsabilidades:

1. Interpretar a URI.
2. Resolver a referência ausente.
3. Agendar o callback no WorkManager.
4. Enviar um broadcast para uma UI ativa.
5. Abrir a `MainActivity` com a referência.
6. Encerrar.

A ordem é importante:

```text
Receber callback
    |
Validar
    |
WorkManager aceita o trabalho
    |
Avisar UI e abrir resultado
    |
Finalizar Activity
```

A Activity aguarda `enqueue(...).await()`. Isso confirma que o WorkManager
persistiu o trabalho antes de a Activity terminar.

### `CieloPaymentCallbackScheduler`

Transforma o callback em uma solicitação do WorkManager:

```kotlin
OneTimeWorkRequestBuilder<CieloPaymentCallbackWorker>()
```

Apenas `reference` e `status` são enviados no `Data`. O payload completo não é
usado porque o `Data` do WorkManager possui limite de tamanho.

O trabalho possui um nome por referência:

```text
cielo-callback-pedido-123
```

Ele usa `ExistingWorkPolicy.KEEP`. Se o mesmo callback for agendado novamente
enquanto já existir um trabalho com aquele nome, o trabalho existente é
mantido.

### `CieloPaymentCallbackWorker`

É quem efetivamente persiste o resultado financeiro no Room:

1. Lê `reference` e `status` do `inputData`.
2. Rejeita dados ausentes ou status que não sejam terminais.
3. Obtém as dependências pelo `CieloTicketsApplication`.
4. Chama `UpdatePurchaseStatusUseCase`.
5. Limpa a referência ativa correspondente.
6. Finaliza com sucesso ou solicita nova tentativa.

Resultados possíveis:

```kotlin
Result.success()
Result.retry()
Result.failure()
```

- `success`: processamento concluído.
- `retry`: ocorreu uma falha SQLite transitória.
- `failure`: os dados do trabalho são inválidos ou a aplicação esperada não
  está disponível.

## Classes do timeout

### `PaymentProcessingTimeoutScheduler`

É o contrato para agendar o vencimento de uma compra:

```kotlin
suspend fun schedule(reference: String)
```

Isso mantém o caso de uso independente da API Android.

### `PaymentProcessingTimeoutSchedulerImpl`

Cria um worker com atraso de um minuto:

```kotlin
.setInitialDelay(1L, TimeUnit.MINUTES)
```

Cada compra recebe um nome único:

```text
payment-processing-timeout-pedido-123
```

Também utiliza `ExistingWorkPolicy.KEEP`, impedindo múltiplos cronômetros para
a mesma referência.

### `PaymentProcessingTimeoutWorker`

Depois do atraso, tenta executar:

```text
PROCESSING -> TIMED_OUT
```

A atualização passa pelo mesmo `UpdatePurchaseStatusUseCase` usado pelos
callbacks. Se um callback já tiver aprovado a compra, a transição
`APPROVED -> TIMED_OUT` será recusada e o timeout não sobrescreverá a
aprovação.

Se a atualização para `TIMED_OUT` for aceita, o worker envia um broadcast para
que o BottomSheet ativo saia do loading.

O timeout não limpa a referência ativa, pois um callback tardio sem referência
ainda pode chegar.

## Como o WorkManager funciona neste projeto

O WorkManager é usado para trabalhos que precisam sobreviver ao ciclo de vida
da interface e à morte do processo.

Ao executar:

```kotlin
workManager.enqueueUniqueWork(...)
```

o AndroidX persiste a descrição do trabalho em seu banco interno. O trabalho
não depende de:

- Activity aberta;
- Fragment aberto;
- BottomSheet visível;
- ViewModel vivo;
- processo atual continuar existindo.

O sistema pode recriar o processo posteriormente e instanciar o worker.

### Worker de callback

Não possui atraso:

```text
Callback recebido
    |
WorkRequest persistido
    |
Worker executa assim que possível
    |
Room atualizado
```

### Worker de timeout

Possui atraso inicial:

```text
Pagamento entrou em PROCESSING
    |
WorkRequest agendado para não antes de 1 minuto
    |
Um callback chegou antes?
    +-- Sim -> compra já está terminal; timeout não altera
    +-- Não -> PROCESSING passa para TIMED_OUT
```

Um minuto significa que o worker não deve executar antes desse prazo. O Android
pode executá-lo depois, dependendo de bateria, carga, políticas do sistema ou
disponibilidade de recursos.

## Caminho da atualização visual

O Room é a fonte da verdade. O broadcast melhora a resposta da tela que já está
aberta.

### UI ainda está aberta

```text
Worker atualiza Room
    |
Worker envia broadcast
    |
CieloPaymentResultObserverImpl recebe
    |
CheckoutViewModel atualiza CheckoutUiState
    |
BottomSheet substitui loading pelo resultado
```

### Aplicativo estava fechado

```text
Callback abre CieloResponseActivity
    |
WorkManager recebe o trabalho
    |
MainActivity é criada ou trazida para frente
    |
Navegação abre a referência
    |
ReceiptViewModel observa o Room
    |
Worker persiste o resultado
    |
Tela atualiza automaticamente
```

### `CieloPaymentResultObserverImpl`

É o receiver usado pela apresentação ativa. Ele aceita:

- `APPROVED`;
- `DENIED`;
- `CANCELLED`;
- `ERROR`;
- `TIMED_OUT`.

O broadcast é uma otimização para resposta imediata, não a garantia de
persistência. A garantia está no WorkManager e no Room.

### `ObservePurchaseAttemptUseCase`

Expõe uma tentativa como `Flow<PurchaseAttempt?>`. O recibo permanece
observando:

```text
PROCESSING -> TIMED_OUT -> APPROVED
```

A mesma tela consegue refletir cada atualização sem precisar ser recriada.

### `MainActivity`

Recebe a referência enviada pela `CieloResponseActivity`, espera estar em
estado `RESUMED` e abre o recibo correspondente. Também evita abrir duas telas
para a mesma referência.

## Fluxo completo ao iniciar uma compra

```text
CheckoutViewModel
    |
Cria PurchaseAttempt
    |
Salva como CREATED no Room
    |
StartPaymentUseCaseImpl
    |
CREATED -> PROCESSING
    |
Agenda timeout de 1 minuto
    |
CieloPaymentGatewayImpl valida credenciais
    |
Codifica payload da Cielo
    |
CieloActivePaymentCoordinatorImpl ativa a referência
    |
CieloPaymentIntentLauncher abre o app Cielo
```

Se o launcher falhar, a nova referência ativa é limpa e a tentativa passa para
`ERROR`.

## Fluxo completo ao receber o resultado

```text
Cielo chama order://payment
    |
CieloResponseActivity
    |
CieloCallbackUriParser
    |
CieloCallbackResponseParser
    |
CieloCallbackReferenceResolver
    |
CieloPaymentCallbackScheduler
    |
CieloPaymentCallbackWorker
    |
UpdatePurchaseStatusUseCase
    |
Room
```

Paralelamente, a Activity envia o broadcast e abre a tela do resultado.

## Por que existem mecanismos diferentes

| Mecanismo | Responsabilidade |
| --- | --- |
| WorkManager | Garantir processamento durável |
| Room | Ser a fonte da verdade |
| Broadcast | Atualizar imediatamente uma UI ativa |
| Intent para `MainActivity` | Trazer o resultado para a frente |
| `Flow` do Room | Manter recibo e histórico sincronizados |
| SharedPreferences | Correlacionar callbacks sem referência |

Esses mecanismos são complementares. O WorkManager garante execução; o Room
guarda o estado; o broadcast melhora a atualização imediata; e a navegação
apresenta ao operador a transação persistida.
