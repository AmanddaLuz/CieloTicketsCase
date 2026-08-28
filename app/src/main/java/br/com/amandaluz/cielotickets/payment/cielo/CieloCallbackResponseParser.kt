package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import org.json.JSONException
import org.json.JSONObject

/**
 * Converte o corpo decodificado do callback Cielo em um resultado técnico
 * tipado, incluindo respostas de erro que não possuem referência.
 */
class CieloCallbackResponseParser {

    fun parse(rawResponse: String): CieloCallbackResult? {
        val json = parseJson(rawResponse) ?: return null
        return if (json.has("code")) {
            parseError(json)
        } else {
            parseApproved(json)
        }
    }

    private fun parseJson(rawResponse: String): JSONObject? =
        try {
            JSONObject(rawResponse)
        } catch (_: JSONException) {
            null
        }

    private fun parseError(json: JSONObject): CieloCallbackResult =
        CieloCallbackResult(
            reference = "",
            status = mapBodyCode(json.optInt("code", UNKNOWN_CODE)),
            errorMessage = json.optString("reason")
                .takeIf(String::isNotBlank)
                ?: DEFAULT_ERROR_MESSAGE,
        )

    private fun parseApproved(json: JSONObject): CieloCallbackResult? =
        json.optString("reference")
            .takeIf(String::isNotBlank)
            ?.let { reference ->
                CieloCallbackResult(
                    reference = reference,
                    status = PaymentStatus.APPROVED,
                    errorMessage = null,
                )
            }

    private fun mapBodyCode(code: Int): PaymentStatus = when (code) {
        CANCELLED_CODE -> PaymentStatus.CANCELLED
        GENERIC_ERROR_CODE, PAYMENT_DENIED_CODE -> PaymentStatus.DENIED
        AUTH_ERROR_CODE -> PaymentStatus.ERROR
        else -> PaymentStatus.ERROR
    }

    private companion object {
        const val UNKNOWN_CODE = -1
        const val CANCELLED_CODE = 1
        const val GENERIC_ERROR_CODE = 2
        const val PAYMENT_DENIED_CODE = 3
        const val AUTH_ERROR_CODE = 4
        const val DEFAULT_ERROR_MESSAGE = "Erro desconhecido"
    }
}
