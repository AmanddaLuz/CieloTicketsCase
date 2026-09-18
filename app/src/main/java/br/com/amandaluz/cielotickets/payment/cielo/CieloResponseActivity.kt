package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import br.com.amandaluz.cielotickets.MainActivity
import kotlinx.coroutines.launch

/**
 * Ponto de entrada do custom scheme `order://payment`.
 *
 * A Activity não apresenta UI: valida e decodifica o retorno, agenda a
 * persistência durável quando existe referência, publica um broadcast restrito
 * ao pacote e encerra imediatamente.
 */
class CieloResponseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        lifecycleScope.launch {
            handleIntent(intent)
            finish()
        }
    }

    private suspend fun handleIntent(intent: Intent?) {
        val parsedCallback = intent?.data?.let(callbackParser::parse)
        if (parsedCallback == null) {
            Log.w(TAG, "Ignored malformed Cielo callback")
            return
        }
        val callback = referenceResolver.resolve(parsedCallback)

        callbackScheduler.enqueue(callback)
        sendBroadcast(
            Intent(ACTION_PAYMENT_RESULT).apply {
                setPackage(packageName)
                putExtra(EXTRA_REFERENCE, callback.reference)
                putExtra(EXTRA_STATUS, callback.status.name)
                putExtra(EXTRA_ERROR_MESSAGE, callback.errorMessage)
            },
        )
        openResult(callback.reference)
    }

    private val callbackParser: CieloCallbackUriParser by lazy {
        CieloCallbackUriParser(CieloCallbackResponseParser())
    }

    private val callbackScheduler: CieloPaymentCallbackScheduler by lazy {
        CieloPaymentCallbackScheduler(applicationContext)
    }

    private val referenceResolver: CieloCallbackReferenceResolver by lazy {
        CieloCallbackReferenceResolver(
            CieloActivePaymentStoreImpl(applicationContext),
        )
    }

    private fun openResult(reference: String) {
        if (reference.isBlank()) {
            Log.w(TAG, "Cielo callback has no correlatable purchase reference")
            return
        }
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_PAYMENT_RESULT_REFERENCE, reference)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            },
        )
    }

    companion object {
        const val ACTION_PAYMENT_RESULT =
            "br.com.amandaluz.cielotickets.PAYMENT_RESULT"
        const val EXTRA_REFERENCE = "reference"
        const val EXTRA_STATUS = "status"
        const val EXTRA_ERROR_MESSAGE = "errorMessage"

        private const val TAG = "CieloResponse"
    }
}
