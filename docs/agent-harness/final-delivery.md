# Registro do agente — entrega final

Data: 2026-08-28

## Prompts representativos

Os pedidos abaixo orientaram mudanças relevantes. Foram resumidos somente para
remover repetições de conversa, preservando a intenção técnica.

### Fundação

> Criar um novo projeto Android em XML, equivalente ao case existente, com
> documentação, linters, Detekt, GitFlow, branches protegidas, SOLID, inversão de
> dependências, classes Impl, Views passivas e componentes reutilizáveis.

**Resultado:** projeto independente com XML, ViewBinding, MVVM, contratos de
domínio, composition root, CI, SDD e ADRs.

### Entregas incrementais

> Fazer as alterações em uma branch separada da develop, sem commit nem push,
> fornecendo apenas a mensagem sugerida.

**Resultado:** fases curtas em `feature/*`, validação antes da entrega e controle
de commits mantido com a responsável pelo repositório.

### Integração Cielo

> Usar a integração por deep link já comprovada no emulador, com
> CieloResponseActivity.

**Resultado:** callback exato `order://payment`, Activity sem UI, parser Base64,
broadcast restrito ao pacote e persistência do resultado pelo checkout ativo.

### Histórico e recibo

> Adicionar filtro por status no início do histórico e exibir comprovante com QR
> Code para o pagamento aprovado.

**Resultado:** filtros reativos, navegação por referência persistida, recibo com
itens e QR Code opaco exclusivo para compras aprovadas.

### Entrega final

> Conferir os requisitos do arquivo do case, adicionar KDocs em português,
> produzir um README completo e remover referências a tecnologias fora da
> solução final.

**Resultado:** checklist de conformidade, documentação final autônoma, KDocs nos
pontos arquiteturais e README com execução, bibliotecas, Cielo, trade-offs,
qualidade e evoluções.

## Restrições aplicadas ao agente

- não versionar segredos ou dados de pagamento;
- não executar commit ou push;
- trabalhar sempre a partir de `develop` em branch curta;
- usar XML e ViewBinding;
- manter Views passivas;
- usar contratos e implementações explícitas para repositories e use cases;
- persistir a referência antes de abrir a cobrança;
- impedir transições concorrentes sobre estados terminais;
- não adicionar retry automático para callbacks desconhecidos;
- reutilizar o fluxo Cielo validado no emulador;
- executar somente ferramentas de qualidade já existentes;
- manter specs, ADRs, arquitetura e testes consistentes;
- economizar contexto registrando apenas decisões relevantes.

## Resultados que orientaram a implementação

| Observação | Decisão resultante |
|---|---|
| Uma tentativa pode conter vários eventos | Persistir snapshots em tabela de itens |
| Cliques rápidos podem publicar carrinhos divergentes | Serializar mutações com `Mutex` |
| Dois inícios podem abrir duas cobranças | Exigir `CREATED -> PROCESSING` atômico |
| A Cielo retorna erros sem referência | Aplicar somente à tentativa ativa |
| Query parameters extras alteraram o callback conhecido | Preservar `order://payment` |
| O callback pode chegar com a tela em segundo plano | Manter observer no ViewModel da feature |
| Eventos podem mudar após a compra | Recibo usa snapshots persistidos |
| QR com dados do pedido expõe informação desnecessária | Codificar somente referência opaca |
| Histórico pequeno não exige paginação imediata | Filtrar em memória sobre Flow persistido |
| Código Android não é adequado à cobertura JVM | Validar wiring e Views em testes instrumentados |

## Validação usada

```bash
./gradlew testDebugUnitTest
./gradlew detekt
./gradlew lintDebug
./gradlew koverVerifyDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
git diff --check
```
