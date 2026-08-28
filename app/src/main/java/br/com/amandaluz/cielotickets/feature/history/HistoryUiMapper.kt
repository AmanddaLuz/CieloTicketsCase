package br.com.amandaluz.cielotickets.feature.history

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

class HistoryUiMapper(
    private val formatCurrency: (Long) -> String,
    private val formatDate: (Long) -> String,
) {
    fun map(attempt: PurchaseAttempt): HistoryItemUiModel =
        HistoryItemUiModel(
            reference = attempt.reference,
            singleEventName = attempt.items.singleOrNull()?.eventName,
            eventCount = attempt.items.size,
            totalQuantity = attempt.totalQuantity,
            totalPrice = formatCurrency(attempt.totalInCents),
            date = formatDate(attempt.createdAt),
            status = attempt.status,
        )
}
