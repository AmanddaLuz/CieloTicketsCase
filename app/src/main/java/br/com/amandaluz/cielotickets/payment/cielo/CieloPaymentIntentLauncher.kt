package br.com.amandaluz.cielotickets.payment.cielo

interface CieloPaymentIntentLauncher {
    sealed interface Result {
        data object Launched : Result
        data object AppNotAvailable : Result
        data object TechnicalFailure : Result
    }

    fun launch(paymentUri: String): Result
}
