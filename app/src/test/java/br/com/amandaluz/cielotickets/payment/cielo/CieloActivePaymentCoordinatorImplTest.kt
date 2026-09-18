package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CieloActivePaymentCoordinatorImplTest {

    @Test
    fun expiresPreviousProcessingAttemptBeforeActivatingNewReference() = runTest {
        val store = FakeActivePaymentStore(PREVIOUS_REFERENCE)
        val updateStatus = FakeUpdateStatusUseCase(
            UpdatePurchaseStatusUseCase.Result.Updated(
                PREVIOUS_REFERENCE,
                PaymentStatus.TIMED_OUT,
            ),
        )
        val coordinator = CieloActivePaymentCoordinatorImpl(store, updateStatus)

        assertTrue(coordinator.activate(NEW_REFERENCE))
        assertEquals(
            listOf(PREVIOUS_REFERENCE to PaymentStatus.TIMED_OUT),
            updateStatus.requests,
        )
        assertEquals(NEW_REFERENCE, store.currentReference())
    }

    @Test
    fun replacesStaleCorrelationWhenPreviousAttemptIsAlreadyTerminal() = runTest {
        val store = FakeActivePaymentStore(PREVIOUS_REFERENCE)
        val coordinator = CieloActivePaymentCoordinatorImpl(
            activePaymentStore = store,
            updatePurchaseStatus = FakeUpdateStatusUseCase(
                UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                    reference = PREVIOUS_REFERENCE,
                    currentStatus = PaymentStatus.APPROVED,
                    requestedStatus = PaymentStatus.TIMED_OUT,
                ),
            ),
        )

        assertTrue(coordinator.activate(NEW_REFERENCE))
        assertEquals(NEW_REFERENCE, store.currentReference())
    }

    @Test
    fun keepsPreviousCorrelationWhenItCannotBeSafelyExpired() = runTest {
        val store = FakeActivePaymentStore(PREVIOUS_REFERENCE)
        val coordinator = CieloActivePaymentCoordinatorImpl(
            activePaymentStore = store,
            updatePurchaseStatus = FakeUpdateStatusUseCase(
                UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                    reference = PREVIOUS_REFERENCE,
                    currentStatus = PaymentStatus.CREATED,
                    requestedStatus = PaymentStatus.TIMED_OUT,
                ),
            ),
        )

        assertFalse(coordinator.activate(NEW_REFERENCE))
        assertEquals(PREVIOUS_REFERENCE, store.currentReference())
    }

    private class FakeActivePaymentStore(
        private var reference: String?,
    ) : CieloActivePaymentStore {
        override fun claim(reference: String): Boolean {
            this.reference = reference
            return true
        }

        override fun currentReference(): String? = reference

        override fun replace(
            expectedReference: String,
            newReference: String,
        ): Boolean {
            if (reference != expectedReference) {
                return false
            }
            reference = newReference
            return true
        }

        override fun clear(reference: String) {
            if (this.reference == reference) {
                this.reference = null
            }
        }
    }

    private class FakeUpdateStatusUseCase(
        private val result: UpdatePurchaseStatusUseCase.Result,
    ) : UpdatePurchaseStatusUseCase {
        val requests = mutableListOf<Pair<String, PaymentStatus>>()

        override suspend fun invoke(
            reference: String,
            newStatus: PaymentStatus,
        ): UpdatePurchaseStatusUseCase.Result {
            requests += reference to newStatus
            return result
        }
    }

    private companion object {
        const val PREVIOUS_REFERENCE = "previous-reference"
        const val NEW_REFERENCE = "new-reference"
    }
}
