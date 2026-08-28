package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

interface BuildTicketQrContentUseCase {
    operator fun invoke(attempt: PurchaseAttempt): String?
}
