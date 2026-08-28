package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

/**
 * Aplica uma transição válida de pagamento sem sobrescrever estados terminais.
 */
interface UpdatePurchaseStatusUseCase {
    sealed interface Result {
        data class Updated(
            val reference: String,
            val status: PaymentStatus,
        ) : Result

        data class Unchanged(
            val reference: String,
            val status: PaymentStatus,
        ) : Result

        data class NotFound(val reference: String) : Result

        data class InvalidTransition(
            val reference: String,
            val currentStatus: PaymentStatus,
            val requestedStatus: PaymentStatus,
        ) : Result
    }

    suspend operator fun invoke(
        reference: String,
        newStatus: PaymentStatus,
    ): Result
}
