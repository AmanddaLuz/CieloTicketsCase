package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await

class CieloPaymentCallbackScheduler(
    context: Context,
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    suspend fun enqueue(callback: CieloCallbackResult) {
        if (callback.reference.isBlank()) {
            return
        }

        val request = OneTimeWorkRequestBuilder<CieloPaymentCallbackWorker>()
            .setInputData(
                Data.Builder()
                    .putString(
                        CieloPaymentCallbackWorker.KEY_REFERENCE,
                        callback.reference,
                    )
                    .putString(
                        CieloPaymentCallbackWorker.KEY_STATUS,
                        callback.status.name,
                    )
                    .build(),
            )
            .build()

        workManager
            .enqueueUniqueWork(
                workName(callback.reference),
                ExistingWorkPolicy.KEEP,
                request,
            )
            .await()
    }

    companion object {
        fun workName(reference: String): String = "cielo-callback-$reference"
    }
}
