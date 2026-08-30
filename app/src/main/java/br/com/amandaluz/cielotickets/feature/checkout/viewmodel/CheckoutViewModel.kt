package br.com.amandaluz.cielotickets.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResult
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResultObserver
import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutError
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutPhase
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Orquestra o checkout com persistência anterior à cobrança e consumo
 * idempotente do callback.
 *
 * O observer externo é registrado durante a vida do ViewModel para continuar
 * ativo enquanto o aplicativo de pagamento está em primeiro plano.
 */
class CheckoutViewModel(
    private val createPurchaseAttempt: CreatePurchaseAttemptUseCase,
    private val savePurchaseAttempt: SavePurchaseAttemptUseCase,
    private val startPayment: StartPaymentUseCase,
    private val updatePurchaseStatus: UpdatePurchaseStatusUseCase,
    private val paymentResultObserver: PaymentResultObserver,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = mutableUiState.asStateFlow()

    private val paymentMutex = Mutex()
    private var currentAttempt: PurchaseAttempt? = null

    init {
        paymentResultObserver.start(::onPaymentResult)
    }

    fun start(cart: Cart) {
        viewModelScope.launch {
            paymentMutex.withLock {
                if (mutableUiState.value.phase in ACTIVE_PHASES) return@withLock

                mutableUiState.value = CheckoutUiState(
                    phase = CheckoutPhase.STARTING,
                )
                val attempt = createPurchaseAttempt(cart)
                currentAttempt = attempt

                when (val saveResult = savePurchaseAttempt(attempt)) {
                    is SavePurchaseAttemptUseCase.Result.Saved -> {
                        handleStartResult(startPayment(saveResult.attempt))
                    }
                    is SavePurchaseAttemptUseCase.Result.DuplicateReference -> {
                        showError(
                            CheckoutError.DUPLICATE_REFERENCE,
                            saveResult.reference,
                        )
                    }
                    is SavePurchaseAttemptUseCase.Result.InvalidInitialStatus -> {
                        showError(CheckoutError.INVALID_INITIAL_STATUS)
                    }
                }
            }
        }
    }

    fun reset() {
        if (mutableUiState.value.phase in ACTIVE_PHASES ||
            mutableUiState.value.receiptNavigationPending
        ) {
            return
        }
        currentAttempt = null
        mutableUiState.value = CheckoutUiState()
    }

    fun consumeReceiptNavigation() {
        mutableUiState.value = mutableUiState.value.copy(
            receiptNavigationPending = false,
        )
    }

    override fun onCleared() {
        paymentResultObserver.stop()
        super.onCleared()
    }

    private fun handleStartResult(result: StartPaymentUseCase.Result) {
        mutableUiState.value = when (result) {
            is StartPaymentUseCase.Result.Started -> CheckoutUiState(
                phase = CheckoutPhase.PROCESSING,
                reference = result.reference,
            )
            is StartPaymentUseCase.Result.AlreadyProcessing -> CheckoutUiState(
                phase = CheckoutPhase.PROCESSING,
                reference = result.reference,
            )
            is StartPaymentUseCase.Result.AppNotAvailable -> errorState(
                CheckoutError.APP_NOT_AVAILABLE,
                result.reference,
            )
            is StartPaymentUseCase.Result.CredentialsNotConfigured -> errorState(
                CheckoutError.CREDENTIALS_NOT_CONFIGURED,
                result.reference,
            )
            is StartPaymentUseCase.Result.TechnicalFailure -> errorState(
                CheckoutError.TECHNICAL_FAILURE,
                result.reference,
            )
            is StartPaymentUseCase.Result.NotFound -> errorState(
                CheckoutError.ATTEMPT_NOT_FOUND,
                result.reference,
            )
            is StartPaymentUseCase.Result.InvalidStatus -> errorState(
                CheckoutError.INVALID_STATUS,
                result.reference,
            )
        }
    }

    private fun onPaymentResult(result: PaymentResult) {
        viewModelScope.launch {
            paymentMutex.withLock {
                val attempt = currentAttempt ?: return@withLock
                if (mutableUiState.value.phase != CheckoutPhase.PROCESSING) {
                    return@withLock
                }
                if (result.reference.isNotBlank() &&
                    result.reference != attempt.reference
                ) {
                    return@withLock
                }

                when (updatePurchaseStatus(attempt.reference, result.status)) {
                    is UpdatePurchaseStatusUseCase.Result.Updated,
                    is UpdatePurchaseStatusUseCase.Result.Unchanged,
                    -> {
                        mutableUiState.value = CheckoutUiState(
                            phase = CheckoutPhase.TERMINAL,
                            reference = attempt.reference,
                            terminalStatus = result.status,
                            callbackMessage = result.errorMessage,
                            receiptNavigationPending =
                                result.status == PaymentStatus.APPROVED,
                        )
                    }
                    is UpdatePurchaseStatusUseCase.Result.NotFound,
                    is UpdatePurchaseStatusUseCase.Result.InvalidTransition,
                    -> showError(
                        CheckoutError.CALLBACK_REJECTED,
                        attempt.reference,
                    )
                }
            }
        }
    }

    private fun showError(error: CheckoutError, reference: String? = null) {
        mutableUiState.value = errorState(error, reference)
    }

    private fun errorState(
        error: CheckoutError,
        reference: String? = null,
    ) = CheckoutUiState(
        phase = CheckoutPhase.ERROR,
        reference = reference,
        error = error,
    )

    private companion object {
        val ACTIVE_PHASES = setOf(
            CheckoutPhase.STARTING,
            CheckoutPhase.PROCESSING,
        )
    }
}
