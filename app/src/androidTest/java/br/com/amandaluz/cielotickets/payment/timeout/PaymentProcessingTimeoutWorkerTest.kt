package br.com.amandaluz.cielotickets.payment.timeout

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResult
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.payment.cielo.CieloActivePaymentStoreImpl
import br.com.amandaluz.cielotickets.payment.cielo.CieloPaymentResultObserverImpl
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentProcessingTimeoutWorkerTest {

    @Test
    fun expiresProcessingAttemptAndNotifiesActiveUi() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val database = AppDatabase.getInstance(application)
        val repository = application.appContainer.purchaseRepository
        val activePaymentStore = CieloActivePaymentStoreImpl(application)
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        activePaymentStore.currentReference()?.let(activePaymentStore::clear)
        val timeoutReceived = CountDownLatch(1)
        val observedResult = AtomicReference<PaymentResult>()
        val observer = CieloPaymentResultObserverImpl(application)
        observer.start {
            observedResult.set(it)
            timeoutReceived.countDown()
        }

        try {
            assertEquals(
                PurchaseRepository.InsertResult.Inserted,
                repository.insert(processingAttempt()),
            )
            assertTrue(activePaymentStore.claim(REFERENCE))
            val worker = timeoutWorker(application)

            assertEquals(ListenableWorker.Result.success(), worker.doWork())
            assertEquals(
                PaymentStatus.TIMED_OUT,
                repository.findByReference(REFERENCE)?.status,
            )
            assertTrue(timeoutReceived.await(3L, TimeUnit.SECONDS))
            assertEquals(REFERENCE, observedResult.get()?.reference)
            assertEquals(PaymentStatus.TIMED_OUT, observedResult.get()?.status)
            assertEquals(REFERENCE, activePaymentStore.currentReference())

            application.appContainer.updatePurchaseStatus(
                REFERENCE,
                PaymentStatus.APPROVED,
            )
            assertEquals(
                PaymentStatus.APPROVED,
                repository.findByReference(REFERENCE)?.status,
            )
        } finally {
            observer.stop()
            activePaymentStore.clear(REFERENCE)
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    @Test
    fun doesNotReplaceCallbackThatCompletedBeforeTimeout() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val database = AppDatabase.getInstance(application)
        val repository = application.appContainer.purchaseRepository
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }

        try {
            assertEquals(
                PurchaseRepository.InsertResult.Inserted,
                repository.insert(
                    processingAttempt().withStatus(
                        PaymentStatus.APPROVED,
                        updatedAt = 300L,
                    ),
                ),
            )

            assertEquals(
                ListenableWorker.Result.success(),
                timeoutWorker(application).doWork(),
            )
            assertEquals(
                PaymentStatus.APPROVED,
                repository.findByReference(REFERENCE)?.status,
            )
        } finally {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    @Test
    fun schedulesUniqueWorkForOneMinute() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val workManager = WorkManager.getInstance(application)
        val workName = PaymentProcessingTimeoutSchedulerImpl.workName(
            SCHEDULE_REFERENCE,
        )
        workManager.cancelUniqueWork(workName).result.get(3L, TimeUnit.SECONDS)
        val beforeSchedule = System.currentTimeMillis()

        try {
            PaymentProcessingTimeoutSchedulerImpl(application)
                .schedule(SCHEDULE_REFERENCE)
            val afterSchedule = System.currentTimeMillis()
            val workInfo = workManager.getWorkInfosForUniqueWork(workName)
                .get(3L, TimeUnit.SECONDS)
                .single()
            val delayMillis = TimeUnit.MINUTES.toMillis(
                PaymentProcessingTimeoutSchedulerImpl.TIMEOUT_MINUTES,
            )

            assertTrue(workInfo.nextScheduleTimeMillis >= beforeSchedule + delayMillis)
            assertTrue(
                workInfo.nextScheduleTimeMillis <=
                    afterSchedule + delayMillis + SCHEDULE_TOLERANCE_MILLIS,
            )
        } finally {
            workManager.cancelUniqueWork(workName)
                .result
                .get(3L, TimeUnit.SECONDS)
        }
    }

    private fun timeoutWorker(
        application: CieloTicketsApplication,
    ): PaymentProcessingTimeoutWorker =
        TestListenableWorkerBuilder<PaymentProcessingTimeoutWorker>(
            context = application,
            inputData = Data.Builder()
                .putString(
                    PaymentProcessingTimeoutWorker.KEY_REFERENCE,
                    REFERENCE,
                )
                .build(),
        ).build()

    private fun processingAttempt() = PurchaseAttempt.restore(
        reference = REFERENCE,
        items = listOf(
            PurchaseItem(
                eventId = "timeout-event",
                eventName = "Timeout Event",
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
        const val REFERENCE = "processing-timeout-reference"
        const val SCHEDULE_REFERENCE = "processing-timeout-schedule"
        const val SCHEDULE_TOLERANCE_MILLIS = 5_000L
    }
}
