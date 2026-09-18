# Cielo integration constraints

Para sintomas, causas e correções verificadas durante o desenvolvimento,
consulte
[`../troubleshooting/desafios-tecnicos-e-solucoes.md`](../troubleshooting/desafios-tecnicos-e-solucoes.md).

- Cielo is accessed only through the domain `PaymentGateway` contract.
- The purchase UUID is persisted before opening the payment Intent.
- Callback parsing is isolated from Activities and ViewModels.
- The callback URI remains exactly `order://payment`, matching the integration
  validated with the Cielo emulator.
- `CieloResponseActivity` enqueues referenced callbacks for durable persistence
  and sends a package-scoped broadcast to the active checkout.
- The active reference is persisted before opening Cielo, allowing
  reference-less errors to be correlated after process death.
- The callback brings `MainActivity` to the foreground and the result surface
  observes Room instead of trusting an Intent status.
- A unique WorkManager timeout changes `PROCESSING` to recoverable `TIMED_OUT`
  after one minute without a callback.
- Timeout never repeats the charge and preserves correlation for a late result.
- Approved, denied, cancelled, authentication and technical errors are distinct.
- Unknown or missing callbacks never trigger an automatic retry.
- Credentials come from untracked local configuration.
- Logs and QR Codes exclude credentials, PAN and payment-sensitive data.
- The custom URI scheme does not authenticate the sender. Production financial
  fulfillment requires trusted backend/Cielo reconciliation.
