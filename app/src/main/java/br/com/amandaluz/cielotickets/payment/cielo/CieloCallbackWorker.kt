package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.domain.usecase.UpdatePurchaseStatusUseCase

class CieloCallbackWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result =
        try {
            processCallback()
        } catch (_: SQLiteException) {
            Result.retry()
        }

    private suspend fun processCallback(): Result {
        val callback = readCallback() ?: return Result.failure()
        return persistCallback(callback)
    }

    private suspend fun persistCallback(callback: CallbackData): Result {
        val attempt = purchaseRepository.findByReference(callback.reference)
        if (
            callback.status == PaymentStatus.APPROVED &&
            callback.paidAmountInCents != attempt?.totalInCents
        ) {
            Log.w(TAG, "Ignored callback with mismatched paid amount")
            return Result.failure()
        }

        return when (
            val update = updatePurchaseStatus(callback.reference, callback.status)
        ) {
            is UpdatePurchaseStatusUseCase.Result.Updated,
            is UpdatePurchaseStatusUseCase.Result.Unchanged,
            -> Result.success()
            is UpdatePurchaseStatusUseCase.Result.NotFound -> {
                Log.w(TAG, "Ignored callback for unknown purchase")
                Result.failure()
            }
            is UpdatePurchaseStatusUseCase.Result.InvalidTransition -> {
                Log.w(
                    TAG,
                    "Ignored callback transition from ${update.currentStatus} " +
                        "to ${update.requestedStatus}",
                )
                Result.failure()
            }
        }
    }

    private fun readCallback(): CallbackData? {
        val reference = inputData.getString(KEY_REFERENCE)
            ?.takeIf(String::isNotBlank)
        val status = inputData.getString(KEY_STATUS)?.let(::parseStatus)
        return if (reference != null && status != null) {
            CallbackData(
                reference = reference,
                status = status,
                paidAmountInCents = inputData.getLong(
                    KEY_PAID_AMOUNT,
                    NO_PAID_AMOUNT,
                ).takeUnless { it == NO_PAID_AMOUNT },
            )
        } else {
            null
        }
    }

    private fun parseStatus(status: String): PaymentStatus? =
        try {
            PaymentStatus.valueOf(status)
        } catch (_: IllegalArgumentException) {
            null
        }

    private val purchaseRepository: PurchaseRepository by lazy {
        appContainer.purchaseRepository
    }

    private val updatePurchaseStatus: UpdatePurchaseStatusUseCase by lazy {
        appContainer.updatePurchaseStatus
    }

    private val appContainer by lazy {
        (applicationContext as CieloTicketsApplication).appContainer
    }

    private data class CallbackData(
        val reference: String,
        val status: PaymentStatus,
        val paidAmountInCents: Long?,
    )

    companion object {
        const val KEY_REFERENCE = "reference"
        const val KEY_STATUS = "status"
        const val KEY_PAID_AMOUNT = "paidAmount"
        const val NO_PAID_AMOUNT = -1L

        private const val TAG = "CieloCallbackWorker"
    }
}
