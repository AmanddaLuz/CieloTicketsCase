package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

fun interface GetPurchaseAttemptUseCase {
    suspend operator fun invoke(reference: String): PurchaseAttempt?
}
