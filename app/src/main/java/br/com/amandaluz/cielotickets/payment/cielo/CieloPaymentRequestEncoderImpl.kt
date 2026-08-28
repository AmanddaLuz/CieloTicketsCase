package br.com.amandaluz.cielotickets.payment.cielo

import android.net.Uri
import android.util.Base64
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import org.json.JSONArray
import org.json.JSONObject

class CieloPaymentRequestEncoderImpl : CieloPaymentRequestEncoder {

    override fun encode(
        attempt: PurchaseAttempt,
        clientId: String,
        accessToken: String,
    ): String {
        val payload = JSONObject().apply {
            put("accessToken", accessToken)
            put("clientID", clientId)
            put("reference", attempt.reference)
            put("value", attempt.totalInCents)
            put("paymentCode", PAYMENT_CODE)
            put(
                "items",
                JSONArray().apply {
                    attempt.items.forEach { item ->
                        put(
                            JSONObject().apply {
                                put("name", item.eventName)
                                put("quantity", item.quantity)
                                put("sku", item.eventId)
                                put("unitOfMeasure", UNIT_OF_MEASURE)
                                put("unitPrice", item.unitPriceInCents)
                            },
                        )
                    }
                },
            )
        }.toString()
        val encodedPayload = Base64.encodeToString(
            payload.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP,
        )
        return Uri.parse(PAYMENT_URI).buildUpon()
            .appendQueryParameter(REQUEST_PARAMETER, encodedPayload)
            .appendQueryParameter(CALLBACK_PARAMETER, CALLBACK_URI)
            .build()
            .toString()
    }

    companion object {
        const val CALLBACK_SCHEME = "order"
        const val CALLBACK_HOST = "payment"
        const val RESPONSE_PARAMETER = "response"

        private const val PAYMENT_URI = "lio://payment"
        private const val CALLBACK_URI = "order://payment"
        private const val PAYMENT_CODE = "CREDITO_AVISTA"
        private const val UNIT_OF_MEASURE = "UNIDADE"
        private const val REQUEST_PARAMETER = "request"
        private const val CALLBACK_PARAMETER = "urlCallback"
    }
}
