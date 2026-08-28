package br.com.amandaluz.cielotickets.domain.gateway

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

data class PaymentResult(
    val reference: String,
    val status: PaymentStatus,
    val errorMessage: String?,
)

interface PaymentResultObserver {
    fun start(onResult: (PaymentResult) -> Unit)

    fun stop()
}
