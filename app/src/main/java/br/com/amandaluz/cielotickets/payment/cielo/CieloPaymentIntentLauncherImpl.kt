package br.com.amandaluz.cielotickets.payment.cielo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

class CieloPaymentIntentLauncherImpl(
    private val context: Context,
) : CieloPaymentIntentLauncher {

    override fun launch(paymentUri: String): CieloPaymentIntentLauncher.Result {
        val intent = Intent(Intent.ACTION_VIEW, paymentUri.toUri()).apply {
            setPackage(CIELO_APP_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (context.packageManager.resolveActivity(intent, 0) == null) {
            return CieloPaymentIntentLauncher.Result.AppNotAvailable
        }

        return try {
            context.startActivity(intent)
            CieloPaymentIntentLauncher.Result.Launched
        } catch (_: ActivityNotFoundException) {
            CieloPaymentIntentLauncher.Result.AppNotAvailable
        } catch (_: SecurityException) {
            CieloPaymentIntentLauncher.Result.TechnicalFailure
        }
    }

    private companion object {
        const val CIELO_APP_PACKAGE = "br.com.cielosmart.orderservice"
    }
}
