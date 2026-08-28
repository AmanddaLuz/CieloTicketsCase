package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.gateway.PaymentGateway
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

class CieloPaymentGatewayImpl(
    private val clientId: String,
    private val accessToken: String,
    private val requestEncoder: CieloPaymentRequestEncoder,
    private val intentLauncher: CieloPaymentIntentLauncher,
) : PaymentGateway {

    override fun initiatePayment(attempt: PurchaseAttempt): PaymentGateway.Result {
        if (clientId.isBlank() || accessToken.isBlank()) {
            return PaymentGateway.Result.CredentialsNotConfigured
        }

        val paymentUri = requestEncoder.encode(
            attempt = attempt,
            clientId = clientId,
            accessToken = accessToken,
        )
        return when (intentLauncher.launch(paymentUri)) {
            CieloPaymentIntentLauncher.Result.Launched -> PaymentGateway.Result.Initiated
            CieloPaymentIntentLauncher.Result.AppNotAvailable -> {
                PaymentGateway.Result.AppNotAvailable
            }
            CieloPaymentIntentLauncher.Result.TechnicalFailure -> {
                PaymentGateway.Result.TechnicalFailure
            }
        }
    }
}
