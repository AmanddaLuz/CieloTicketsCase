package br.com.amandaluz.cielotickets.feature.checkout.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository

/**
 * Valida a máquina de estados e persiste a mudança por compare-and-set.
 *
 * Resultados repetidos são idempotentes e estados terminais permanecem
 * imutáveis.
 */
class UpdatePurchaseStatusUseCaseImpl(
    private val purchaseRepository: PurchaseRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : UpdatePurchaseStatusUseCase {

    override suspend fun invoke(
        reference: String,
        newStatus: PaymentStatus,
    ): UpdatePurchaseStatusUseCase.Result {
        val attempt = purchaseRepository.findByReference(reference)
        return when {
            attempt == null -> UpdatePurchaseStatusUseCase.Result.NotFound(reference)
            attempt.status == newStatus -> {
                UpdatePurchaseStatusUseCase.Result.Unchanged(reference, newStatus)
            }
            !attempt.status.canTransitionTo(newStatus) -> {
                UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                    reference = reference,
                    currentStatus = attempt.status,
                    requestedStatus = newStatus,
                )
            }
            else -> updateStatus(
                reference = reference,
                expectedStatus = attempt.status,
                newStatus = newStatus,
            )
        }
    }

    private suspend fun updateStatus(
        reference: String,
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
    ): UpdatePurchaseStatusUseCase.Result =
        when (
            val result = purchaseRepository.compareAndSetStatus(
                reference = reference,
                expectedStatus = expectedStatus,
                newStatus = newStatus,
                updatedAt = currentTimeMillis(),
            )
        ) {
            PurchaseRepository.StatusUpdateResult.Updated -> {
                UpdatePurchaseStatusUseCase.Result.Updated(reference, newStatus)
            }
            PurchaseRepository.StatusUpdateResult.NotFound -> {
                UpdatePurchaseStatusUseCase.Result.NotFound(reference)
            }
            is PurchaseRepository.StatusUpdateResult.StatusMismatch -> {
                resolveStatusMismatch(reference, newStatus, result.actualStatus)
            }
        }

    private fun resolveStatusMismatch(
        reference: String,
        requestedStatus: PaymentStatus,
        actualStatus: PaymentStatus,
    ): UpdatePurchaseStatusUseCase.Result =
        if (actualStatus == requestedStatus) {
            UpdatePurchaseStatusUseCase.Result.Unchanged(reference, requestedStatus)
        } else {
            UpdatePurchaseStatusUseCase.Result.InvalidTransition(
                reference = reference,
                currentStatus = actualStatus,
                requestedStatus = requestedStatus,
            )
        }
}
