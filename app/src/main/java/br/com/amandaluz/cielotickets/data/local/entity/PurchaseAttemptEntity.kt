package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade pai da tentativa de compra.
 *
 * A referência é criada no domínio antes da abertura da Cielo e identifica a
 * tentativa no callback, histórico, comprovante e relação com os itens.
 *
 * Cada linha representa uma tentativa de compra, não apenas uma compra aprovada.
 *
 * reference     status       createdAt   updatedAt
 * abc-123       PROCESSING   1000        1200
 * def-456       APPROVED     2000        2300
 * ghi-789       CANCELLED    3000        3400
 *
 * @property status nome persistido do `PaymentStatus`.
 * @property paymentMethod nome persistido do `PaymentMethod` escolhido.
 * @property createdAt instante de criação em milissegundos.
 * @property updatedAt instante da última transição em milissegundos.
 */
@Entity(tableName = "purchase_attempts")
data class PurchaseAttemptEntity(
    @PrimaryKey
    val reference: String,
    val status: String,
    val paymentMethod: String,
    val createdAt: Long,
    val updatedAt: Long,
)
