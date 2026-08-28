package br.com.amandaluz.cielotickets.domain.model

/**
 * Snapshot de um item no momento da compra.
 *
 * O snapshot preserva nome e preço mesmo que o catálogo seja alterado depois.
 */
data class PurchaseItem(
    val eventId: String,
    val eventName: String,
    val quantity: Int,
    val unitPriceInCents: Long,
) {
    init {
        require(eventId.isNotBlank()) { "Purchase event id must not be blank" }
        require(eventName.isNotBlank()) { "Purchase event name must not be blank" }
        require(quantity > 0) { "Purchase quantity must be positive" }
        require(unitPriceInCents > 0) { "Purchase unit price must be positive" }
    }

    val subtotalInCents: Long =
        Math.multiplyExact(unitPriceInCents, quantity.toLong())

    companion object {
        fun from(cartItem: CartItem): PurchaseItem = PurchaseItem(
            eventId = cartItem.event.id,
            eventName = cartItem.event.name,
            quantity = cartItem.quantity,
            unitPriceInCents = cartItem.event.priceInCents,
        )
    }
}

/**
 * Tentativa persistível de compra identificada por uma referência idempotente.
 */
class PurchaseAttempt private constructor(
    val reference: String,
    items: List<PurchaseItem>,
    val status: PaymentStatus,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val items: List<PurchaseItem> = items.toList()

    init {
        require(reference.isNotBlank()) { "Purchase reference must not be blank" }
        require(this.items.isNotEmpty()) { "Purchase attempt must contain items" }
        require(this.items.map(PurchaseItem::eventId).distinct().size == this.items.size) {
            "Purchase attempt must not contain duplicate events"
        }
        require(createdAt >= 0) { "Creation timestamp must not be negative" }
        require(updatedAt >= createdAt) {
            "Update timestamp must not be earlier than creation timestamp"
        }
    }

    val totalQuantity: Int = this.items.fold(0) { total, item ->
        Math.addExact(total, item.quantity)
    }

    val totalInCents: Long = this.items.fold(0L) { total, item ->
        Math.addExact(total, item.subtotalInCents)
    }

    fun withStatus(
        newStatus: PaymentStatus,
        updatedAt: Long,
    ): PurchaseAttempt = restore(
        reference = reference,
        items = items,
        status = newStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun create(
            reference: String,
            cart: Cart,
            createdAt: Long,
        ): PurchaseAttempt = PurchaseAttempt(
            reference = reference,
            items = cart.items.map(PurchaseItem::from),
            status = PaymentStatus.CREATED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

        fun restore(
            reference: String,
            items: List<PurchaseItem>,
            status: PaymentStatus,
            createdAt: Long,
            updatedAt: Long,
        ): PurchaseAttempt = PurchaseAttempt(
            reference = reference,
            items = items.toList(),
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
