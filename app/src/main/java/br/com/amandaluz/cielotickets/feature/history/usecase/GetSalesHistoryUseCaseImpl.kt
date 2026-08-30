package br.com.amandaluz.cielotickets.feature.history.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSalesHistoryUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
) : GetSalesHistoryUseCase {
    override fun invoke(): Flow<List<PurchaseAttempt>> =
        purchaseRepository.observeHistory().map { attempts ->
            attempts.sortedByDescending(PurchaseAttempt::createdAt)
        }
}
