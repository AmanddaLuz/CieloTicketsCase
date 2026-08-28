package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

/**
 * Reivindica uma tentativa persistida e inicia no máximo uma cobrança externa.
 */
interface StartPaymentUseCase {
    sealed interface Result {
        data class Started(val reference: String) : Result
        data class AlreadyProcessing(val reference: String) : Result
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
