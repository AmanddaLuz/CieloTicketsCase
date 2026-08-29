# Desafios técnicos e soluções de engenharia

Este documento registra desafios reais encontrados durante o desenvolvimento,
suas causas, as decisões adotadas e as medidas que evitam regressões. O objetivo
é preservar o raciocínio de engenharia, e não apenas o estado final do código.

## 1. ViewModels com dependências não podiam ser criados pelo factory padrão

**Sintoma:** falha ao abrir uma tela porque o Android não conseguia instanciar
um `ViewModel` com dependências no construtor.

**Causa:** o factory padrão conhece apenas construtores compatíveis com a
criação automática do framework. Os ViewModels do projeto dependem de use
cases, mappers e observers.

**Solução:** centralizar a composição em `AppContainerImpl` e fornecer
`ViewModelProvider.Factory` explícito para cada ViewModel.

**Prevenção:** manter a criação das dependências fora de Fragments e
ViewModels. Testes usam implementações controladas dos contratos sem depender
do container da aplicação.

## 2. Conflito com outra instalação no emulador

**Sintoma:** a instalação de debug conflitava com um aplicativo que utilizava o
mesmo application ID.

**Causa:** duas variantes de desenvolvimento tentavam ocupar o mesmo pacote no
dispositivo.

**Solução:** utilizar
`br.com.amandaluz.cielotickets.xml` como application ID de debug e preservar
`br.com.amandaluz.cielotickets` para release.

**Prevenção:** manter IDs distintos por variante sempre que versões de
desenvolvimento precisarem coexistir no mesmo dispositivo.

## 3. Aplicativo Cielo não encontrado

**Sintoma:** o checkout retornava `AppNotAvailable`.

**Causa:** package e URI encontrados em materiais antigos não correspondiam ao
emulador Cielo Smart utilizado.

**Solução:** usar:

```text
Package: br.com.cielosmart.orderservice
Pagamento: lio://payment
Callback: order://payment
```

O URI de pagamento inclui `urlCallback=order://payment`, e o Manifest declara o
package consultado e a Activity que recebe o callback.

**Diagnóstico útil:**

```bash
adb shell pm list packages | grep -i "cielo\|lio"
adb shell pm dump br.com.cielosmart.orderservice
```

## 4. Payload recusado por ausência de `sku`

**Sintoma:** a Cielo era aberta, mas devolvia erro ao criar a ordem.

**Causa:** cada item do payload exige um `sku` não vazio.

**Solução:** `CieloPaymentRequestEncoderImpl` envia o `eventId` como `sku`.
Esse valor é estável, identifica o item e não exige um novo identificador apenas
para a integração.

**Prevenção:** o teste do encoder decodifica o Base64 e verifica os campos
obrigatórios do payload.

## 5. Callback de erro não possuía o mesmo formato do sucesso

**Sintoma:** o pagamento permanecia aguardando ou o callback não podia ser
interpretado.

**Causa:** respostas de erro podem chegar sem `responsecode` e sem referência.
O resultado útil está no JSON Base64 do parâmetro `response`.

**Solução:** separar o processamento em duas etapas:

1. `CieloCallbackUriParser` valida o URI e decodifica o parâmetro `response`;
2. `CieloCallbackResponseParser` interpreta o JSON e produz um resultado
   tipado.

Callbacks sem referência são aceitos somente para a tentativa que está ativa e
em processamento. Um callback com referência diferente é ignorado.

## 6. Cancelamento podia ser interpretado como aprovação

**Sintoma:** um resultado cancelado podia ser apresentado como pagamento
aprovado.

**Causa:** `responsecode=0` indica que a requisição foi concluída, não que a
venda foi aprovada. O resultado real depende do conteúdo do JSON.

**Solução:** o parser utiliza a seguinte regra:

| `code` no JSON | Status |
|---|---|
| ausente, com referência válida | `APPROVED` |
| `1` | `CANCELLED` |
| `2` ou `3` | `DENIED` |
| `4` ou desconhecido | `ERROR` |

### Exemplos de callbacks recebidos e mapeados

Os exemplos abaixo usam respostas reduzidas aos campos relevantes para o
parser. Em uma aprovação real, o JSON pode conter outros dados da ordem.

