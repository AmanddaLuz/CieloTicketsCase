package br.com.amandaluz.cielotickets.feature.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.feature.events.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.feature.events.EventsUiError
import br.com.amandaluz.cielotickets.feature.events.EventsUiMapper
import br.com.amandaluz.cielotickets.feature.events.EventsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mantém o catálogo e o único carrinho válido da feature.
 *
 * Mutações são serializadas e somente um [Cart] aprovado por [BuildCartUseCase]
 * é publicado para as Views e para o checkout.
 */
class EventsViewModel(
    private val getAvailableEvents: GetAvailableEventsUseCase,
    private val buildCart: BuildCartUseCase,
    private val uiMapper: EventsUiMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = mutableUiState.asStateFlow()

    private val mutationMutex = Mutex()
    private var events: List<Event> = emptyList()
    private var selections: Map<String, Int> = emptyMap()
    private var cart: Cart? = null

    val checkoutCart: Cart?
        get() = cart

    init {
        loadEvents()
    }

    fun addTicket(eventId: String) {
        mutateSelection(eventId) { event, currentQuantity ->
            (currentQuantity + 1).coerceAtMost(event.maxTicketsPerPurchase)
        }
    }

    fun removeTicket(eventId: String) {
        mutateSelection(eventId) { _, currentQuantity ->
            (currentQuantity - 1).coerceAtLeast(0)
        }
    }

    fun setCartOpen(isOpen: Boolean) {
        mutableUiState.update { state ->
            if (isOpen && state.cart == null) {
                state
            } else {
                state.copy(isCartOpen = isOpen)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            mutationMutex.withLock {
                selections = emptyMap()
                cart = null
                publishState(isCartOpen = false)
            }
        }
    }

    fun completeCheckout() {
        viewModelScope.launch {
            mutationMutex.withLock {
                selections = emptyMap()
                cart = null
                publishState(isCartOpen = true)
            }
        }
    }

    fun consumeError() {
        mutableUiState.update { it.copy(error = null) }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            events = getAvailableEvents()
            publishState()
        }
    }

    private fun mutateSelection(
        eventId: String,
        quantityChange: (Event, Int) -> Int,
    ) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val event = events.firstOrNull { it.id == eventId }
                    ?: return@withLock
                val quantity = quantityChange(event, selections[eventId] ?: 0)
                val candidate = if (quantity == 0) {
                    selections - eventId
                } else {
                    selections + (eventId to quantity)
                }
                applyCandidate(candidate)
            }
        }
    }

    private suspend fun applyCandidate(candidate: Map<String, Int>) {
        if (candidate.isEmpty()) {
            selections = emptyMap()
            cart = null
            publishState(isCartOpen = false)
            return
        }

        when (val result = buildCart(candidate)) {
            is BuildCartUseCase.Result.Success -> {
                selections = candidate
                cart = result.cart
                publishState()
            }
            BuildCartUseCase.Result.AmountOverflow -> {
                mutableUiState.update { it.copy(error = EventsUiError.AMOUNT_OVERFLOW) }
            }
            BuildCartUseCase.Result.EmptyCart,
            is BuildCartUseCase.Result.EventNotFound,
            is BuildCartUseCase.Result.NonPositiveQuantity,
            is BuildCartUseCase.Result.QuantityLimitExceeded,
            -> mutableUiState.update { it.copy(error = EventsUiError.CART_CHANGED) }
        }
    }

    private fun publishState(
        isCartOpen: Boolean = mutableUiState.value.isCartOpen,
    ) {
        mutableUiState.value = EventsUiState(
            isLoading = false,
            events = uiMapper.mapEvents(events, cart),
            cart = uiMapper.mapCart(cart),
            isCartOpen = isCartOpen,
            error = null,
        )
    }
}
