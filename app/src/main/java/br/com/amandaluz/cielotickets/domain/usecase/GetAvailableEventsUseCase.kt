package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Event

interface GetAvailableEventsUseCase {
    suspend operator fun invoke(): List<Event>
}

