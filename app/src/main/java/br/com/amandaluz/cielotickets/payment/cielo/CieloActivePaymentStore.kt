package br.com.amandaluz.cielotickets.payment.cielo

interface CieloActivePaymentStore {
    fun claim(reference: String): Boolean

    fun currentReference(): String?

    fun replace(
        expectedReference: String,
        newReference: String,
    ): Boolean

    fun clear(reference: String)
}
