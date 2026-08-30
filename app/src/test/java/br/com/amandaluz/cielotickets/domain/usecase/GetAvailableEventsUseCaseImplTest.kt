package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCaseImpl
import br.com.amandaluz.cielotickets.testfake.FakeEventRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAvailableEventsUseCaseImplTest {

    @Test
    fun returnsRepositoryCatalog() = runTest {
        val events = listOf(event("event-1"), event("event-2"))
        val useCase: GetAvailableEventsUseCase =
            GetAvailableEventsUseCaseImpl(FakeEventRepository(events))

        assertEquals(events, useCase())
    }

    @Test
    fun returnsEmptyCatalog() = runTest {
        val useCase: GetAvailableEventsUseCase =
            GetAvailableEventsUseCaseImpl(FakeEventRepository())

        assertTrue(useCase().isEmpty())
    }

    private fun event(id: String) = Event(
        id = id,
        name = id,
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = 3_000L,
    )
}

