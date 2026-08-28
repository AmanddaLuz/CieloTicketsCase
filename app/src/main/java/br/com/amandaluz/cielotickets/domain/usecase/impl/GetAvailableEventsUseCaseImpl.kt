package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.repository.EventRepository
import br.com.amandaluz.cielotickets.domain.usecase.GetAvailableEventsUseCase

class GetAvailableEventsUseCaseImpl(
    private val eventRepository: EventRepository,
) : GetAvailableEventsUseCase {
    override suspend fun invoke(): List<Event> =
        eventRepository.getAvailableEvents()
}

