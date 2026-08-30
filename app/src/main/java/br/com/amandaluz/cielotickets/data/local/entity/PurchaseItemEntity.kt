package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Snapshot persistido de um item pertencente a uma tentativa.
 *
 * Nome, quantidade e preço são copiados no momento da compra para que o
 * comprovante não dependa de alterações futuras no catálogo. A chave composta
 * preserva uma posição única, enquanto o índice por tentativa e evento impede
 * que o mesmo evento seja duplicado.
 *
 * Representa os itens pertencentes a uma tentativa.
 *
 * attemptReference  position  eventId  eventName  quantity  unitPriceInCents
 * abc-123           0         event-1  Festival   2         3500
 * abc-123           1         event-2  Teatro     1         5000
 *
 * A chave primária é formada por duas colunas. A posição precisa ser única
 * somente dentro da mesma tentativa.
 * CASCADE: Se uma tentativa for excluída, seus itens são excluídos automaticamente
 *
 * @property attemptReference chave estrangeira da tentativa proprietária.
 * @property position ordem original do item no carrinho.
 * @property eventId identidade estável do evento.
 * @property unitPriceInCents preço unitário em centavos no momento da compra.
 */
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
