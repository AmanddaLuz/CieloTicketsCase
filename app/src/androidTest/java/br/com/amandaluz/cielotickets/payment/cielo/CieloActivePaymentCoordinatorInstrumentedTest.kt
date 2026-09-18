package br.com.amandaluz.cielotickets.payment.cielo

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieloActivePaymentCoordinatorInstrumentedTest {

    @Test
    fun expiresPreviousProcessingAttemptAndActivatesNewPurchase() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val database = AppDatabase.getInstance(application)
        val repository = application.appContainer.purchaseRepository
        val store = CieloActivePaymentStoreImpl(application)
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        store.currentReference()?.let(store::clear)

        try {
            repository.insert(attempt(PREVIOUS_REFERENCE))
            repository.insert(attempt(NEW_REFERENCE))
            assertTrue(store.claim(PREVIOUS_REFERENCE))
            val coordinator = CieloActivePaymentCoordinatorImpl(
                activePaymentStore = store,
                updatePurchaseStatus = application.appContainer.updatePurchaseStatus,
            )

            assertTrue(coordinator.activate(NEW_REFERENCE))

            assertEquals(
                PaymentStatus.TIMED_OUT,
                repository.findByReference(PREVIOUS_REFERENCE)?.status,
            )
            assertEquals(
                PaymentStatus.PROCESSING,
                repository.findByReference(NEW_REFERENCE)?.status,
            )
            assertEquals(NEW_REFERENCE, store.currentReference())
        } finally {
            store.currentReference()?.let(store::clear)
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    private fun attempt(reference: String) = PurchaseAttempt.restore(
        reference = reference,
        items = listOf(
            PurchaseItem(
                eventId = "event-$reference",
                eventName = "Festival",
                quantity = 1,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = PaymentStatus.PROCESSING,
        paymentMethod = PaymentMethod.CREDIT_CASH,
        createdAt = 100L,
        updatedAt = 200L,
    )

    private companion object {
        const val PREVIOUS_REFERENCE = "previous-processing"
        const val NEW_REFERENCE = "new-processing"
    }
}
