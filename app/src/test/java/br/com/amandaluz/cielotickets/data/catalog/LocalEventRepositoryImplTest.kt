package br.com.amandaluz.cielotickets.data.catalog

import br.com.amandaluz.cielotickets.domain.repository.EventRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalEventRepositoryImplTest {
    private val repository: EventRepository = LocalEventRepositoryImpl()

    @Test
    fun exposesExpectedCatalog() = runTest {
        val events = repository.getAvailableEvents()

        assertEquals(EXPECTED_EVENT_COUNT, events.size)
        assertEquals(events.size, events.map { it.id }.distinct().size)
        assertTrue(events.all { it.priceInCents > 0 })
    }

    @Test
    fun findsKnownEventById() = runTest {
        val event = repository.getAvailableEvents().first()

        assertNotNull(repository.findById(event.id))
    }

    @Test
    fun returnsNullForUnknownEvent() = runTest {
        assertNull(repository.findById("unknown"))
    }

    private companion object {
        const val EXPECTED_EVENT_COUNT = 10
    }
}

