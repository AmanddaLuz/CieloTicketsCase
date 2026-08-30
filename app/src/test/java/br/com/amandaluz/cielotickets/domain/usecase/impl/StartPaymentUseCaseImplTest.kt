package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.gateway.PaymentGateway
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StartPaymentUseCaseImplTest {

    @Test
    fun transitionsToProcessingBeforeLaunchingGateway() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.Initiated)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.Updated(
                REFERENCE,
                PaymentStatus.PROCESSING,
            ),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)
        val attempt = attempt()

        assertEquals(
            StartPaymentUseCase.Result.Started(REFERENCE),
            useCase(attempt),
        )
        assertEquals(listOf(PaymentStatus.PROCESSING), updateStatus.requestedStatuses)
        assertEquals(attempt, gateway.launchedAttempt)
    }

    @Test
    fun doesNotLaunchDuplicatePaymentWhenAlreadyProcessing() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.Initiated)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.Unchanged(
                REFERENCE,
                PaymentStatus.PROCESSING,
            ),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)

        assertEquals(
            StartPaymentUseCase.Result.AlreadyProcessing(REFERENCE),
            useCase(attempt()),
        )
        assertNull(gateway.launchedAttempt)
    }

    @Test
    fun marksAttemptAsErrorWhenCieloAppIsUnavailable() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.AppNotAvailable)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.Updated(
                REFERENCE,
                PaymentStatus.PROCESSING,
            ),
            UpdatePurchaseStatusUseCase.Result.Updated(
                REFERENCE,
                PaymentStatus.ERROR,
            ),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)

        assertEquals(
            StartPaymentUseCase.Result.AppNotAvailable(REFERENCE),
            useCase(attempt()),
        )
        assertEquals(
            listOf(PaymentStatus.PROCESSING, PaymentStatus.ERROR),
            updateStatus.requestedStatuses,
        )
    }

    @Test
    fun reportsMissingAttemptWithoutLaunchingGateway() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.Initiated)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.NotFound(REFERENCE),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)

        assertEquals(
            StartPaymentUseCase.Result.NotFound(REFERENCE),
            useCase(attempt()),
        )
        assertNull(gateway.launchedAttempt)
    }

    @Test
    fun reportsTerminalAttemptWithoutLaunchingGateway() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.Initiated)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                reference = REFERENCE,
                currentStatus = PaymentStatus.APPROVED,
                requestedStatus = PaymentStatus.PROCESSING,
            ),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)

        assertEquals(
            StartPaymentUseCase.Result.InvalidStatus(
                REFERENCE,
                PaymentStatus.APPROVED,
            ),
            useCase(attempt()),
        )
        assertNull(gateway.launchedAttempt)
    }

    @Test
    fun marksAttemptAsErrorAfterTechnicalGatewayFailure() = runTest {
        val gateway = FakePaymentGateway(PaymentGateway.Result.TechnicalFailure)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.Updated(
                REFERENCE,
                PaymentStatus.PROCESSING,
            ),
            UpdatePurchaseStatusUseCase.Result.Updated(
                REFERENCE,
                PaymentStatus.ERROR,
            ),
        )
        val useCase = StartPaymentUseCaseImpl(gateway, updateStatus)

        assertEquals(
            StartPaymentUseCase.Result.TechnicalFailure(REFERENCE),
            useCase(attempt()),
        )
        assertEquals(
            listOf(PaymentStatus.PROCESSING, PaymentStatus.ERROR),
            updateStatus.requestedStatuses,
        )
    }

    private fun attempt() = PurchaseAttempt.restore(
        reference = REFERENCE,
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 2,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = PaymentStatus.CREATED,
        createdAt = 100L,
        updatedAt = 100L,
    )

    private class FakePaymentGateway(
        private val result: PaymentGateway.Result,
    ) : PaymentGateway {
        var launchedAttempt: PurchaseAttempt? = null

        override fun initiatePayment(attempt: PurchaseAttempt): PaymentGateway.Result {
            launchedAttempt = attempt
            return result
        }
    }

    private class FakeUpdateStatusUseCase(
        vararg results: UpdatePurchaseStatusUseCase.Result,
    ) : UpdatePurchaseStatusUseCase {
        private val pendingResults = ArrayDeque(results.toList())
        val requestedStatuses = mutableListOf<PaymentStatus>()

        override suspend fun invoke(
            reference: String,
            newStatus: PaymentStatus,
        ): UpdatePurchaseStatusUseCase.Result {
            assertEquals(REFERENCE, reference)
            requestedStatuses += newStatus
            return pendingResults.removeFirst()
        }
    }

    private companion object {
        const val REFERENCE = "reference-1"
    }
}
