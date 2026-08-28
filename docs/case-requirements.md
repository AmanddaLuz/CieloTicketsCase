# Atendimento aos requisitos do case Android

Fonte auditada: `Case_Android.pdf`, disponibilizado com o desafio técnico.

## Resultado

| Requisito | Status | Evidência principal |
|---|---|---|
| Listar eventos disponíveis | Atendido | `LocalEventRepositoryImpl`, `EventsFragment` e `EventAdapter` |
| Selecionar quantidade de ingressos | Atendido | `QuantitySelectorView`, `EventsViewModel` e `BuildCartUseCaseImpl` |
| Iniciar pagamento pela Cielo | Atendido | `StartPaymentUseCaseImpl` e `CieloPaymentGatewayImpl` |
| Concluir pagamento pela Cielo | Atendido | `CieloResponseActivity`, parser de callback e `CheckoutViewModel` |
| Registrar compra aprovada | Atendido | estado `APPROVED`, Room e comprovante |
| Registrar compra negada | Atendido | estado `DENIED`, Room e histórico |
| Registrar compra cancelada | Atendido | estado `CANCELLED`, Room e histórico |
| Exibir comprovante da compra | Atendido | `ReceiptFragment` carregado por referência persistida |
| QR Code vinculado à compra concluída | Atendido | QR exclusivo para `APPROVED` com referência da compra |
| Tratamento explícito de erros | Atendido | resultados tipados do gateway, use cases e checkout |
| Evitar cobrança duplicada | Atendido | persist-before-pay, mutex e compare-and-set |
| Código organizado e manutenível | Atendido | MVVM, Clean Architecture, SOLID e contratos com classes `Impl` |
| Testes automatizados críticos | Atendido | testes JVM e instrumentados em `app/src/test` e `app/src/androidTest` |
| Uso documentado de IA | Atendido | `docs/agent-harness/` |
| Kotlin Android | Atendido | Kotlin 2.0.21 e Android Gradle Plugin 8.11.2 |
| Instruções claras de execução | Atendido | `README.md` |
| Decisões arquiteturais | Atendido | `docs/adr/` e `docs/architecture/overview.md` |
| Diagramas de arquitetura e fluxo | Atendido | `docs/architecture/interactive-flows.md` |
| Bibliotecas e justificativas | Atendido | seção de bibliotecas do `README.md` |
| Integração Cielo documentada | Atendido | README, `docs/specs/payment-spec.md` e `docs/cielo/` |
| Trade-offs documentados | Atendido | README e ADRs |
| Próximas evoluções documentadas | Atendido | seção “O que faria com mais tempo” do README |
| Harness com specs | Atendido | `docs/specs/` |
| Harness com prompts e restrições | Atendido | `docs/agent-harness/final-delivery.md` |
| Harness com resultados | Atendido | registros por fase em `docs/agent-harness/` |
| Repositório público | Atendido | `https://github.com/AmanddaLuz/CieloTicketsCase` |

## Requisitos adicionais implementados

- carrinho com vários eventos;
- histórico persistido com filtro por status;
- estados de erro técnico;
- snapshots dos itens para preservar o recibo;
- prevenção de overflow monetário;
- schema Room versionado;
- cobertura mínima automatizada de 75%;
- Android Lint, Detekt e CI;
- GitFlow com branches principais protegidas;
- QR Code opaco sem dados financeiros.

## Limites conhecidos

- o catálogo é local e determinístico;
- o custom URI scheme atende ao emulador, mas não autentica a origem;
- o broadcast depende de um checkout ativo;
- não há backend para reconciliação financeira;
- tentativas sem callback permanecem pendentes e não são reenviadas
  automaticamente.

Esses limites não impedem os fluxos obrigatórios do case e estão documentados
como trade-offs para uma evolução de produção.
