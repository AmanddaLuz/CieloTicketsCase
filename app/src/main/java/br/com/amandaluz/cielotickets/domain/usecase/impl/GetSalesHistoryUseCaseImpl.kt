package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.domain.usecase.GetSalesHistoryUseCase
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
