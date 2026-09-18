package br.com.amandaluz.cielotickets

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import br.com.amandaluz.cielotickets.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pendingPaymentResultReference: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        capturePaymentResult(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        capturePaymentResult(intent)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            openPendingPaymentResult()
        }
    }

    override fun onResume() {
        super.onResume()
        openPendingPaymentResult()
    }

    private fun capturePaymentResult(intent: Intent?) {
        pendingPaymentResultReference = intent
            ?.getStringExtra(EXTRA_PAYMENT_RESULT_REFERENCE)
            ?.takeIf(String::isNotBlank)
        intent?.removeExtra(EXTRA_PAYMENT_RESULT_REFERENCE)
    }

    private fun openPendingPaymentResult() {
        if (supportFragmentManager.isStateSaved) {
            return
        }
        val reference = pendingPaymentResultReference ?: return
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost)
            as NavHostFragment
        val navController = navHost.navController
        val currentReference = navController.currentBackStackEntry
            ?.arguments
            ?.getString(RECEIPT_REFERENCE_ARGUMENT)
        val alreadyShowingResult =
            navController.currentDestination?.id == R.id.receiptFragment &&
                currentReference == reference
        if (!alreadyShowingResult) {
            navController.navigate(
                R.id.receiptFragment,
                bundleOf(RECEIPT_REFERENCE_ARGUMENT to reference),
            )
        }
        pendingPaymentResultReference = null
    }

    companion object {
        const val EXTRA_PAYMENT_RESULT_REFERENCE =
            "br.com.amandaluz.cielotickets.PAYMENT_RESULT_REFERENCE"
        private const val RECEIPT_REFERENCE_ARGUMENT = "reference"
    }
}
