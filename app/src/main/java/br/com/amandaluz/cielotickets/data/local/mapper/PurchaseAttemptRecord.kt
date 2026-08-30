package br.com.amandaluz.cielotickets.data.local.mapper

import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptWithItems
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod

/**
 * Agrupa os registros pai e filhos necessários para uma inserção transacional.
 */
data class PurchaseAttemptRecord(
    val attempt: PurchaseAttemptEntity,
    val items: List<PurchaseItemEntity>,
)

/**
 * Converte o agregado de domínio para registros Room.
 *
 * A posição de cada item é persistida para restaurar a ordem original.
 */
fun PurchaseAttempt.toRecord(): PurchaseAttemptRecord = PurchaseAttemptRecord(
    attempt = PurchaseAttemptEntity(
        reference = reference,
        status = status.name,
        paymentMethod = paymentMethod.name,
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

/**
 * Restaura o agregado de domínio a partir da relação Room.
 *
 * As entities permanecem restritas à camada de dados, e os itens são ordenados
 * pela posição persistida antes da restauração.
 */
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
    paymentMethod = PaymentMethod.valueOf(attempt.paymentMethod),
    createdAt = attempt.createdAt,
    updatedAt = attempt.updatedAt,
)
