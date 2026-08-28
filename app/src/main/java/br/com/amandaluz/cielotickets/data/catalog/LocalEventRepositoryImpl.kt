package br.com.amandaluz.cielotickets.data.catalog

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.repository.EventRepository

class LocalEventRepositoryImpl : EventRepository {
    private val eventsById = EVENTS.associateBy(Event::id)

    override suspend fun getAvailableEvents(): List<Event> =
        eventsById.values.toList()

    override suspend fun findById(id: String): Event? = eventsById[id]

    private companion object {
        val EVENTS = listOf(
            Event(
                id = "festival-gastronomico",
                name = "Festival Gastronômico",
                venue = "Praça Central",
                date = "12 set • 18:00",
                priceInCents = 3_500L,
            ),
            Event(
                id = "show-acustico",
                name = "Show Acústico",
                venue = "Teatro Municipal",
                date = "20 set • 20:30",
                priceInCents = 6_000L,
            ),
            Event(
                id = "feira-criativa",
                name = "Feira Criativa",
                venue = "Centro Cultural",
                date = "28 set • 10:00",
                priceInCents = 2_000L,
            ),
            Event(
                id = "festival-rock",
                name = "Festival de Rock",
                venue = "Arena Esportiva",
                date = "05 out • 19:00",
                priceInCents = 12_000L,
            ),
            Event(
                id = "espetaculo-danca",
                name = "Espetáculo de Dança",
                venue = "Teatro Municipal",
                date = "08 out • 20:00",
                priceInCents = 5_500L,
            ),
            Event(
                id = "expo-arte",
                name = "Exposição de Arte Moderna",
                venue = "Galeria Municipal",
                date = "10 out • 10:00",
                priceInCents = 1_500L,
            ),
            Event(
                id = "stand-up",
                name = "Stand-up Comedy",
                venue = "Casa de Shows",
                date = "14 out • 21:00",
                priceInCents = 8_000L,
            ),
            Event(
                id = "feira-livro",
                name = "Feira do Livro",
                venue = "Parque Municipal",
                date = "17 out • 09:00",
                priceInCents = 500L,
            ),
            Event(
                id = "concerto-classico",
                name = "Concerto Clássico",
                venue = "Sala de Concertos",
                date = "22 out • 19:30",
                priceInCents = 9_500L,
            ),
            Event(
                id = "festival-jazz",
                name = "Festival de Jazz",
                venue = "Anfiteatro do Parque",
                date = "28 out • 18:00",
                priceInCents = 7_000L,
            ),
        )
    }
}

