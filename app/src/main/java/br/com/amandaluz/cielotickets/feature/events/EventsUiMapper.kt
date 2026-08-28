package br.com.amandaluz.cielotickets.feature.events

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event

class EventsUiMapper(
    private val formatCurrency: (Long) -> String,
) {
    fun mapEvents(
        events: List<Event>,
        cart: Cart?,
    ): List<EventItemUiModel> {
        val cartItemsByEvent = cart?.items?.associateBy { it.event.id }.orEmpty()
        return events.map { event ->
            mapEvent(event, cartItemsByEvent[event.id])
        }
    }

    fun mapCart(cart: Cart?): CartUiModel? = cart?.let { currentCart ->
        CartUiModel(
            items = currentCart.items
                .sortedBy { it.event.name }
                .map(::mapCartItem),
            totalQuantity = currentCart.totalQuantity,
            totalQuantityLabel = quantityLabel(currentCart.totalQuantity),
            totalPrice = formatCurrency(currentCart.totalInCents),
        )
    }

    private fun mapEvent(
        event: Event,
        cartItem: CartItem?,
    ): EventItemUiModel {
        val quantity = cartItem?.quantity ?: 0
        return EventItemUiModel(
            id = event.id,
            name = event.name,
            venueAndDate = "${event.venue} • ${event.date}",
            price = formatCurrency(event.priceInCents),
            quantity = quantity,
            subtotal = cartItem?.subtotalInCents?.let(formatCurrency),
            canAdd = quantity < event.maxTicketsPerPurchase,
            canRemove = quantity > 0,
        )
    }

    private fun mapCartItem(item: CartItem): CartItemUiModel =
        CartItemUiModel(
            eventId = item.event.id,
            name = item.event.name,
            unitPriceLabel = "${formatCurrency(item.event.priceInCents)} / ingresso",
            quantity = item.quantity,
            subtotal = formatCurrency(item.subtotalInCents),
            canAdd = item.quantity < item.event.maxTicketsPerPurchase,
            canRemove = true,
        )

    private fun quantityLabel(quantity: Int): String =
        if (quantity == SINGLE_ITEM) {
            "$quantity ingresso"
        } else {
            "$quantity ingressos"
        }

    private companion object {
        const val SINGLE_ITEM = 1
    }
}

