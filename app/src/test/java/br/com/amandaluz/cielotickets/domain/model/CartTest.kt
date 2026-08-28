package br.com.amandaluz.cielotickets.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CartTest {

    @Test
    fun calculatesMultiEventTotalsInCents() {
        val first = CartItem(event("first", 1_999L), quantity = 3)
        val second = CartItem(event("second", 5_000L), quantity = 2)

        val cart = Cart(listOf(first, second))

        assertEquals(5, cart.totalQuantity)
        assertEquals(15_997L, cart.totalInCents)
    }

    @Test
    fun rejectsEmptyCartAndDuplicateEvents() {
        assertThrows(IllegalArgumentException::class.java) {
            Cart(emptyList())
        }
        val item = CartItem(event("same", 1_000L), quantity = 1)
        assertThrows(IllegalArgumentException::class.java) {
            Cart(listOf(item, item))
        }
    }

    @Test
    fun rejectsInvalidItemQuantity() {
        val event = event("event", 1_000L).copy(maxTicketsPerPurchase = 2)

        assertThrows(IllegalArgumentException::class.java) {
            CartItem(event, quantity = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CartItem(event, quantity = 3)
        }
    }

    @Test
    fun copiesMutableItemList() {
        val mutableItems = mutableListOf(
            CartItem(event("event", 1_000L), quantity = 1),
        )

        val cart = Cart(mutableItems)
        mutableItems.clear()

        assertEquals(1, cart.items.size)
        assertEquals(1_000L, cart.totalInCents)
    }

    private fun event(id: String, price: Long) = Event(
        id = id,
        name = id,
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = price,
    )
}
