# Registro do agente — seleção de modalidade de pagamento

Data: 2026-08-29

## Prompt representativo

> No BottomSheet, adicionamos mais um botão: o primeiro é crédito à
> vista e o segundo é débito à vista. A chamada que eles fazem é a mesma, só
> o `put("paymentCode", PAYMENT_CODE)` que passa correspondente ao botão
> escolhido. Ajustar no banco mais um campo `paymentMethod: PaymentMethod`

## Restrições aplicadas

- Nenhum commit e nenhum PR foram criados; a alteração aguarda review manual
  na branch em uso.
- `PaymentMethod` foi modelado no domínio, não na View, para não duplicar
  regra de negócio em Fragments.
- `PurchaseAttempt` passou a carregar o método escolhido durante todo o ciclo
  de vida da tentativa, preservando a idempotência já existente (ADR 0008).
- O encoder Cielo deixou de fixar `paymentCode`, delegando ao domínio via
  `PaymentMethod.cieloCode`.
- A coluna nova em `purchase_attempts` exigiu uma migration Room (`1 → 2`) com
  backfill para `CREDIT_CASH`, validada por
  `AppDatabaseMigrationTest` (androidTest).

## Resultado

- Novo `PaymentMethod` (`CREDIT_CASH`, `DEBIT_CASH`) no domínio.
- `CreatePurchaseAttemptUseCase`, `CheckoutViewModel.start` e o encoder Cielo
  passaram a receber/propagar o método escolhido.
- `bottom_sheet_cart.xml` e `CartBottomSheetFragment` substituíram o botão
  único por dois botões dedicados, mantendo Views passivas.
- Testes unitários e instrumentados atualizados; suíte completa validada:
  `assembleDebug`, `testDebugUnitTest`, `detekt`, `koverVerifyDebug`, além dos
  testes instrumentados de migração, encoder e repositório Room.
- Documentação atualizada: `docs/specs/checkout-spec.md` e novo
  `docs/adr/0010-payment-method-selection.md`.
