package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

class CieloPaymentCallbackWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val callback = parseInput()
        val application = applicationContext as? CieloTicketsApplication
        return when {
            callback == null || application == null -> Result.failure()
            else -> persistCallback(application, callback)
        }
    }

    private fun parseInput(): CallbackInput? {
        val reference = inputData.getString(KEY_REFERENCE)
            ?.takeIf(String::isNotBlank)
        val status = inputData.getString(KEY_STATUS)
            ?.let(::parseStatus)
            ?.takeIf(PaymentStatus::isTerminal)
        return if (reference == null || status == null) {
            null
        } else {
            CallbackInput(reference, status)
        }
    }

    private suspend fun persistCallback(
        application: CieloTicketsApplication,
        callback: CallbackInput,
    ): Result = try {
        application.appContainer.updatePurchaseStatus(
            callback.reference,
            callback.status,
        )
        CieloActivePaymentStoreImpl(application).clear(callback.reference)
        Result.success()
    } catch (_: SQLiteException) {
        Result.retry()
    }

    private fun parseStatus(value: String): PaymentStatus? =
        runCatching { PaymentStatus.valueOf(value) }.getOrNull()

    private data class CallbackInput(
        val reference: String,
        val status: PaymentStatus,
    )

    companion object {
        const val KEY_REFERENCE = "reference"
        const val KEY_STATUS = "status"
    }
}
