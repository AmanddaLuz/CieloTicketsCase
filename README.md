# CieloTickets

[![CI](https://github.com/AmanddaLuz/CieloTicketsCase/actions/workflows/ci.yml/badge.svg)](https://github.com/AmanddaLuz/CieloTicketsCase/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AmanddaLuz_CieloTicketsCase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AmanddaLuz_CieloTicketsCase)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A575%25-0061A4)

Aplicativo Android para venda de ingressos de eventos locais com pagamento pelo
ecossistema Cielo Smart, persistência do histórico e geração de QR Code para
compras aprovadas.

O projeto foi desenvolvido em Kotlin com telas nativas em XML e ViewBinding,
seguindo MVVM, Clean Architecture, SOLID, fluxo de dados unidirecional e
injeção manual de dependências.

**Repositório:** <https://github.com/AmanddaLuz/CieloTicketsCase>

## Funcionalidades

- catálogo local com eventos disponíveis;
- seleção de ingressos para um ou vários eventos;
- validação de quantidade máxima por evento;
- carrinho reutilizável em BottomSheet;
- pagamento por Deep Link com o emulador Cielo Smart;
- proteção contra cobranças duplicadas;
- persistência de tentativas e itens com Room;
- resultados aprovados, negados, cancelados e de erro;
- comprovante carregado a partir da compra persistida;
- QR Code exclusivo para compras aprovadas;
- histórico ordenado do mais recente para o mais antigo;
- filtro de histórico por status.

## Demonstração do fluxo

```text
Home
 ├── Eventos
 │    └── Carrinho
 │         └── Cielo Smart
 │              ├── Aprovado -> Comprovante + QR Code
 │              └── Negado/Cancelado/Erro -> resultado no BottomSheet
 └── Histórico
      └── Filtro por status
           └── Comprovante persistido
```

Uma versão visual e navegável desses fluxos está disponível em
[`docs/architecture/interactive-flows.md`](docs/architecture/interactive-flows.md),
com diagramas Mermaid de jornada, arquitetura, integração Cielo, estados e
histórico.

## Requisitos

- Android Studio com suporte ao Android Gradle Plugin 8.11.2;
- JDK 17;
- Android SDK 36;
- dispositivo ou emulador Android a partir da API 24;
- emulador Cielo Smart para testar pagamentos;
- Git.

O fluxo Cielo foi validado no Android 10/API 29.

## Configuração

1. Clone o repositório:

   ```bash
   git clone https://github.com/AmanddaLuz/CieloTicketsCase.git
   cd CieloTicketsCase
   ```

2. Crie o arquivo local de configuração:

   ```bash
   cp local.properties.example local.properties
   ```

3. Informe o caminho do Android SDK em `local.properties`:

   ```properties
   sdk.dir=/caminho/para/Android/sdk
   ```

4. Para o emulador Cielo, mantenha os valores públicos de teste presentes no
   arquivo de exemplo:

   ```properties
   CIELO_CLIENT_ID=emulator-test-client-id
   CIELO_ACCESS_TOKEN=emulator-test-access-token
   ```

Credenciais reais devem permanecer somente no `local.properties`, que não é
versionado.

## Emulador Cielo Smart

1. Baixe o emulador pela
   [documentação oficial](https://docs.cielo.com.br/cielo-smart/docs/baixando-o-emulador-cielo).
2. Instale o APK no dispositivo:

   ```bash
   adb install caminho/cielo-lio-emulator.apk
   ```

3. Abra o emulador Cielo ao menos uma vez.
4. Execute o CieloTickets e inicie uma compra.

O pacote consultado pela aplicação é `br.com.cielosmart.orderservice`.

## Execução

Pelo Android Studio, selecione a configuração `app` e execute em um dispositivo
com o emulador Cielo instalado.

Pelo terminal:

```bash
./gradlew installDebug
adb shell am start -n \
  br.com.amandaluz.cielotickets.xml/br.com.amandaluz.cielotickets.MainActivity
```

Builds de debug usam o sufixo `.xml`. Builds de release preservam o application
ID `br.com.amandaluz.cielotickets`.

## Integração com a Cielo

O pagamento é isolado pelo contrato `PaymentGateway`. A implementação
`CieloPaymentGatewayImpl`:

1. valida a configuração local;
2. converte a compra persistida para o payload Cielo;
3. abre `lio://payment`;
4. informa o callback exato `order://payment`.

O retorno abre `CieloResponseActivity`, que:

1. valida o scheme e o host;
2. decodifica a resposta Base64;
3. converte o resultado para um status de domínio;
4. envia um broadcast restrito ao pacote da aplicação.

O checkout correlaciona o resultado com a tentativa em processamento e persiste
a transição por compare-and-set. Callbacks duplicados não substituem um estado
terminal já registrado.

### Prevenção de duplicidade

A tentativa recebe um UUID e é salva antes da abertura da Cielo. O início do
pagamento exige a transição atômica:

```text
CREATED -> PROCESSING
```

Somente quem conclui essa transição pode abrir a cobrança. Um novo clique durante
`STARTING` ou `PROCESSING` não cria outra tentativa.

### Estados

```text
CREATED -> PROCESSING -> APPROVED
                      -> DENIED
                      -> CANCELLED
                      -> ERROR
```

## Arquitetura

O projeto utiliza um único módulo Android com limites explícitos entre camadas:

```text
presentation -> contrato de use case <- implementação
                                   -> contrato de repository <- implementação
                                   -> contrato de gateway <- adapter Cielo
```

| Camada | Responsabilidade |
|---|---|
| `domain/model` | Entidades e invariantes puras |
| `domain/usecase` | Contratos das operações de negócio |
| `domain/usecase/impl` | Implementações dos casos de uso |
| `domain/repository` | Contratos de persistência e catálogo |
| `domain/gateway` | Contratos de serviços externos |
| `data` | Catálogo local, Room, DAOs e repositories `Impl` |
| `payment/cielo` | Encoder, launcher, callback e gateway Cielo |
| `feature` | ViewModels, estados imutáveis, Fragments e adapters |
| `ui` | Componentes XML reutilizáveis e utilitários de lifecycle |
| `di` | Composition root e injeção manual |

As Views apenas exibem estado e encaminham ações. Totais, limites, persistência
e transições de pagamento não são calculados por Fragments, adapters ou custom
Views.

Mais detalhes em [`docs/architecture/overview.md`](docs/architecture/overview.md).
Consulte também os
[`diagramas interativos`](docs/architecture/interactive-flows.md).

## Persistência

Room armazena:

- `purchase_attempts`: referência, status e timestamps;
- `purchase_items`: snapshots dos eventos, quantidades, preços e posição.

A tentativa e seus itens são inseridos na mesma transação. A referência é única,
os itens possuem foreign key com cascade e atualizações de status usam SQL
compare-and-set.

O schema exportado está versionado em [`app/schemas`](app/schemas).

## QR Code

O QR Code é gerado apenas quando a compra está `APPROVED`. Seu conteúdo é uma
referência opaca:

```text
CIELO_TICKET|<purchase-reference>
```

Preços, nomes de eventos, credenciais e dados de pagamento não são incorporados.
Em produção, a referência deve ser validada por uma fonte confiável antes da
liberação do ingresso.

## Bibliotecas

| Biblioteca | Uso e justificativa |
|---|---|
| AndroidX AppCompat, Fragment e Core | Compatibilidade e infraestrutura de telas |
| Navigation Component | Grafo e back stack em uma única Activity |
| ViewBinding | Acesso seguro às Views XML sem buscas manuais |
| ConstraintLayout | Layouts responsivos |
| RecyclerView | Catálogo, carrinho, histórico e itens do recibo |
| Material Components | Botões, toolbar, chips e BottomSheet |
| Kotlin Coroutines e Flow | Estado reativo e operações assíncronas |
| Room | Persistência relacional, transações e consultas observáveis |
| ZXing Core | Geração local do QR Code sem tela externa |
| JUnit e Coroutines Test | Testes JVM determinísticos |
| AndroidX Test e Espresso | Testes de Room, navegação e fluxos XML |
| Detekt | Análise estática Kotlin |
| Android Lint | Validação do projeto Android |
| Kover | Cobertura mínima automatizada |

As versões ficam centralizadas em
[`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Qualidade e testes

Execute os gates locais:

```bash
./gradlew testDebugUnitTest
./gradlew detekt
./gradlew lintDebug
./gradlew koverVerifyDebug
./gradlew assembleDebug
```

Com um emulador ativo:

```bash
./gradlew connectedDebugAndroidTest
```

Ou execute a validação principal em uma chamada:

```bash
./gradlew testDebugUnitTest detekt lintDebug \
  koverVerifyDebug assembleDebug connectedDebugAndroidTest
```

Os testes críticos cobrem:

- limites, totais e overflow do carrinho;
- snapshots de compras com vários eventos;
- persistência transacional e referências duplicadas;
- transições de status atômicas;
- clique duplicado no pagamento;
- falhas de credencial, launcher e aplicativo Cielo;
- parsing e correlação de callbacks;
- filtros do histórico;
- recibos e QR Code somente para compras aprovadas;
- navegação e componentes XML.

Kover exige ao menos 75% de cobertura nas classes JVM elegíveis. Código de
framework Android é validado por testes instrumentados.

## Integração contínua

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) executa em
Pull Requests para `develop` e `main`:

- política de branches;
- Lint, Detekt e testes unitários;
- cobertura Kover;
- build do APK de debug;
- publicação dos relatórios como artefatos.

### SonarCloud

O workflow
[`sonarcloud.yml`](.github/workflows/sonarcloud.yml) envia análise de código,
Lint, Detekt, testes e cobertura Kover para:

<https://sonarcloud.io/project/overview?id=AmanddaLuz_CieloTicketsCase>

Para ativá-lo:

1. importe `AmanddaLuz/CieloTicketsCase` na organização `amnddaluz` do
   SonarCloud;
2. gere um token em **My Account > Security**;
3. no GitHub, abra **Settings > Secrets and variables > Actions**;
4. crie o secret `SONAR_TOKEN`.

O token nunca deve ser adicionado ao repositório ou ao `local.properties`.

## GitFlow

- `main`: releases;
- `develop`: integração;
- `feature/*`: funcionalidades;
- `bugfix/*`: correções;
- `release/*`: preparação de versão;
- `hotfix/*`: correção urgente de produção.

`main` e `develop` não recebem commits diretos. Mudanças entram por Pull Request,
com checks obrigatórios e conversas resolvidas.

Consulte [`CONTRIBUTING.md`](CONTRIBUTING.md).

### Versão e tag automática

A versão deve ser alterada em uma branch `release/*` ou `feature/*`, entrar por
PR em `develop` e depois por um PR de `develop` para `main`.

Após o merge na `main`, o workflow
[`release-tag.yml`](.github/workflows/release-tag.yml):

1. lê o arquivo `VERSION`;
2. valida o formato Semantic Versioning;
3. cria a tag anotada `v<versão>`;
4. publica a tag somente se ela ainda não existir.

Não é necessário criar commits ou tags diretamente nas branches protegidas.

## Decisões e trade-offs

- **Módulo único:** reduz complexidade para o tamanho do case, mantendo limites
  por packages. Em crescimento real, domínio, dados e apresentação poderiam ser
  separados em módulos Gradle.
- **Injeção manual:** torna dependências explícitas sem adicionar um framework
  para uma base pequena. Um projeto maior poderia adotar Hilt.
- **Catálogo local:** atende ao escopo sem backend. Produção exigiria API,
  sincronização, disponibilidade e política de preços.
- **Callback por custom scheme:** é compatível com o emulador, mas não comprova
  criptograficamente a origem. Produção exige reconciliação por backend ou API
  confiável da Cielo.
- **Broadcast em processo:** reduz componentes e preserva o fluxo validado. Se o
  processo não estiver ativo no retorno, a tentativa permanece pendente para
  reconciliação futura.
- **Filtro em memória:** adequado ao pequeno histórico local. Uma base extensa
  deveria filtrar e paginar no banco.
- **QR opaco:** minimiza exposição de dados, mas depende de validação confiável
  da referência.

## O que faria com mais tempo

- backend para catálogo, preços e reconciliação Cielo;
- callback HTTPS autenticado;
- recuperação automática de tentativas pendentes;
- paginação do histórico;
- criptografia adicional para dados locais;
- suporte completo a tema escuro e acessibilidade avançada;
- testes end-to-end em uma matriz de dispositivos;
- modularização Gradle;
- observabilidade sem dados sensíveis;
- validação e consumo único do ingresso por um aplicativo de entrada.

## Uso de IA e documentação

A implementação foi conduzida com suporte de agente de IA sob regras explícitas
de segurança, arquitetura, economia de contexto, GitFlow e validação.

- requisitos e conformidade:
  [`docs/case-requirements.md`](docs/case-requirements.md);
- specs:
  [`docs/specs`](docs/specs);
- decisões arquiteturais:
  [`docs/adr`](docs/adr);
- arquitetura:
  [`docs/architecture/overview.md`](docs/architecture/overview.md);
- diagramas e fluxos:
  [`docs/architecture/interactive-flows.md`](docs/architecture/interactive-flows.md);
- estratégia de testes:
  [`docs/testing/strategy.md`](docs/testing/strategy.md);
- prompts, restrições e resultados do agente:
  [`docs/agent-harness`](docs/agent-harness).

## Segurança

- segredos não são versionados;
- credenciais entram via `local.properties`;
- logs não incluem credenciais ou dados de pagamento;
- broadcasts são restritos ao package;
- callbacks desconhecidos não geram nova cobrança;
- estados terminais não são sobrescritos;
- QR Codes não contêm dados financeiros.

## Licença e finalidade

Projeto de case técnico para demonstração de arquitetura, integração Android,
qualidade e decisões de engenharia.
