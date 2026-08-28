package br.com.amandaluz.cielotickets.domain.model

data class Event(
    val id: String,
    val name: String,
    val venue: String,
    val date: String,
    val priceInCents: Long,
    val maxTicketsPerPurchase: Int = DEFAULT_MAX_TICKETS,
) {
    init {
        require(id.isNotBlank()) { "Event id must not be blank" }
        require(name.isNotBlank()) { "Event name must not be blank" }
        require(venue.isNotBlank()) { "Event venue must not be blank" }
        require(date.isNotBlank()) { "Event date must not be blank" }
        require(priceInCents > 0) { "Event price must be positive" }
        require(maxTicketsPerPurchase > 0) {
            "Maximum tickets per purchase must be positive"
        }
    }

    private companion object {
        const val DEFAULT_MAX_TICKETS = 10
    }
}

