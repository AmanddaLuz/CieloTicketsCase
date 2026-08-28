package br.com.amandaluz.cielotickets.feature.events

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class EventsUiMapperTest {
    private val mapper = EventsUiMapper { "$it cents" }
    private val event = Event(
        id = "event-1",
        name = "Festival",
        venue = "Praça",
        date = "12 set",
        priceInCents = 3_500L,
        maxTicketsPerPurchase = 2,
    )

    @Test
    fun mapsDomainCartWithoutRecalculatingTotalsInViews() {
        val cart = Cart(listOf(CartItem(event, quantity = 2)))

        val eventItem = mapper.mapEvents(listOf(event), cart).single()
        val cartUi = requireNotNull(mapper.mapCart(cart))

        assertEquals(2, eventItem.quantity)
        assertEquals("7000 cents", eventItem.subtotal)
        assertFalse(eventItem.canAdd)
        assertEquals(2, cartUi.totalQuantity)
        assertEquals("2 ingressos", cartUi.totalQuantityLabel)
        assertEquals("7000 cents", cartUi.totalPrice)
        assertEquals("7000 cents", cartUi.items.single().subtotal)
    }

    @Test
    fun mapsUnselectedEventAndEmptyCart() {
        val eventItem = mapper.mapEvents(listOf(event), cart = null).single()

        assertEquals(0, eventItem.quantity)
        assertNull(eventItem.subtotal)
        assertNull(mapper.mapCart(null))
    }
}

