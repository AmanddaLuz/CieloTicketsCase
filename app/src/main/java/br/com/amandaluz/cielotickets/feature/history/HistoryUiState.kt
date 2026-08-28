package br.com.amandaluz.cielotickets.feature.history

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

data class HistoryUiState(
    val isLoading: Boolean = true,
    val selectedFilter: HistoryStatusFilter = HistoryStatusFilter.ALL,
    val sales: List<HistoryItemUiModel> = emptyList(),
)

enum class HistoryStatusFilter(
    val status: PaymentStatus?,
) {
    ALL(null),
    APPROVED(PaymentStatus.APPROVED),
    DENIED(PaymentStatus.DENIED),
    CANCELLED(PaymentStatus.CANCELLED),
    ERROR(PaymentStatus.ERROR),
    PROCESSING(PaymentStatus.PROCESSING),
    CREATED(PaymentStatus.CREATED),
}

data class HistoryItemUiModel(
    val reference: String,
    val singleEventName: String?,
    val eventCount: Int,
    val totalQuantity: Int,
    val totalPrice: String,
    val date: String,
    val status: PaymentStatus,
)
