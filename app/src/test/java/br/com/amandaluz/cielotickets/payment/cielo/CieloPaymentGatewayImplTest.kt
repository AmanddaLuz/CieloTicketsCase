package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.gateway.PaymentGateway
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CieloPaymentGatewayImplTest {

    @Test
    fun doesNotBuildOrLaunchRequestWithoutCredentials() {
        val encoder = FakeRequestEncoder()
        val launcher = FakeIntentLauncher(CieloPaymentIntentLauncher.Result.Launched)
        val gateway = CieloPaymentGatewayImpl(
            clientId = "",
            accessToken = "token",
            requestEncoder = encoder,
            intentLauncher = launcher,
        )

        assertEquals(
            PaymentGateway.Result.CredentialsNotConfigured,
            gateway.initiatePayment(attempt()),
        )
        assertNull(encoder.encodedAttempt)
        assertFalse(launcher.called)
    }

    @Test
    fun reportsInitiatedAfterLaunchingEncodedRequest() {
        val encoder = FakeRequestEncoder()
        val launcher = FakeIntentLauncher(CieloPaymentIntentLauncher.Result.Launched)
        val gateway = CieloPaymentGatewayImpl(
            clientId = "client",
            accessToken = "token",
            requestEncoder = encoder,
            intentLauncher = launcher,
        )
        val attempt = attempt()

        assertEquals(
            PaymentGateway.Result.Initiated,
            gateway.initiatePayment(attempt),
        )
        assertEquals(attempt, encoder.encodedAttempt)
        assertEquals("lio://encoded", launcher.paymentUri)
    }

    @Test
    fun mapsLauncherFailuresToTypedGatewayResults() {
        val attempt = attempt()

        assertEquals(
            PaymentGateway.Result.AppNotAvailable,
            gatewayWith(CieloPaymentIntentLauncher.Result.AppNotAvailable)
                .initiatePayment(attempt),
        )
        assertEquals(
            PaymentGateway.Result.TechnicalFailure,
            gatewayWith(CieloPaymentIntentLauncher.Result.TechnicalFailure)
                .initiatePayment(attempt),
        )
    }

    private fun gatewayWith(
        launcherResult: CieloPaymentIntentLauncher.Result,
    ) = CieloPaymentGatewayImpl(
        clientId = "client",
        accessToken = "token",
        requestEncoder = FakeRequestEncoder(),
        intentLauncher = FakeIntentLauncher(launcherResult),
    )

    private fun attempt() = PurchaseAttempt.restore(
        reference = "reference-1",
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 1,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = PaymentStatus.PROCESSING,
        createdAt = 100L,
        updatedAt = 200L,
    )

    private class FakeRequestEncoder : CieloPaymentRequestEncoder {
        var encodedAttempt: PurchaseAttempt? = null

        override fun encode(
            attempt: PurchaseAttempt,
            clientId: String,
            accessToken: String,
        ): String {
            encodedAttempt = attempt
            assertEquals("client", clientId)
            assertEquals("token", accessToken)
            return "lio://encoded"
        }
    }

    private class FakeIntentLauncher(
        private val result: CieloPaymentIntentLauncher.Result,
    ) : CieloPaymentIntentLauncher {
        var called = false
        var paymentUri: String? = null

        override fun launch(paymentUri: String): CieloPaymentIntentLauncher.Result {
            called = true
            this.paymentUri = paymentUri
            return result
        }
    }
}
