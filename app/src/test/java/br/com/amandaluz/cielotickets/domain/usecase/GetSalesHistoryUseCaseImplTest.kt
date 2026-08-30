package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.feature.history.usecase.GetSalesHistoryUseCaseImpl
import br.com.amandaluz.cielotickets.feature.history.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.testfake.FakePurchaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSalesHistoryUseCaseImplTest {

    @Test
    fun exposesRepositoryHistoryNewestFirst() = runTest {
        val older = attempt("older", createdAt = 1)
        val newer = attempt("newer", createdAt = 2)
        val useCase: GetSalesHistoryUseCase = GetSalesHistoryUseCaseImpl(
            FakePurchaseRepository(listOf(older, newer)),
        )

        assertEquals(listOf(newer, older), useCase().first())
    }

    @Test
    fun exposesEmptyHistory() = runTest {
        val useCase: GetSalesHistoryUseCase =
            GetSalesHistoryUseCaseImpl(FakePurchaseRepository())

        assertTrue(useCase().first().isEmpty())
    }

    private fun attempt(reference: String, createdAt: Long) = PurchaseAttempt.restore(
        reference = reference,
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 1,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = PaymentStatus.CREATED,
        paymentMethod = PaymentMethod.CREDIT_CASH,
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}
