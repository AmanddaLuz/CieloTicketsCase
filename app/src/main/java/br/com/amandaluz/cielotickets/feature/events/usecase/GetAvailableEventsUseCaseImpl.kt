package br.com.amandaluz.cielotickets.feature.events.usecase

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.repository.EventRepository

class GetAvailableEventsUseCaseImpl(
    private val eventRepository: EventRepository,
) : GetAvailableEventsUseCase {
    override suspend fun invoke(): List<Event> =
        eventRepository.getAvailableEvents()
}

