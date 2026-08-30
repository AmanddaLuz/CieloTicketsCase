package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.testfake.FakePurchaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SavePurchaseAttemptUseCaseImplTest {

    private val repository = FakePurchaseRepository()
    private val useCase: SavePurchaseAttemptUseCase =
        SavePurchaseAttemptUseCaseImpl(repository)

    @Test
    fun insertsCreatedAttempt() = runTest {
        val attempt = attempt()

        assertEquals(SavePurchaseAttemptUseCase.Result.Saved(attempt), useCase(attempt))
        assertEquals(attempt, repository.findByReference(attempt.reference))
    }

    @Test
    fun rejectsDuplicateReferenceWithoutOverwriting() = runTest {
        val original = attempt()
        useCase(original)
        val duplicate = PurchaseAttempt.restore(
            reference = original.reference,
            items = listOf(item(eventName = "Changed")),
            status = PaymentStatus.CREATED,
            paymentMethod = PaymentMethod.CREDIT_CASH,
            createdAt = original.createdAt,
            updatedAt = original.updatedAt,
        )

        val result = useCase(duplicate)

        assertEquals(
            SavePurchaseAttemptUseCase.Result.DuplicateReference(original.reference),
            result,
        )
        assertEquals(original, repository.findByReference(original.reference))
    }

    @Test
    fun rejectsAttemptThatDidNotStartAsCreated() = runTest {
        val original = attempt()
        val attempt = PurchaseAttempt.restore(
            reference = original.reference,
            items = original.items,
            status = PaymentStatus.PROCESSING,
            paymentMethod = PaymentMethod.CREDIT_CASH,
            createdAt = original.createdAt,
            updatedAt = original.updatedAt,
        )

        assertEquals(
            SavePurchaseAttemptUseCase.Result.InvalidInitialStatus(PaymentStatus.PROCESSING),
            useCase(attempt),
        )
    }

    private fun attempt() = PurchaseAttempt.restore(
        reference = "reference-1",
        items = listOf(item()),
        status = PaymentStatus.CREATED,
        paymentMethod = PaymentMethod.CREDIT_CASH,
        createdAt = 1,
        updatedAt = 1,
    )

    private fun item(eventName: String = "Festival") = PurchaseItem(
        eventId = "event-1",
        eventName = eventName,
        quantity = 2,
        unitPriceInCents = 3_000L,
    )
}
