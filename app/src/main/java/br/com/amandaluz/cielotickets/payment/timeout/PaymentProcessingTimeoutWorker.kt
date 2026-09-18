package br.com.amandaluz.cielotickets.payment.timeout

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import br.com.amandaluz.cielotickets.payment.cielo.CieloResponseActivity

class PaymentProcessingTimeoutWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val reference = inputData.getString(KEY_REFERENCE)
            ?.takeIf(String::isNotBlank)
        val application = applicationContext as? CieloTicketsApplication
        return when {
            reference == null || application == null -> Result.failure()
            else -> expire(application, reference)
        }
    }

    private suspend fun expire(
        application: CieloTicketsApplication,
        reference: String,
    ): Result = try {
        val update = application.appContainer.updatePurchaseStatus(
            reference,
            PaymentStatus.TIMED_OUT,
        )
        if (update is UpdatePurchaseStatusUseCase.Result.Updated ||
            update is UpdatePurchaseStatusUseCase.Result.Unchanged
        ) {
            notifyActiveUi(reference)
        }
        Result.success()
    } catch (_: SQLiteException) {
        Result.retry()
    }

    private fun notifyActiveUi(reference: String) {
        applicationContext.sendBroadcast(
            Intent(CieloResponseActivity.ACTION_PAYMENT_RESULT).apply {
                setPackage(applicationContext.packageName)
                putExtra(CieloResponseActivity.EXTRA_REFERENCE, reference)
                putExtra(
                    CieloResponseActivity.EXTRA_STATUS,
                    PaymentStatus.TIMED_OUT.name,
                )
            },
        )
    }

    companion object {
        const val KEY_REFERENCE = "reference"
    }
}