O caminho comum para todos os casos é:

```text
order://payment?response=<Base64>
  -> CieloResponseActivity
  -> CieloCallbackUriParser valida scheme/host e decodifica o Base64
  -> CieloCallbackResponseParser interpreta o JSON
  -> CieloCallbackResult(reference, status, errorMessage)
  -> PaymentResult
  -> CheckoutViewModel
```

#### Pagamento aprovado

Callback recebido:

```text
order://payment?response=eyJyZWZlcmVuY2UiOiJwdXJjaGFzZS0xMjMiLCJzdGF0dXMiOiJQQUlEIn0=&responsecode=0
```

Conteúdo do parâmetro `response` após decodificar o Base64:

```json
{"reference":"purchase-123","status":"PAID"}
```

Como o JSON não contém `code` e possui uma referência válida, o resultado é:

```kotlin
CieloCallbackResult(
    reference = "purchase-123",
    status = PaymentStatus.APPROVED,
    errorMessage = null,
)
```

O campo `status` do exemplo faz parte da resposta da ordem, mas a aprovação é
determinada pela ausência de `code` e pela presença da referência.

#### Pagamento cancelado

Callback recebido:

```text
order://payment?response=eyJjb2RlIjoxLCJyZWFzb24iOiJDQU5DRUxBRE8gUEVMTyBVU1VBUklPIn0=&responsecode=0
```

JSON decodificado:

```json
{"code":1,"reason":"CANCELADO PELO USUARIO"}
```

Resultado mapeado:

```kotlin
CieloCallbackResult(
    reference = "",
    status = PaymentStatus.CANCELLED,
    errorMessage = "CANCELADO PELO USUARIO",
)
```

Mesmo com `responsecode=0`, o `code=1` no corpo representa cancelamento.

#### Pagamento negado com `code=2`

Esse código pode ser devolvido quando a ordem não pode ser criada.

Callback recebido:

```text
order://payment?response=eyJjb2RlIjoyLCJyZWFzb24iOiJFUlJPIEFPIENSSUFSIEEgT1JERU0ifQ==
```

JSON decodificado:

```json
{"code":2,"reason":"ERRO AO CRIAR A ORDEM"}
```

Resultado mapeado:

```kotlin
CieloCallbackResult(
    reference = "",
    status = PaymentStatus.DENIED,
    errorMessage = "ERRO AO CRIAR A ORDEM",
)
```

#### Pagamento negado com `code=3`

Callback recebido:

```text
order://payment?response=eyJjb2RlIjozLCJyZWFzb24iOiJQQUdBTUVOVE8gTkVHQURPIn0=
```

JSON decodificado:

```json
{"code":3,"reason":"PAGAMENTO NEGADO"}
```

Resultado mapeado:

```kotlin
CieloCallbackResult(
    reference = "",
    status = PaymentStatus.DENIED,
    errorMessage = "PAGAMENTO NEGADO",
)
```

#### Erro de autenticação

Callback recebido:

```text
order://payment?response=eyJjb2RlIjo0LCJyZWFzb24iOiJFUlJPIERFIEFVVEVOVElDQUNBTyJ9
```

JSON decodificado:

```json
{"code":4,"reason":"ERRO DE AUTENTICACAO"}
```

Resultado mapeado:

```kotlin
CieloCallbackResult(
    reference = "",
    status = PaymentStatus.ERROR,
    errorMessage = "ERRO DE AUTENTICACAO",
)
```

#### Código desconhecido

Callback recebido:

```text
order://payment?response=eyJjb2RlIjo5OSwicmVhc29uIjoiQ09ESUdPIERFU0NPTkhFQ0lETyJ9
```

JSON decodificado:

```json
{"code":99,"reason":"CODIGO DESCONHECIDO"}
```

Resultado mapeado de forma segura:

```kotlin
CieloCallbackResult(
    reference = "",
    status = PaymentStatus.ERROR,
    errorMessage = "CODIGO DESCONHECIDO",
)
```

Um Base64 inválido, JSON malformado, scheme diferente de `order`, host
diferente de `payment` ou aprovação sem referência produz `null`. Esse retorno
é rejeitado e não altera a compra nem inicia uma nova cobrança.

