package br.com.amandaluz.cielotickets.feature.events.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.repository.EventRepository

class BuildCartUseCaseImpl(
    private val eventRepository: EventRepository,
) : BuildCartUseCase {

    override suspend fun invoke(selections: Map<String, Int>): BuildCartUseCase.Result {
        if (selections.isEmpty()) {
            return BuildCartUseCase.Result.EmptyCart
        }

        val resolutions = selections.toSortedMap().map { (eventId, quantity) ->
            resolveItem(eventId, quantity)
        }
        val failure = resolutions
            .filterIsInstance<ItemResolution.Failure>()
            .firstOrNull()

        return failure?.result ?: buildCart(resolutions)
    }

    @Suppress("SwallowedException")
    private fun buildCart(
        resolutions: List<ItemResolution>,
    ): BuildCartUseCase.Result = try {
        BuildCartUseCase.Result.Success(
            Cart(resolutions.filterIsInstance<ItemResolution.Success>().map { it.item }),
        )
    } catch (_: ArithmeticException) {
        BuildCartUseCase.Result.AmountOverflow
    }

    private suspend fun resolveItem(
        eventId: String,
        quantity: Int,
    ): ItemResolution {
        val event = if (quantity > 0) eventRepository.findById(eventId) else null
        return when {
            quantity <= 0 -> ItemResolution.Failure(
                BuildCartUseCase.Result.NonPositiveQuantity(eventId),
            )
            event == null -> ItemResolution.Failure(
                BuildCartUseCase.Result.EventNotFound(eventId),
            )
            quantity > event.maxTicketsPerPurchase -> ItemResolution.Failure(
                BuildCartUseCase.Result.QuantityLimitExceeded(
                    eventId = eventId,
                    maximum = event.maxTicketsPerPurchase,
                ),
            )
            else -> createCartItem(event, quantity)
        }
    }

    @Suppress("SwallowedException")
    private fun createCartItem(
        event: Event,
        quantity: Int,
    ): ItemResolution = try {
        ItemResolution.Success(CartItem(event, quantity))
    } catch (_: ArithmeticException) {
        ItemResolution.Failure(BuildCartUseCase.Result.AmountOverflow)
    }

    private sealed interface ItemResolution {
        data class Success(val item: CartItem) : ItemResolution
        data class Failure(val result: BuildCartUseCase.Result) : ItemResolution
    }
}
