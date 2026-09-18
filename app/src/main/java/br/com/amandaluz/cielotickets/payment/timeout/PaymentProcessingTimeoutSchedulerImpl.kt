package br.com.amandaluz.cielotickets.payment.timeout

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import java.util.concurrent.TimeUnit

class PaymentProcessingTimeoutSchedulerImpl(
    context: Context,
) : PaymentProcessingTimeoutScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override suspend fun schedule(reference: String) {
        require(reference.isNotBlank()) { "Payment reference must not be blank" }
        val request = OneTimeWorkRequestBuilder<PaymentProcessingTimeoutWorker>()
            .setInitialDelay(TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(PaymentProcessingTimeoutWorker.KEY_REFERENCE, reference)
                    .build(),
            )
            .build()

        workManager.enqueueUniqueWork(
            workName(reference),
            ExistingWorkPolicy.KEEP,
            request,
        ).await()
    }

    companion object {
        const val TIMEOUT_MINUTES = 1L

        fun workName(reference: String): String =
            "payment-processing-timeout-$reference"
    }
}
