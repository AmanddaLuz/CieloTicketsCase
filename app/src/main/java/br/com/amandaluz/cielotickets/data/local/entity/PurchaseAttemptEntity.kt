package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_attempts")
data class PurchaseAttemptEntity(
    @PrimaryKey
    val reference: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
)

