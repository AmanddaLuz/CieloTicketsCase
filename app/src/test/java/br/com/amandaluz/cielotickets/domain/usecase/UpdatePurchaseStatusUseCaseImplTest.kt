package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import br.com.amandaluz.cielotickets.testfake.FakePurchaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePurchaseStatusUseCaseImplTest {

    @Test
    fun movesCreatedAttemptToProcessing() = runTest {
        val repository = FakePurchaseRepository(listOf(attempt()))
        val useCase = UpdatePurchaseStatusUseCaseImpl(
            purchaseRepository = repository,
            currentTimeMillis = { 200L },
        )

        val result = useCase("reference-1", PaymentStatus.PROCESSING)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.Updated(
                "reference-1",
                PaymentStatus.PROCESSING,
            ),
            result,
        )
        assertEquals(200L, repository.findByReference("reference-1")?.updatedAt)
    }

    @Test
    fun acceptsEveryTerminalResultFromProcessing() = runTest {
        PaymentStatus.entries.filter(PaymentStatus::isTerminal).forEach { terminalStatus ->
            val repository = FakePurchaseRepository(
                listOf(attempt(status = PaymentStatus.PROCESSING)),
            )
            val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

            assertEquals(
                UpdatePurchaseStatusUseCase.Result.Updated("reference-1", terminalStatus),
                useCase("reference-1", terminalStatus),
            )
        }
    }

    @Test
    fun repeatedCallbackIsIdempotent() = runTest {
        val repository = FakePurchaseRepository(
            listOf(attempt(status = PaymentStatus.APPROVED)),
        )
        val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.Unchanged(
                "reference-1",
                PaymentStatus.APPROVED,
            ),
            useCase("reference-1", PaymentStatus.APPROVED),
        )
    }

    @Test
    fun rejectsUnknownReference() = runTest {
        val result = UpdatePurchaseStatusUseCaseImpl(FakePurchaseRepository())(
            "missing",
            PaymentStatus.PROCESSING,
        )

        assertEquals(UpdatePurchaseStatusUseCase.Result.NotFound("missing"), result)
    }

    @Test
    fun rejectsInvalidTransition() = runTest {
        val repository = FakePurchaseRepository(
            listOf(attempt(status = PaymentStatus.APPROVED)),
        )
        val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                reference = "reference-1",
                currentStatus = PaymentStatus.APPROVED,
                requestedStatus = PaymentStatus.ERROR,
            ),
            useCase("reference-1", PaymentStatus.ERROR),
        )
    }

    @Test
    fun reportsConcurrentRemovalDuringUpdate() = runTest {
        val repository = FakePurchaseRepository(listOf(attempt())).apply {
            nextStatusUpdateResult = PurchaseRepository.StatusUpdateResult.NotFound
        }
        val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.NotFound("reference-1"),
            useCase("reference-1", PaymentStatus.PROCESSING),
        )
    }

    @Test
    fun concurrentDuplicateCallbackIsUnchanged() = runTest {
        val repository = FakePurchaseRepository(
            listOf(attempt(status = PaymentStatus.PROCESSING)),
        ).apply {
            nextStatusUpdateResult =
                PurchaseRepository.StatusUpdateResult.StatusMismatch(PaymentStatus.APPROVED)
        }
        val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.Unchanged(
                "reference-1",
                PaymentStatus.APPROVED,
            ),
            useCase("reference-1", PaymentStatus.APPROVED),
        )
    }

    @Test
    fun concurrentDifferentTerminalCallbackIsRejected() = runTest {
        val repository = FakePurchaseRepository(
            listOf(attempt(status = PaymentStatus.PROCESSING)),
        ).apply {
            nextStatusUpdateResult =
                PurchaseRepository.StatusUpdateResult.StatusMismatch(PaymentStatus.APPROVED)
        }
        val useCase = UpdatePurchaseStatusUseCaseImpl(repository)

        assertEquals(
            UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                reference = "reference-1",
                currentStatus = PaymentStatus.APPROVED,
                requestedStatus = PaymentStatus.DENIED,
            ),
            useCase("reference-1", PaymentStatus.DENIED),
        )
    }

    private fun attempt(
        status: PaymentStatus = PaymentStatus.CREATED,
    ) = PurchaseAttempt.restore(
        reference = "reference-1",
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 2,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = status,
        createdAt = 100,
        updatedAt = 100,
    )
}
