package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.data.local.repository.RoomPurchaseRepositoryImpl
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieloResponseActivityTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: PurchaseRepository

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.getInstance(context)
        database.clearAllTables()
        repository = RoomPurchaseRepositoryImpl(database.purchaseAttemptDao())
        repository.insert(attempt())
        repository.compareAndSetStatus(
            reference = REFERENCE,
            expectedStatus = PaymentStatus.CREATED,
            newStatus = PaymentStatus.PROCESSING,
            updatedAt = 200L,
        )
    }

    @After
    fun tearDown() = runTest {
        database.clearAllTables()
    }

    @Test
    fun persistsCallbackWithoutAnActiveScreen() = runTest {
        val rawResponse =
            """
            {
              "id":"order-1",
              "reference":"$REFERENCE",
              "paidAmount":3000,
              "items":[{"sku":"event-1"}],
              "payments":[{"paymentFields":{"statusCode":"1"}}]
            }
            """.trimIndent()
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
            .appendQueryParameter(
                CieloPaymentRequestEncoderImpl.REFERENCE_PARAMETER,
                REFERENCE,
            )
            .build()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CieloResponseActivity::class.java).apply {
            data = callbackUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ActivityScenario.launch<CieloResponseActivity>(intent).use {
            withContext(Dispatchers.Default) {
                withTimeout(10_000L) {
                    while (
                        repository.findByReference(REFERENCE)?.status !=
                        PaymentStatus.APPROVED
                    ) {
                        delay(50L)
                    }
                }
            }
        }

        assertEquals(
            PaymentStatus.APPROVED,
            repository.findByReference(REFERENCE)?.status,
        )
    }

    private fun attempt() = PurchaseAttempt.restore(
        reference = REFERENCE,
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = "Festival",
                quantity = 1,
                unitPriceInCents = 3_000L,
            ),
        ),
        status = PaymentStatus.CREATED,
        createdAt = 100L,
        updatedAt = 100L,
    )

    private companion object {
        const val REFERENCE = "reference-activity"
    }
}
