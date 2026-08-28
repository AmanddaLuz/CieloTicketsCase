package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.usecase.impl.BuildCartUseCaseImpl
import br.com.amandaluz.cielotickets.testfake.FakeEventRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildCartUseCaseImplTest {

    private val events = listOf(
        event("event-1", 1_999L),
        event("event-2", 5_000L),
    )
    private val useCase: BuildCartUseCase =
        BuildCartUseCaseImpl(FakeEventRepository(events))

    @Test
    fun buildsMultiEventCartWithExactTotal() = runTest {
        val result = useCase(mapOf("event-1" to 3, "event-2" to 2))

        assertTrue(result is BuildCartUseCase.Result.Success)
        val cart = (result as BuildCartUseCase.Result.Success).cart
        assertEquals(5, cart.totalQuantity)
        assertEquals(15_997L, cart.totalInCents)
    }

    @Test
    fun rejectsEmptyCart() = runTest {
        assertTrue(useCase(emptyMap()) is BuildCartUseCase.Result.EmptyCart)
    }

    @Test
    fun rejectsNonPositiveQuantity() = runTest {
        val result = useCase(mapOf("event-1" to 0))

        assertEquals(
            BuildCartUseCase.Result.NonPositiveQuantity("event-1"),
            result,
        )
    }

    @Test
    fun rejectsQuantityAboveEventLimit() = runTest {
        val result = useCase(mapOf("event-1" to 6))

        assertEquals(
            BuildCartUseCase.Result.QuantityLimitExceeded("event-1", maximum = 5),
            result,
        )
    }

    @Test
    fun identifiesUnknownEvent() = runTest {
        assertEquals(
            BuildCartUseCase.Result.EventNotFound("missing"),
            useCase(mapOf("missing" to 1)),
        )
    }

    @Test
    fun returnsDeterministicFirstFailure() = runTest {
        val result = useCase(
            hashMapOf(
                "z-missing" to 1,
                "a-missing" to 1,
            ),
        )

        assertEquals(BuildCartUseCase.Result.EventNotFound("a-missing"), result)
    }

    @Test
    fun reportsMonetaryOverflow() = runTest {
        val overflowEvent = event("overflow", Long.MAX_VALUE)
        val overflowUseCase = BuildCartUseCaseImpl(
            FakeEventRepository(listOf(overflowEvent)),
        )

        assertEquals(
            BuildCartUseCase.Result.AmountOverflow,
            overflowUseCase(mapOf("overflow" to 2)),
        )
    }

    private fun event(id: String, price: Long) = Event(
        id = id,
        name = id,
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = price,
        maxTicketsPerPurchase = 5,
    )
}
