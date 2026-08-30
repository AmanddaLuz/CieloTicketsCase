package br.com.amandaluz.cielotickets.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PurchaseAttemptTest {

    @Test
    fun createsSnapshotFromMultiEventCart() {
        val cart = Cart(
            listOf(
                CartItem(event("event-1", 3_000L), quantity = 2),
                CartItem(event("event-2", 5_000L), quantity = 1),
            ),
        )

        val attempt = PurchaseAttempt.create(
            reference = "reference-1",
            cart = cart,
            createdAt = 100L,
            paymentMethod = PaymentMethod.CREDIT_CASH,
        )

        assertEquals(PaymentStatus.CREATED, attempt.status)
        assertEquals(3, attempt.totalQuantity)
        assertEquals(11_000L, attempt.totalInCents)
        assertEquals(listOf("event-1", "event-2"), attempt.items.map(PurchaseItem::eventId))
    }

    @Test
    fun createsIndependentReferencesWhenProvidedByCaller() {
        assertNotEquals(attempt("reference-1").reference, attempt("reference-2").reference)
    }

    @Test
    fun rejectsInvalidSnapshotData() {
        assertThrows(IllegalArgumentException::class.java) {
            restoredAttempt(reference = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            restoredAttempt(items = emptyList())
        }
        assertThrows(IllegalArgumentException::class.java) {
            restoredAttempt(items = listOf(purchaseItem(), purchaseItem()))
        }
        assertThrows(IllegalArgumentException::class.java) {
            restoredAttempt(createdAt = 2, updatedAt = 1)
        }
    }

    @Test
    fun copiesMutablePurchaseItems() {
        val mutableItems = mutableListOf(purchaseItem())

        val attempt = restoredAttempt(items = mutableItems)
        mutableItems.clear()

        assertEquals(1, attempt.items.size)
        assertEquals(6_000L, attempt.totalInCents)
    }

    private fun attempt(reference: String) = restoredAttempt(reference = reference)

    private fun restoredAttempt(
        reference: String = "reference-1",
        items: List<PurchaseItem> = listOf(purchaseItem()),
        createdAt: Long = 1,
        updatedAt: Long = createdAt,
    ) = PurchaseAttempt.restore(
        reference = reference,
        items = items,
        status = PaymentStatus.CREATED,
        createdAt = createdAt,
        updatedAt = updatedAt,
        paymentMethod = PaymentMethod.CREDIT_CASH,
    )

    private fun purchaseItem() = PurchaseItem(
        eventId = "event-1",
        eventName = "Festival",
        quantity = 2,
        unitPriceInCents = 3_000L,
    )

    private fun event(id: String, price: Long) = Event(
        id = id,
        name = id,
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = price,
    )
}
