package br.com.amandaluz.cielotickets.payment.timeout

fun interface PaymentProcessingTimeoutScheduler {
    suspend fun schedule(reference: String)
}
