package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository

class GetPurchaseAttemptUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
) : GetPurchaseAttemptUseCase {
    override suspend fun invoke(reference: String): PurchaseAttempt? {
        require(reference.isNotBlank()) { "Purchase reference must not be blank" }
        return purchaseRepository.findByReference(reference)
    }
}
