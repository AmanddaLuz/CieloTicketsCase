package br.com.amandaluz.cielotickets.feature.events

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.domain.usecase.impl.BuildCartUseCaseImpl
import br.com.amandaluz.cielotickets.testfake.FakeEventRepository
import br.com.amandaluz.cielotickets.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val event = Event(
        id = "event-1",
        name = "Festival",
        venue = "Praça",
        date = "12 set",
        priceInCents = 3_500L,
        maxTicketsPerPurchase = 2,
    )

    @Test
    fun serializesQuantityChangesAndRespectsEventLimit() {
        val viewModel = viewModel()

        repeat(3) { viewModel.addTicket(event.id) }

        val state = viewModel.uiState.value
        assertEquals(2, state.events.single().quantity)
        assertFalse(state.events.single().canAdd)
        assertEquals(2, state.cart?.totalQuantity)
        assertEquals("7000 cents", state.cart?.totalPrice)
    }

    @Test
    fun removesLastItemAndClosesCart() {
        val viewModel = viewModel()
        viewModel.addTicket(event.id)
        viewModel.setCartOpen(true)
        assertTrue(viewModel.uiState.value.isCartOpen)

        viewModel.removeTicket(event.id)

        assertNull(viewModel.uiState.value.cart)
        assertFalse(viewModel.uiState.value.isCartOpen)
    }

    @Test
    fun clearCartRemovesEverySelection() {
        val viewModel = viewModel()
        viewModel.addTicket(event.id)

        viewModel.clearCart()

        assertEquals(0, viewModel.uiState.value.events.single().quantity)
        assertNull(viewModel.uiState.value.cart)
    }

    @Test
    fun completedCheckoutClearsCartAndKeepsResultSheetOpen() {
        val viewModel = viewModel()
        viewModel.addTicket(event.id)
        viewModel.setCartOpen(true)

        viewModel.completeCheckout()

        assertEquals(0, viewModel.uiState.value.events.single().quantity)
        assertNull(viewModel.uiState.value.cart)
        assertTrue(viewModel.uiState.value.isCartOpen)
    }

    private fun viewModel(): EventsViewModel {
        val repository = FakeEventRepository(listOf(event))
        val getEvents = object : GetAvailableEventsUseCase {
            override suspend fun invoke(): List<Event> = listOf(event)
        }
        val buildCart: BuildCartUseCase = BuildCartUseCaseImpl(repository)
        return EventsViewModel(
            getAvailableEvents = getEvents,
            buildCart = buildCart,
            uiMapper = EventsUiMapper { "$it cents" },
        )
    }
}
