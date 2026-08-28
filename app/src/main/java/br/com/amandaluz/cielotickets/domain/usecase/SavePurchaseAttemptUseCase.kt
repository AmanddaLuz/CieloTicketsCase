package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

interface SavePurchaseAttemptUseCase {
    sealed interface Result {
        data class Saved(val attempt: PurchaseAttempt) : Result
        data class DuplicateReference(val reference: String) : Result
        data class InvalidInitialStatus(val actualStatus: PaymentStatus) : Result
    }

    suspend operator fun invoke(attempt: PurchaseAttempt): Result
}

