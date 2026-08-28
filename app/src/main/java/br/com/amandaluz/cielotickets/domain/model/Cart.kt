package br.com.amandaluz.cielotickets.domain.model

/** Item validado do carrinho com preço obtido do catálogo de domínio. */
data class CartItem(
    val event: Event,
    val quantity: Int,
) {
    init {
        require(quantity > 0) { "Ticket quantity must be positive" }
        require(quantity <= event.maxTicketsPerPurchase) {
            "Ticket quantity exceeds the event limit"
        }
    }

    val subtotalInCents: Long =
        Math.multiplyExact(event.priceInCents, quantity.toLong())
}

/**
 * Carrinho imutável responsável por garantir unicidade, quantidade e totais
 * monetários exatos.
 */
class Cart(items: List<CartItem>) {
    val items: List<CartItem> = items.toList()

    init {
        require(this.items.isNotEmpty()) { "Cart must contain at least one item" }
        require(this.items.map { it.event.id }.distinct().size == this.items.size) {
            "Cart must not contain duplicate events"
        }
    }

    val totalQuantity: Int = this.items.fold(0) { total, item ->
        Math.addExact(total, item.quantity)
    }

    val totalInCents: Long = this.items.fold(0L) { total, item ->
        Math.addExact(total, item.subtotalInCents)
    }
}
