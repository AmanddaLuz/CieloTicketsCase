package br.com.amandaluz.cielotickets.feature.checkout.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository

class SavePurchaseAttemptUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
) : SavePurchaseAttemptUseCase {

    override suspend fun invoke(
        attempt: PurchaseAttempt,
    ): SavePurchaseAttemptUseCase.Result {
        if (attempt.status != PaymentStatus.CREATED) {
            return SavePurchaseAttemptUseCase.Result.InvalidInitialStatus(attempt.status)
        }

        return when (purchaseRepository.insert(attempt)) {
            PurchaseRepository.InsertResult.Inserted -> {
                SavePurchaseAttemptUseCase.Result.Saved(attempt)
            }
            PurchaseRepository.InsertResult.DuplicateReference -> {
                SavePurchaseAttemptUseCase.Result.DuplicateReference(attempt.reference)
            }
        }
    }
}
