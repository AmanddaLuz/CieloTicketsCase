package br.com.amandaluz.cielotickets.feature.receipt

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCase

/**
 * Converte uma tentativa persistida em dados prontos para o comprovante.
 *
 * Centraliza formatação de moeda e data, preserva a identidade dos itens e
 * delega ao domínio a decisão de produzir conteúdo para o QR Code.
 */
class ReceiptUiMapper(
    private val formatCurrency: (Long) -> String,
    private val formatDate: (Long) -> String,
    private val buildTicketQrContent: BuildTicketQrContentUseCase,
) {
    /** Mapeia o agregado sem transferir cálculos ou formatação para a View. */
    fun map(attempt: PurchaseAttempt): ReceiptUiModel = ReceiptUiModel(
        reference = attempt.reference,
        date = formatDate(attempt.createdAt),
        status = attempt.status,
        items = attempt.items.map { item ->
            ReceiptItemUiModel(
                eventId = item.eventId,
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
