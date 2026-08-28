package br.com.amandaluz.cielotickets.feature.receipt

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

sealed interface ReceiptUiState {
    data object Loading : ReceiptUiState
    data object NotFound : ReceiptUiState
    data class Content(val receipt: ReceiptUiModel) : ReceiptUiState
}

data class ReceiptUiModel(
    val reference: String,
    val date: String,
    val status: PaymentStatus,
    val items: List<ReceiptItemUiModel>,
    val totalQuantity: Int,
    val totalPrice: String,
    val qrContent: String?,
)

data class ReceiptItemUiModel(
    val eventName: String,
    val quantity: Int,
    val unitPrice: String,
    val subtotal: String,
)
