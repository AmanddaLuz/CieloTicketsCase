package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.feature.checkout.ActivePaymentCoordinator
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase

class CieloActivePaymentCoordinatorImpl(
    private val activePaymentStore: CieloActivePaymentStore,
    private val updatePurchaseStatus: UpdatePurchaseStatusUseCase,
) : ActivePaymentCoordinator {

    override suspend fun activate(reference: String): Boolean {
        require(reference.isNotBlank()) { "Payment reference must not be blank" }
        val currentReference = activePaymentStore.currentReference()
        return when {
            currentReference == null || currentReference == reference -> {
                activePaymentStore.claim(reference)
            }
            expirePrevious(currentReference) -> {
                activePaymentStore.replace(currentReference, reference)
            }
            else -> false
        }
    }

    override fun clear(reference: String) {
        activePaymentStore.clear(reference)
    }

    private suspend fun expirePrevious(reference: String): Boolean =
        when (
            val result = updatePurchaseStatus(
                reference,
                PaymentStatus.TIMED_OUT,
            )
        ) {
            is UpdatePurchaseStatusUseCase.Result.Updated,
            is UpdatePurchaseStatusUseCase.Result.Unchanged,
            is UpdatePurchaseStatusUseCase.Result.NotFound,
            -> true
            is UpdatePurchaseStatusUseCase.Result.InvalidTransition -> {
                result.currentStatus == PaymentStatus.TIMED_OUT ||
                    result.currentStatus.isTerminal
            }
        }
}
