package br.com.amandaluz.cielotickets.feature.checkout.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

/**
 * Reivindica uma tentativa persistida e inicia no máximo uma cobrança externa.
 */
fun interface StartPaymentUseCase {
    sealed interface Result {
        sealed interface Processing : Result {
            val reference: String
        }

        data class Started(
            override val reference: String,
        ) : Processing

        data class AlreadyProcessing(
            override val reference: String,
        ) : Processing

        data class AppNotAvailable(val reference: String) : Result
        data class CredentialsNotConfigured(val reference: String) : Result
        data class TechnicalFailure(val reference: String) : Result
        data class NotFound(val reference: String) : Result
        data class InvalidStatus(
            val reference: String,
            val currentStatus: PaymentStatus,
        ) : Result
    }

    suspend operator fun invoke(attempt: PurchaseAttempt): Result
}
