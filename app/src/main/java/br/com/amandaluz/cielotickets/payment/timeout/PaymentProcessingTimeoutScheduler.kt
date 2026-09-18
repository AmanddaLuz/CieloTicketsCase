package br.com.amandaluz.cielotickets.payment.timeout

interface PaymentProcessingTimeoutScheduler {
    suspend fun schedule(reference: String)
}
