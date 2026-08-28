package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.usecase.BuildTicketQrContentUseCase

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
