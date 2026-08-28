package br.com.amandaluz.cielotickets.data.local.mapper

import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptWithItems
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseAttemptRecordTest {

    @Test
    fun mapsDomainAttemptToRelationalRecord() {
        val record = attempt().toRecord()

        assertEquals("reference-1", record.attempt.reference)
        assertEquals("PROCESSING", record.attempt.status)
        assertEquals(listOf(0, 1), record.items.map { it.position })
        assertEquals(listOf("event-1", "event-2"), record.items.map { it.eventId })
    }

    @Test
    fun restoresDomainAttemptInOriginalItemOrder() {
        val source = attempt()
        val record = source.toRecord()
        val restored = PurchaseAttemptWithItems(
            attempt = record.attempt,
            items = record.items.reversed(),
        ).toDomain()

        assertEquals(source.reference, restored.reference)
        assertEquals(source.status, restored.status)
        assertEquals(source.createdAt, restored.createdAt)
        assertEquals(source.updatedAt, restored.updatedAt)
        assertEquals(source.items, restored.items)
        assertEquals(source.totalInCents, restored.totalInCents)
    }

    private fun attempt() = PurchaseAttempt.restore(
        reference = "reference-1",
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 2,
                unitPriceInCents = 3_000L,
            ),
            PurchaseItem(
                eventId = "event-2",
                eventName = "Show",
                quantity = 1,
                unitPriceInCents = 5_000L,
            ),
        ),
        status = PaymentStatus.PROCESSING,
        createdAt = 100L,
        updatedAt = 200L,
    )
}

