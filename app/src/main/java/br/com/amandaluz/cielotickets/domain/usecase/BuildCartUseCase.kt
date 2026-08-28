package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart

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
