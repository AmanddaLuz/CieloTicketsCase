package br.com.amandaluz.cielotickets.payment.cielo.encoder

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

fun interface CieloPaymentRequestEncoder {
    fun encode(
        attempt: PurchaseAttempt,
        clientId: String,
        accessToken: String,
    ): String
}
