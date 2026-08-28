package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

data class CieloCallbackResult(
    val reference: String,
    val status: PaymentStatus,
    val paidAmountInCents: Long?,
    val errorMessage: String?,
)
