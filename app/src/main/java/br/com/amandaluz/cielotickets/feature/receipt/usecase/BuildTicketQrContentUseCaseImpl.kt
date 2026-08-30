package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

/**
 * Produz um payload opaco somente para compras aprovadas.
 *
 * O conteúdo não inclui preços, eventos, credenciais ou dados do pagamento.
 */
class BuildTicketQrContentUseCaseImpl : BuildTicketQrContentUseCase {
    override fun invoke(attempt: PurchaseAttempt): String? =
        if (attempt.status == PaymentStatus.APPROVED) {
            "$PREFIX|${attempt.reference}"
        } else {
            null
        }

    private companion object {
        const val PREFIX = "CIELO_TICKET"
    }
}
