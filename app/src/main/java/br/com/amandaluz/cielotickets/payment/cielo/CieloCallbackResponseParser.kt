package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import org.json.JSONException
import org.json.JSONObject

class CieloCallbackResponseParser {

    fun parse(
        rawResponse: String,
        fallbackReference: String?,
    ): CieloCallbackResult? {
        val json = parseJson(rawResponse) ?: return null
        return if (json.has("code")) {
            parseErrorResponse(json, fallbackReference)
        } else {
            parseApprovedResponse(json, fallbackReference)
        }
    }

    private fun parseJson(rawResponse: String): JSONObject? =
        try {
            JSONObject(rawResponse)
        } catch (_: JSONException) {
            null
        }

    private fun parseErrorResponse(
        json: JSONObject,
        fallbackReference: String?,
    ): CieloCallbackResult? =
        resolveErrorReference(json, fallbackReference)?.let { reference ->
            CieloCallbackResult(
                reference = reference,
                status = mapBodyCode(json.optInt("code", UNKNOWN_CODE)),
                paidAmountInCents = null,
                errorMessage = json.optString("reason")
                    .takeIf(String::isNotBlank)
                    ?: DEFAULT_ERROR_MESSAGE,
            )
        }

    private fun parseApprovedResponse(
        json: JSONObject,
        fallbackReference: String?,
    ): CieloCallbackResult? {
        val reference = json.optString("reference").takeIf(String::isNotBlank)
        val paidAmount = json.optLong("paidAmount", INVALID_AMOUNT)
        val isCorrelated = reference != null &&
            fallbackReference != null &&
            reference == fallbackReference
        val hasOrderEvidence = json.optString("id").isNotBlank() &&
            json.optJSONArray("items")?.length()?.let { it > 0 } == true &&
            hasApprovedPayment(json) &&
            paidAmount > 0L

        return if (isCorrelated && hasOrderEvidence) {
            CieloCallbackResult(
                reference = requireNotNull(reference),
                status = PaymentStatus.APPROVED,
                paidAmountInCents = paidAmount,
                errorMessage = null,
            )
        } else {
            null
        }
    }

    private fun resolveErrorReference(
        json: JSONObject,
        fallbackReference: String?,
    ): String? {
        val responseReference = json.optString("reference")
            .takeIf(String::isNotBlank)
        val referencesMatch = responseReference == null ||
            fallbackReference == null ||
            responseReference == fallbackReference
        return if (referencesMatch) {
            responseReference ?: fallbackReference?.takeIf(String::isNotBlank)
        } else {
            null
        }
    }

    private fun hasApprovedPayment(json: JSONObject): Boolean {
        val payments = json.optJSONArray("payments") ?: return false
        return (0 until payments.length()).any { index ->
            val statusCode = payments.optJSONObject(index)
                ?.optJSONObject("paymentFields")
                ?.optString("statusCode")
            statusCode == PIX_APPROVED_STATUS || statusCode == CARD_APPROVED_STATUS
        }
    }

    private fun mapBodyCode(code: Int): PaymentStatus = when (code) {
        CANCELLED_CODE -> PaymentStatus.CANCELLED
        PAYMENT_DENIED_CODE -> PaymentStatus.DENIED
        GENERIC_ERROR_CODE, AUTH_ERROR_CODE -> PaymentStatus.ERROR
        else -> PaymentStatus.ERROR
    }

    private companion object {
        const val UNKNOWN_CODE = -1
        const val INVALID_AMOUNT = -1L
        const val CANCELLED_CODE = 1
        const val GENERIC_ERROR_CODE = 2
        const val PAYMENT_DENIED_CODE = 3
        const val AUTH_ERROR_CODE = 4
        const val PIX_APPROVED_STATUS = "0"
        const val CARD_APPROVED_STATUS = "1"
        const val DEFAULT_ERROR_MESSAGE = "Erro desconhecido"
    }
}
