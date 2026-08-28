package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class CieloResponseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enqueueCallback(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        enqueueCallback(intent)
        finish()
    }

    private fun enqueueCallback(intent: Intent?) {
        val callback = intent?.data?.let(callbackParser::parse)
        if (callback == null) {
            Log.w(TAG, "Ignored malformed Cielo callback")
            return
        }
        val request = OneTimeWorkRequestBuilder<CieloCallbackWorker>()
            .setInputData(
                workDataOf(
                    CieloCallbackWorker.KEY_REFERENCE to callback.reference,
                    CieloCallbackWorker.KEY_STATUS to callback.status.name,
                    CieloCallbackWorker.KEY_PAID_AMOUNT to (
                        callback.paidAmountInCents ?: CieloCallbackWorker.NO_PAID_AMOUNT
                        ),
                ),
            )
            .build()
        val uniqueWorkName = "$WORK_PREFIX:${callback.reference}:${callback.status.name}"

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private val callbackParser: CieloCallbackUriParser by lazy {
        CieloCallbackUriParser(CieloCallbackResponseParser())
    }

    private companion object {
        const val TAG = "CieloResponse"
        const val WORK_PREFIX = "cielo-callback"
    }
}

