package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.receipt.usecase.GetPurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.testfake.FakePurchaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPurchaseAttemptUseCaseImplTest {
    @Test
    fun returnsPersistedAttemptByReference() = runTest {
        val attempt = attempt()
        val useCase = GetPurchaseAttemptUseCaseImpl(
            FakePurchaseRepository(listOf(attempt)),
        )

        assertEquals(attempt, useCase(attempt.reference))
    }

    private fun attempt(): PurchaseAttempt {
        val event = Event(
            id = "event-1",
            name = "Festival",
            venue = "Praça",
            date = "12 set",
            priceInCents = 3_500L,
            maxTicketsPerPurchase = 4,
        )
        return PurchaseAttempt.create(
            reference = "history-reference",
            cart = Cart(listOf(CartItem(event, 1))),
            paymentMethod = PaymentMethod.CREDIT_CASH,
            createdAt = 100L,
        )
    }
}
