package br.com.amandaluz.cielotickets.payment.cielo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResult
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResultObserver
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

class CieloPaymentResultObserverImpl(
    context: Context,
) : PaymentResultObserver {
    private val applicationContext = context.applicationContext
    private var onResult: ((PaymentResult) -> Unit)? = null
    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra(CieloResponseActivity.EXTRA_STATUS)
                ?.let(::parseStatus)
                ?: return
            onResult?.invoke(
                PaymentResult(
                    reference = intent.getStringExtra(
                        CieloResponseActivity.EXTRA_REFERENCE,
                    ).orEmpty(),
                    status = status,
                    errorMessage = intent.getStringExtra(
                        CieloResponseActivity.EXTRA_ERROR_MESSAGE,
                    ),
                ),
            )
        }
    }

    override fun start(onResult: (PaymentResult) -> Unit) {
        this.onResult = onResult
        if (isRegistered) return

        val filter = IntentFilter(CieloResponseActivity.ACTION_PAYMENT_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED,
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            applicationContext.registerReceiver(receiver, filter)
        }
        isRegistered = true
    }

    override fun stop() {
        if (!isRegistered) return
        applicationContext.unregisterReceiver(receiver)
        isRegistered = false
        onResult = null
    }

    private fun parseStatus(value: String): PaymentStatus? {
        val status = PaymentStatus.entries.firstOrNull { it.name == value }
        if (status == null || !status.isTerminal) {
            Log.w(TAG, "Ignored invalid Cielo payment status")
            return null
        }
        return status
    }

    private companion object {
        const val TAG = "CieloPaymentResult"
    }
}
