package br.com.amandaluz.cielotickets.payment.cielo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.payment.cielo.encoder.CieloPaymentRequestEncoderImpl
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieloResponseActivityTest {

    @Test
    fun broadcastsParsedCieloCallback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receivedIntent = AtomicReference<Intent>()
        val callbackReceived = CountDownLatch(1)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                receivedIntent.set(intent)
                callbackReceived.countDown()
            }
        }
        val filter = IntentFilter(CieloResponseActivity.ACTION_PAYMENT_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED,
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        try {
            val scenario = launchCallback(context, "reference-activity")
            try {
                assertTrue(callbackReceived.await(3L, TimeUnit.SECONDS))
                assertEquals(
                    "reference-activity",
                    receivedIntent.get().getStringExtra(
                        CieloResponseActivity.EXTRA_REFERENCE,
                    ),
                )
                assertEquals(
                    PaymentStatus.APPROVED.name,
                    receivedIntent.get().getStringExtra(
                        CieloResponseActivity.EXTRA_STATUS,
                    ),
                )
            } finally {
                scenario.close()
            }
        } finally {
            context.unregisterReceiver(receiver)
        }
    }

    @Test
    fun persistsReferencedCallbackWithoutActiveObserver() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val database = AppDatabase.getInstance(application)
        val repository = application.appContainer.purchaseRepository
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }

        try {
            val attempt = PurchaseAttempt.restore(
                reference = DURABLE_REFERENCE,
                items = listOf(
                    PurchaseItem(
                        eventId = "event-1",
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
            assertEquals(
                PurchaseRepository.InsertResult.Inserted,
                repository.insert(attempt),
            )

            val scenario = launchCallback(application, DURABLE_REFERENCE)
            try {
                val persistedStatus = awaitStatus(repository, DURABLE_REFERENCE)
                assertEquals(PaymentStatus.APPROVED, persistedStatus)
            } finally {
                scenario.close()
            }
        } finally {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    @Test
    fun correlatesReferenceLessErrorWithPersistedActivePayment() = runBlocking {
        val application =
            ApplicationProvider.getApplicationContext<CieloTicketsApplication>()
        val database = AppDatabase.getInstance(application)
        val repository = application.appContainer.purchaseRepository
        val activePaymentStore = CieloActivePaymentStoreImpl(application)
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        activePaymentStore.currentReference()?.let(activePaymentStore::clear)

        try {
            val attempt = PurchaseAttempt.restore(
                reference = REFERENCE_LESS_ERROR_REFERENCE,
                items = listOf(
                    PurchaseItem(
                        eventId = "event-error",
                        eventName = "Festival",
                        quantity = 1,
                        unitPriceInCents = 3_000L,
                    ),
                ),
                status = PaymentStatus.PROCESSING,
                paymentMethod = PaymentMethod.DEBIT_CASH,
                createdAt = 100L,
                updatedAt = 200L,
            )
            assertEquals(
                PurchaseRepository.InsertResult.Inserted,
                repository.insert(attempt),
            )
            assertTrue(activePaymentStore.claim(REFERENCE_LESS_ERROR_REFERENCE))

            val scenario = launchRawCallback(
                application,
                """{"code":3,"reason":"Pagamento negado"}""",
            )
            try {
                assertEquals(
                    PaymentStatus.DENIED,
                    awaitStatus(repository, REFERENCE_LESS_ERROR_REFERENCE),
                )
                assertEquals(null, activePaymentStore.currentReference())
                onView(withText(R.string.payment_result_title))
                    .check(matches(isDisplayed()))
                onView(withText(R.string.status_denied))
                    .check(matches(isDisplayed()))
                Unit
            } finally {
                scenario.close()
            }
        } finally {
            activePaymentStore.clear(REFERENCE_LESS_ERROR_REFERENCE)
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    private fun launchCallback(
        context: Context,
        reference: String,
    ): ActivityScenario<CieloResponseActivity> = launchRawCallback(
        context = context,
        rawResponse = """{"reference":"$reference","id":"order-1"}""",
    )

    private fun launchRawCallback(
        context: Context,
        rawResponse: String,
    ): ActivityScenario<CieloResponseActivity> {
        val encodedResponse = Base64.encodeToString(
            rawResponse.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP,
        )
        val callbackUri = Uri.Builder()
            .scheme(CieloPaymentRequestEncoderImpl.CALLBACK_SCHEME)
            .authority(CieloPaymentRequestEncoderImpl.CALLBACK_HOST)
            .appendQueryParameter(
                CieloPaymentRequestEncoderImpl.RESPONSE_PARAMETER,
                encodedResponse,
            )
            .build()
        val activityIntent = Intent(context, CieloResponseActivity::class.java).apply {
            data = callbackUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return ActivityScenario.launch(activityIntent)
    }

    private suspend fun awaitStatus(
        repository: PurchaseRepository,
        reference: String,
    ): PaymentStatus? {
        repeat(MAX_STATUS_POLLS) {
            val status = repository.findByReference(reference)?.status
            if (status?.isTerminal == true) {
                return status
            }
            delay(STATUS_POLL_INTERVAL_MILLIS)
        }
        return repository.findByReference(reference)?.status
    }

    private companion object {
        const val DURABLE_REFERENCE = "reference-durable"
        const val REFERENCE_LESS_ERROR_REFERENCE = "reference-less-error"
        const val MAX_STATUS_POLLS = 50
        const val STATUS_POLL_INTERVAL_MILLIS = 100L
    }
}
