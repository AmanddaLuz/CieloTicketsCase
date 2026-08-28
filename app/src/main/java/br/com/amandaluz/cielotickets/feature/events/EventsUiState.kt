package br.com.amandaluz.cielotickets.feature.events

data class EventsUiState(
    val isLoading: Boolean = true,
    val events: List<EventItemUiModel> = emptyList(),
    val cart: CartUiModel? = null,
    val isCartOpen: Boolean = false,
    val error: EventsUiError? = null,
)

data class EventItemUiModel(
    val id: String,
    val name: String,
    val venueAndDate: String,
    val price: String,
    val quantity: Int,
    val subtotal: String?,
    val canAdd: Boolean,
    val canRemove: Boolean,
)

data class CartUiModel(
    val items: List<CartItemUiModel>,
    val totalQuantity: Int,
    val totalQuantityLabel: String,
    val totalPrice: String,
)

data class CartItemUiModel(
    val eventId: String,
    val name: String,
    val unitPriceLabel: String,
    val quantity: Int,
    val subtotal: String,
    val canAdd: Boolean,
    val canRemove: Boolean,
)

enum class EventsUiError {
    CART_CHANGED,
    AMOUNT_OVERFLOW,
}

