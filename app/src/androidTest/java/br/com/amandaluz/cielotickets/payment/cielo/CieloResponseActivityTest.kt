package br.com.amandaluz.cielotickets.payment.cielo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Base64
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.payment.cielo.encoder.CieloPaymentRequestEncoderImpl
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
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
        context.registerReceiver(
            receiver,
            IntentFilter(CieloResponseActivity.ACTION_PAYMENT_RESULT),
        )

        try {
            val rawResponse = """{"reference":"reference-activity","id":"order-1"}"""
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

            ActivityScenario.launch<CieloResponseActivity>(activityIntent).close()

            assertTrue(callbackReceived.await(3L, TimeUnit.SECONDS))
            assertEquals(
                "reference-activity",
                receivedIntent.get().getStringExtra(CieloResponseActivity.EXTRA_REFERENCE),
            )
            assertEquals(
                PaymentStatus.APPROVED.name,
                receivedIntent.get().getStringExtra(CieloResponseActivity.EXTRA_STATUS),
            )
        } finally {
            context.unregisterReceiver(receiver)
        }
    }
}
