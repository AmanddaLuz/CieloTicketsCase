package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseAttemptWithItems(
    @Embedded
    val attempt: PurchaseAttemptEntity,
    @Relation(
        parentColumn = "reference",
        entityColumn = "attemptReference",
    )
    val items: List<PurchaseItemEntity>,
)

