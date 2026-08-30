package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCaseImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildTicketQrContentUseCaseImplTest {
    private val useCase = BuildTicketQrContentUseCaseImpl()

    @Test
    fun buildsOpaqueReferenceOnlyForApprovedPurchase() {
        val approved = attempt(PaymentStatus.APPROVED)

        assertEquals("CIELO_TICKET|ticket-reference", useCase(approved))
    }

    @Test
    fun doesNotBuildQrContentForNonApprovedPurchase() {
        PaymentStatus.entries
            .filterNot { it == PaymentStatus.APPROVED }
            .forEach { status ->
                assertNull(useCase(attempt(status)))
            }
    }

    private fun attempt(status: PaymentStatus): PurchaseAttempt {
        val event = Event(
            id = "event-1",
            name = "Festival",
            venue = "Praça",
            date = "12 set",
            priceInCents = 3_500L,
            maxTicketsPerPurchase = 4,
        )
        val created = PurchaseAttempt.create(
            reference = "ticket-reference",
            cart = Cart(listOf(CartItem(event, 2))),
            paymentMethod = PaymentMethod.CREDIT_CASH,
            createdAt = 100L,
        )
        return PurchaseAttempt.restore(
            reference = created.reference,
            items = created.items,
            status = status,
            paymentMethod = created.paymentMethod,
            createdAt = created.createdAt,
            updatedAt = 200L,
        )
    }
}
