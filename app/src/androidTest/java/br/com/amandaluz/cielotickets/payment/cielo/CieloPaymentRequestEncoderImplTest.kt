package br.com.amandaluz.cielotickets.payment.cielo

import android.net.Uri
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieloPaymentRequestEncoderImplTest {

    @Test
    fun encodesEveryPurchaseItemAndKnownWorkingCallback() {
        val paymentUri = Uri.parse(
            CieloPaymentRequestEncoderImpl().encode(
                attempt = attempt(),
                clientId = "client",
                accessToken = "token",
            ),
        )
        val encodedPayload = requireNotNull(paymentUri.getQueryParameter("request"))
        val payload = JSONObject(
            Base64.decode(encodedPayload, Base64.DEFAULT).toString(Charsets.UTF_8),
        )
        val callbackUri = Uri.parse(
            requireNotNull(paymentUri.getQueryParameter("urlCallback")),
        )

        assertEquals("lio", paymentUri.scheme)
        assertEquals("payment", paymentUri.host)
        assertEquals("reference-1", payload.getString("reference"))
        assertEquals(11_000L, payload.getLong("value"))
        assertEquals(2, payload.getJSONArray("items").length())
        assertEquals(
            "event-2",
            payload.getJSONArray("items").getJSONObject(1).getString("sku"),
        )
        assertEquals("order", callbackUri.scheme)
        assertEquals("payment", callbackUri.host)
        assertNull(callbackUri.query)
    }

    private fun attempt() = PurchaseAttempt.restore(
        reference = "reference-1",
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 2,
                unitPriceInCents = 3_000L,
            ),
            PurchaseItem(
                eventId = "event-2",
                eventName = "Show",
                quantity = 1,
                unitPriceInCents = 5_000L,
            ),
        ),
        status = PaymentStatus.PROCESSING,
        createdAt = 100L,
        updatedAt = 200L,
    )
}
