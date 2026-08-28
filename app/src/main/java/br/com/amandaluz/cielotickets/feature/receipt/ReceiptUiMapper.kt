package br.com.amandaluz.cielotickets.feature.receipt

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.usecase.BuildTicketQrContentUseCase

class ReceiptUiMapper(
    private val formatCurrency: (Long) -> String,
    private val formatDate: (Long) -> String,
    private val buildTicketQrContent: BuildTicketQrContentUseCase,
) {
    fun map(attempt: PurchaseAttempt): ReceiptUiModel = ReceiptUiModel(
        reference = attempt.reference,
        date = formatDate(attempt.createdAt),
        status = attempt.status,
        items = attempt.items.map { item ->
            ReceiptItemUiModel(
                eventName = item.eventName,
                quantity = item.quantity,
                unitPrice = formatCurrency(item.unitPriceInCents),
                subtotal = formatCurrency(item.subtotalInCents),
            )
        },
        totalQuantity = attempt.totalQuantity,
        totalPrice = formatCurrency(attempt.totalInCents),
        qrContent = buildTicketQrContent(attempt),
    )
}
