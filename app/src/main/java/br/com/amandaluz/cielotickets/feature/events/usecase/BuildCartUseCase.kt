package br.com.amandaluz.cielotickets.feature.events.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart

/**
 * Constrói um carrinho somente quando todas as seleções respeitam o catálogo,
 * os limites de quantidade e a aritmética monetária exata.
 */
interface BuildCartUseCase {
    sealed interface Result {
        data class Success(val cart: Cart) : Result
        data object EmptyCart : Result
        data class EventNotFound(val eventId: String) : Result
        data class NonPositiveQuantity(val eventId: String) : Result
        data class QuantityLimitExceeded(
            val eventId: String,
            val maximum: Int,
        ) : Result
        data object AmountOverflow : Result
    }

    suspend operator fun invoke(selections: Map<String, Int>): Result
}
