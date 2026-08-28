package br.com.amandaluz.cielotickets.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test

class EventTest {

    @Test
    fun rejectsBlankIdentityFields() {
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(id = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(name = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(venue = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(date = "")
        }
    }

    @Test
    fun rejectsInvalidPriceAndTicketLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(priceInCents = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            event().copy(maxTicketsPerPurchase = 0)
        }
    }

    private fun event() = Event(
        id = "event-1",
        name = "Festival",
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = 5_000L,
    )
}

