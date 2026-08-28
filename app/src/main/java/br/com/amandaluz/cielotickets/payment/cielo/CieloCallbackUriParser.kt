package br.com.amandaluz.cielotickets.payment.cielo

import android.net.Uri
import android.util.Base64

class CieloCallbackUriParser(
    private val responseParser: CieloCallbackResponseParser,
) {
    fun parse(callbackUri: Uri): CieloCallbackResult? {
        val rawResponse = decodeResponse(callbackUri) ?: return null
        return responseParser.parse(
            rawResponse = rawResponse,
            fallbackReference = callbackUri.getQueryParameter(
                CieloPaymentRequestEncoderImpl.REFERENCE_PARAMETER,
            ),
        )
    }

    private fun decodeResponse(callbackUri: Uri): String? =
        if (isExpectedCallback(callbackUri)) {
            callbackUri.getQueryParameter(
                CieloPaymentRequestEncoderImpl.RESPONSE_PARAMETER,
            )?.let(::decodeBase64)
        } else {
            null
        }

    private fun decodeBase64(encodedResponse: String): String? =
        try {
            Base64.decode(encodedResponse, Base64.DEFAULT).toString(Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun isExpectedCallback(callbackUri: Uri): Boolean =
        callbackUri.scheme == CieloPaymentRequestEncoderImpl.CALLBACK_SCHEME &&
            callbackUri.host == CieloPaymentRequestEncoderImpl.CALLBACK_HOST
}
