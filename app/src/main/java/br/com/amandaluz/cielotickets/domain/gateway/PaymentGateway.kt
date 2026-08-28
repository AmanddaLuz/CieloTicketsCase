package br.com.amandaluz.cielotickets.domain.gateway

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

interface PaymentGateway {
    sealed interface Result {
        data object Initiated : Result
        data object AppNotAvailable : Result
        data object CredentialsNotConfigured : Result
        data object TechnicalFailure : Result
    }

    fun initiatePayment(attempt: PurchaseAttempt): Result
}
