package br.com.amandaluz.cielotickets.feature.checkout

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

data class CheckoutUiState(
    val phase: CheckoutPhase = CheckoutPhase.IDLE,
    val reference: String? = null,
    val terminalStatus: PaymentStatus? = null,
    val error: CheckoutError? = null,
    val callbackMessage: String? = null,
    val receiptNavigationPending: Boolean = false,
)

enum class CheckoutPhase {
    IDLE,
    STARTING,
    PROCESSING,
    TERMINAL,
    ERROR,
}

enum class CheckoutError {
    DUPLICATE_REFERENCE,
    INVALID_INITIAL_STATUS,
    APP_NOT_AVAILABLE,
    CREDENTIALS_NOT_CONFIGURED,
    TECHNICAL_FAILURE,
    ATTEMPT_NOT_FOUND,
    INVALID_STATUS,
    CALLBACK_REJECTED,
}