**Prevenção:** manter testes separados para aprovação, negação, cancelamento,
erro de autenticação, JSON inválido e ausência de referência.

## 7. Credenciais não configuradas no ambiente local

**Sintoma:** todas as compras retornavam
`CredentialsNotConfigured`.

**Causa:** `CIELO_CLIENT_ID` e `CIELO_ACCESS_TOKEN` estavam ausentes ou vazios
no `local.properties`.

**Solução para o emulador:**

```properties
CIELO_CLIENT_ID=emulator-test-client-id
CIELO_ACCESS_TOKEN=emulator-test-access-token
```

O arquivo `local.properties.example` documenta a configuração. O
`local.properties` real permanece fora do Git.

**Produção:** um terminal real exige credenciais válidas fornecidas pela Cielo.
Nenhuma credencial deve ser incluída no repositório, em logs ou em QR Codes.

## 8. Persistência iniciada tarde demais permitiria perder a tentativa

**Risco:** abrir o aplicativo de pagamento antes de registrar a compra deixaria
o sistema sem referência confiável para correlacionar o retorno.

**Solução:** o checkout segue obrigatoriamente esta ordem:

```text
Criar tentativa em CREATED
  -> persistir tentativa e itens em transação
  -> compare-and-set CREATED para PROCESSING
  -> abrir a Cielo
```

A referência persistida é enviada no payload e utilizada para recuperar a
compra no callback, no histórico e no comprovante.

## 9. Cliques repetidos podiam iniciar mais de um fluxo

**Risco:** dois cliques rápidos poderiam tentar abrir duas cobranças para o
mesmo carrinho.

**Solução:** `CheckoutViewModel` serializa início e callback com `Mutex`, ignora
novas solicitações em `STARTING` ou `PROCESSING`, e
`StartPaymentUseCaseImpl` reivindica a tentativa com compare-and-set:

```text
CREATED -> PROCESSING
```

Somente a execução que atualiza a linha pode abrir a Cielo.

## 10. Atualizações concorrentes podiam sobrescrever um resultado terminal

**Risco:** callbacks repetidos ou concorrentes poderiam substituir uma
aprovação por negação, cancelamento ou erro.

**Solução:** o DAO atualiza o status somente quando o valor atual corresponde
ao esperado:

```sql
UPDATE purchase_attempts
SET status = :newStatus, updatedAt = :updatedAt
WHERE reference = :reference AND status = :expectedStatus
```

Se nenhuma linha for alterada, o DAO diferencia referência inexistente de
divergência de status. Resultados repetidos são idempotentes e estados
terminais permanecem imutáveis.

## 11. Processamento durável do callback aumentava a complexidade sem garantir a jornada

**Problema:** delegar o retorno do pagamento a trabalho em segundo plano criava
mais estados intermediários e dificultava entregar o resultado imediatamente à
tela ativa.

**Solução:** `CieloResponseActivity` recebe `order://payment`, faz o parsing e
emite um broadcast restrito ao package. `CieloPaymentResultObserverImpl`
entrega o resultado ao `CheckoutViewModel`.

**Limitação conhecida:** se o processo não estiver ativo, a tentativa permanece
em `PROCESSING`. Em produção, a solução adequada é reconciliação confiável com
backend ou API da adquirente, e não repetição automática da cobrança.

## 12. Resultado aprovado permanecia no BottomSheet

**Sintoma:** após uma aprovação, o BottomSheet mostrava apenas uma mensagem de
sucesso e o comprovante com QR Code não era aberto.

**Causa:** o resultado terminal era tratado apenas como estado visual local e o
evento de navegação podia ser consumido no momento inadequado do ciclo de vida.

**Solução:** `CheckoutUiState` mantém
`receiptNavigationPending=true` até `EventsFragment` efetuar a navegação e
chamar `consumeReceiptNavigation()`. Negação, cancelamento e erro permanecem no
BottomSheet; somente aprovação abre o comprovante.

## 13. Limpeza do carrinho podia ser perdida durante a navegação

**Sintoma:** uma venda aprovada podia abrir o comprovante e manter itens no
carrinho ao retornar.

