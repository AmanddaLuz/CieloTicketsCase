package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.gateway.PaymentGateway
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.checkout.ActivePaymentCoordinator
import br.com.amandaluz.cielotickets.payment.cielo.encoder.CieloPaymentRequestEncoder
import br.com.amandaluz.cielotickets.payment.cielo.launcher.CieloPaymentIntentLauncher

/**
 * Adapter que traduz uma [PurchaseAttempt] para o Deep Link da Cielo Smart.
 *
 * Credenciais, serialização e abertura do Intent permanecem isoladas do
 * domínio por contratos específicos.
 */
class CieloPaymentGatewayImpl(
    private val clientId: String,
    private val accessToken: String,
    private val requestEncoder: CieloPaymentRequestEncoder,
    private val intentLauncher: CieloPaymentIntentLauncher,
    private val activePaymentCoordinator: ActivePaymentCoordinator,
) : PaymentGateway {

    override suspend fun initiatePayment(
        attempt: PurchaseAttempt,
    ): PaymentGateway.Result =
        when {
            clientId.isBlank() || accessToken.isBlank() -> {
                PaymentGateway.Result.CredentialsNotConfigured
            }
            else -> initiateConfiguredPayment(attempt)
        }

    private suspend fun initiateConfiguredPayment(
        attempt: PurchaseAttempt,
    ): PaymentGateway.Result {
        val paymentUri = requestEncoder.encode(
            attempt = attempt,
            clientId = clientId,
            accessToken = accessToken,
        )
        if (!activePaymentCoordinator.activate(attempt.reference)) {
            return PaymentGateway.Result.TechnicalFailure
        }
        return when (intentLauncher.launch(paymentUri)) {
            CieloPaymentIntentLauncher.Result.Launched -> PaymentGateway.Result.Initiated
            CieloPaymentIntentLauncher.Result.AppNotAvailable -> {
                activePaymentCoordinator.clear(attempt.reference)
                PaymentGateway.Result.AppNotAvailable
            }
            CieloPaymentIntentLauncher.Result.TechnicalFailure -> {
                activePaymentCoordinator.clear(attempt.reference)
                PaymentGateway.Result.TechnicalFailure
            }
        }
    }
}
