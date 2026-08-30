package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatePurchaseAttemptUseCaseImplTest {

    @Test
    fun createsDeterministicPurchaseSnapshot() {
        val useCase: CreatePurchaseAttemptUseCase = CreatePurchaseAttemptUseCaseImpl(
            generateReference = { "reference-1" },
            currentTimeMillis = { 100L },
        )
        val cart = Cart(
            listOf(
                CartItem(event("event-1", 3_000L), quantity = 2),
                CartItem(event("event-2", 5_000L), quantity = 1),
            ),
        )

        val attempt = useCase(cart, PaymentMethod.DEBIT_CASH)

        assertEquals("reference-1", attempt.reference)
        assertEquals(PaymentStatus.CREATED, attempt.status)
        assertEquals(PaymentMethod.DEBIT_CASH, attempt.paymentMethod)
        assertEquals(100L, attempt.createdAt)
        assertEquals(100L, attempt.updatedAt)
        assertEquals(3, attempt.totalQuantity)
        assertEquals(11_000L, attempt.totalInCents)
    }

    private fun event(id: String, price: Long) = Event(
        id = id,
        name = id,
        venue = "Arena",
        date = "2026-10-10",
        priceInCents = price,
    )
}

