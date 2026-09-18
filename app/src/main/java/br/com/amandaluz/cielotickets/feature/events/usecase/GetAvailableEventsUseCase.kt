package br.com.amandaluz.cielotickets.feature.events.usecase

import br.com.amandaluz.cielotickets.domain.model.Event

fun interface GetAvailableEventsUseCase {
    suspend operator fun invoke(): List<Event>
}