**Causa:** a limpeza dependia de um consumidor que poderia estar parado ou já
ter sido removido quando o evento fosse emitido.

**Solução:** `EventsFragment`, que possui o contexto da jornada, limpa o
carrinho no mesmo fluxo em que navega para o comprovante. O estado aprovado
permanece pendente até essa ação ser consumida.

## 14. Comprovante aprovado sem QR Code ou com dados inconsistentes

**Sintoma:** uma compra aprovada podia exibir apenas o estado de sucesso do
checkout ou montar o comprovante com um objeto transitório.

**Causa:** usar somente o estado em memória acoplaria o recibo ao ciclo de vida
do checkout.

**Solução:** a navegação envia somente a referência. `ReceiptViewModel` chama
`GetPurchaseAttemptUseCase`, que recarrega do Room a tentativa e seus itens. O
QR Code é gerado somente quando o status persistido é `APPROVED`, com o payload:

```text
CIELO_TICKET|<purchase-reference>
```

## 15. Botão de checkout podia ficar parcialmente fora da área visível

**Sintoma:** em determinadas dimensões de tela, o conteúdo do carrinho ocupava
o espaço reservado ao botão de pagamento.

**Causa:** a estrutura não delimitava corretamente a área rolável e a ação
fixa.

**Solução:** separar a lista rolável da área inferior de ação e respeitar
insets e dimensões disponíveis no BottomSheet.

## 16. Barra e botões de navegação do sistema sem contraste

**Sintoma:** a barra de navegação e seus ícones apareciam brancos, reduzindo a
legibilidade.

**Causa:** a cor clara da barra não estava acompanhada da configuração de
ícones escuros nas versões Android compatíveis.

**Solução:** definir a cor da navigation bar e habilitar
`windowLightNavigationBar` em `values-v27`, preservando contraste de acordo com
a versão do sistema.

## 17. Adapter mantinha referência à View além do ciclo de vida

**Risco:** manter o adapter vinculado após `onDestroyView` poderia preservar
referências da hierarquia anterior e gerar comportamento incorreto ao recriar a
tela.

**Solução:** adapters pertencentes à View do Fragment são desvinculados em
`onDestroyView`, assim como o delegate de ViewBinding invalida o binding nesse
momento.

## 18. Itens diferentes do comprovante podiam compartilhar a mesma identidade visual

**Risco:** o `DiffUtil` do `ReceiptItemAdapter` utilizava `eventName` em
`areItemsTheSame`. Dois eventos distintos com o mesmo nome seriam considerados
o mesmo item pelo Adapter, mesmo possuindo identificadores diferentes no
domínio e no banco.

**Causa:** `ReceiptItemUiModel` transportava somente os dados exibidos e
descartava o `eventId` durante o mapeamento para a apresentação. O nome é um
atributo visual mutável e não constitui uma identidade técnica confiável.

**Solução:** propagar o identificador sem exibi-lo:

```text
PurchaseItem.eventId
  -> ReceiptUiMapper
  -> ReceiptItemUiModel.eventId
  -> ReceiptItemAdapter.Diff
```

O `DiffUtil` agora compara:

```kotlin
oldItem.eventId == newItem.eventId
```

Essa regra fica alinhada ao modelo persistido, no qual
`purchase_items` possui um índice único formado por `attemptReference` e
`eventId`. `areContentsTheSame` continua comparando a `data class` completa
para detectar mudanças de nome, quantidade, preço ou subtotal.

**Prevenção:** identidades de adapters devem usar chaves estáveis do domínio,
nunca textos exibidos, posições ou valores formatados. O teste do comprovante
também verifica que o mapper preserva o `eventId`.

## Como usar este documento

Ao investigar uma regressão:

1. reproduza o sintoma sem registrar credenciais ou dados de pagamento;
2. identifique a camada responsável antes de alterar a interface;
3. adicione ou ajuste um teste que reproduza a causa;
4. aplique a correção na camada de menor responsabilidade possível;
5. atualize esta página quando surgir um novo aprendizado relevante.

As regras permanentes da integração estão resumidas em
[`../cielo/integration-constraints.md`](../cielo/integration-constraints.md).
