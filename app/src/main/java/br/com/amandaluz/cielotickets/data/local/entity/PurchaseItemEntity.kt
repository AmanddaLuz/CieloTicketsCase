package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "purchase_items",
    primaryKeys = ["attemptReference", "position"],
    foreignKeys = [
        ForeignKey(
            entity = PurchaseAttemptEntity::class,
            parentColumns = ["reference"],
            childColumns = ["attemptReference"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("attemptReference"),
        Index(
            value = ["attemptReference", "eventId"],
            unique = true,
        ),
    ],
)
data class PurchaseItemEntity(
    val attemptReference: String,
    val position: Int,
    val eventId: String,
    val eventName: String,
    val quantity: Int,
    val unitPriceInCents: Long,
)

