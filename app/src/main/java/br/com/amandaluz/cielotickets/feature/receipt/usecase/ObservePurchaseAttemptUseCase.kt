package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import kotlinx.coroutines.flow.Flow

interface ObservePurchaseAttemptUseCase {
    operator fun invoke(reference: String): Flow<PurchaseAttempt?>
}
