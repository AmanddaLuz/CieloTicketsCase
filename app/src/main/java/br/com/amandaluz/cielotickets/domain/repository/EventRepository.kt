package br.com.amandaluz.cielotickets.domain.repository

import br.com.amandaluz.cielotickets.domain.model.Event

interface EventRepository {
    suspend fun getAvailableEvents(): List<Event>

    suspend fun findById(id: String): Event?
}

