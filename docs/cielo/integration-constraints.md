# Cielo integration constraints

- Cielo is accessed only through the domain `PaymentGateway` contract.
- The purchase UUID is persisted before opening the payment Intent.
- Callback parsing is isolated from Activities and ViewModels.
- Approved, denied, cancelled, authentication and technical errors are distinct.
- Unknown or missing callbacks never trigger an automatic retry.
- Credentials come from untracked local configuration.
- Logs and QR Codes exclude credentials, PAN and payment-sensitive data.

