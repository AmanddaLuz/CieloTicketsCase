package br.com.amandaluz.cielotickets.feature.receipt

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

/** Estados imutáveis renderizados pela tela de comprovante. */
sealed interface ReceiptUiState {
    /** A compra ainda está sendo recuperada da persistência. */
    data object Loading : ReceiptUiState

    /** Não existe tentativa para a referência informada. */
    data object NotFound : ReceiptUiState

    /** O comprovante persistido está pronto para apresentação. */
    data class Content(val receipt: ReceiptUiModel) : ReceiptUiState
}

/**
 * Dados completos do comprovante já formatados para apresentação.
 *
 * O QR Code é ausente para tentativas que não estejam aprovadas.
 */
data class ReceiptUiModel(
    val reference: String,
    val date: String,
    val status: PaymentStatus,
    val items: List<ReceiptItemUiModel>,
    val totalQuantity: Int,
    val totalPrice: String,
    val qrContent: String?,
)

/**
 * Item do comprovante preparado para binding.
 *
 * [eventId] não é exibido: ele preserva a identidade estável usada pelo
 * `DiffUtil`, enquanto nome e valores permanecem atributos de apresentação.
 */
data class ReceiptItemUiModel(
    val eventId: String,
    val eventName: String,
    val quantity: Int,
    val unitPrice: String,
    val subtotal: String,
)
