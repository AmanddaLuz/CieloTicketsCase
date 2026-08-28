package br.com.amandaluz.cielotickets.data.local.mapper

import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptWithItems
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem

data class PurchaseAttemptRecord(
    val attempt: PurchaseAttemptEntity,
    val items: List<PurchaseItemEntity>,
)

fun PurchaseAttempt.toRecord(): PurchaseAttemptRecord = PurchaseAttemptRecord(
    attempt = PurchaseAttemptEntity(
        reference = reference,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    ),
    items = items.mapIndexed { index, item ->
        PurchaseItemEntity(
            attemptReference = reference,
            position = index,
            eventId = item.eventId,
            eventName = item.eventName,
            quantity = item.quantity,
            unitPriceInCents = item.unitPriceInCents,
        )
    },
)

fun PurchaseAttemptWithItems.toDomain(): PurchaseAttempt = PurchaseAttempt.restore(
    reference = attempt.reference,
    items = items.sortedBy(PurchaseItemEntity::position).map { entity ->
        PurchaseItem(
            eventId = entity.eventId,
            eventName = entity.eventName,
            quantity = entity.quantity,
            unitPriceInCents = entity.unitPriceInCents,
        )
    },
    status = PaymentStatus.valueOf(attempt.status),
    createdAt = attempt.createdAt,
    updatedAt = attempt.updatedAt,
)
