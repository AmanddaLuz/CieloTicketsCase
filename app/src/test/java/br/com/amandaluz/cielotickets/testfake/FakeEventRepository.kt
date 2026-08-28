package br.com.amandaluz.cielotickets.testfake

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.repository.EventRepository

class FakeEventRepository(
    events: List<Event> = emptyList(),
) : EventRepository {
    private val eventsById = events.associateBy(Event::id)

    override suspend fun getAvailableEvents(): List<Event> =
        eventsById.values.toList()

    override suspend fun findById(id: String): Event? = eventsById[id]
}
