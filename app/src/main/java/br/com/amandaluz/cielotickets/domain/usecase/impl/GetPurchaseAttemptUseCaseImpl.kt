package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.domain.usecase.GetPurchaseAttemptUseCase

class GetPurchaseAttemptUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
) : GetPurchaseAttemptUseCase {
    override suspend fun invoke(reference: String): PurchaseAttempt? {
        require(reference.isNotBlank()) { "Purchase reference must not be blank" }
        return purchaseRepository.findByReference(reference)
    }
}
