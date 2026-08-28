package br.com.amandaluz.cielotickets.payment.cielo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Ponto de entrada do custom scheme `order://payment`.
 *
 * A Activity não apresenta UI: valida e decodifica o retorno, publica um
 * broadcast restrito ao pacote e encerra imediatamente.
 */
class CieloResponseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        val callback = intent?.data?.let(callbackParser::parse)
        if (callback == null) {
            Log.w(TAG, "Ignored malformed Cielo callback")
            return
        }

        sendBroadcast(
            Intent(ACTION_PAYMENT_RESULT).apply {
                setPackage(packageName)
                putExtra(EXTRA_REFERENCE, callback.reference)
                putExtra(EXTRA_STATUS, callback.status.name)
                putExtra(EXTRA_ERROR_MESSAGE, callback.errorMessage)
            },
        )
    }

    private val callbackParser: CieloCallbackUriParser by lazy {
        CieloCallbackUriParser(CieloCallbackResponseParser())
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
