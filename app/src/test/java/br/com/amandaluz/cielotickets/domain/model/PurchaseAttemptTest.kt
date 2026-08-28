package br.com.amandaluz.cielotickets.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PurchaseAttemptTest {

    @Test
    fun defaultStatusIsCreated() {
        val attempt = purchaseAttempt()

        assertEquals(PaymentStatus.CREATED, attempt.status)
    }

    @Test
    fun referenceIsUniquePerInstance() {
        assertNotEquals(purchaseAttempt().reference, purchaseAttempt().reference)
    }

    @Test
    fun statusChangePreservesReference() {
        val original = purchaseAttempt()

        val updated = original.copy(status = PaymentStatus.APPROVED)

        assertEquals(original.reference, updated.reference)
        assertEquals(PaymentStatus.APPROVED, updated.status)
    }

    @Test
    fun totalIsStoredInLongCents() {
        assertEquals(12_000L, purchaseAttempt().totalInCents)
    }

    private fun purchaseAttempt() = PurchaseAttempt(
        eventId = "event-1",
        eventName = "Show",
        quantity = 2,
        totalInCents = 12_000L,
    )
}

