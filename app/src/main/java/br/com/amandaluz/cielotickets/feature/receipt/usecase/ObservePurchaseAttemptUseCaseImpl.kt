package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow

class ObservePurchaseAttemptUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
) : ObservePurchaseAttemptUseCase {
    override fun invoke(reference: String): Flow<PurchaseAttempt?> {
        require(reference.isNotBlank()) { "Purchase reference must not be blank" }
        return purchaseRepository.observeByReference(reference)
    }
}
