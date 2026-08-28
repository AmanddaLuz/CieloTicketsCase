# Cielo integration constraints

Para sintomas, causas e correções verificadas durante o desenvolvimento,
consulte
[`../troubleshooting/problemas-e-solucoes.md`](../troubleshooting/problemas-e-solucoes.md).

- Cielo is accessed only through the domain `PaymentGateway` contract.
- The purchase UUID is persisted before opening the payment Intent.
- Callback parsing is isolated from Activities and ViewModels.
- The callback URI remains exactly `order://payment`, matching the integration
  validated with the Cielo emulator.
- `CieloResponseActivity` sends a package-scoped broadcast to the active checkout.
- Approved, denied, cancelled, authentication and technical errors are distinct.
- Unknown or missing callbacks never trigger an automatic retry.
- Credentials come from untracked local configuration.
- Logs and QR Codes exclude credentials, PAN and payment-sensitive data.
- The custom URI scheme does not authenticate the sender. Production financial
  fulfillment requires trusted backend/Cielo reconciliation.
