package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResult
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CieloPaymentResultObserverImplTest {

    @Test
    fun forwardsPackageScopedTerminalResult() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val observer = CieloPaymentResultObserverImpl(context)
        val received = AtomicReference<PaymentResult>()
        val latch = CountDownLatch(1)
        observer.start {
            received.set(it)
            latch.countDown()
        }

        try {
            context.sendBroadcast(
                Intent(CieloResponseActivity.ACTION_PAYMENT_RESULT).apply {
                    setPackage(context.packageName)
                    putExtra(
                        CieloResponseActivity.EXTRA_REFERENCE,
                        "observer-reference",
                    )
                    putExtra(
                        CieloResponseActivity.EXTRA_STATUS,
                        PaymentStatus.CANCELLED.name,
                    )
                },
            )

            assertTrue(latch.await(3L, TimeUnit.SECONDS))
            assertEquals("observer-reference", received.get().reference)
            assertEquals(PaymentStatus.CANCELLED, received.get().status)
        } finally {
            observer.stop()
        }
    }
}
