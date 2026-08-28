package br.com.amandaluz.cielotickets.domain.model

import java.util.UUID

data class PurchaseAttempt(
    val reference: String = UUID.randomUUID().toString(),
    val eventId: String,
    val eventName: String,
    val quantity: Int,
    val totalInCents: Long,
    val status: PaymentStatus = PaymentStatus.CREATED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

