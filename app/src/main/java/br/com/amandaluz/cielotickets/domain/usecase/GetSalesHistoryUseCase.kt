package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import kotlinx.coroutines.flow.Flow

interface GetSalesHistoryUseCase {
    operator fun invoke(): Flow<List<PurchaseAttempt>>
}

