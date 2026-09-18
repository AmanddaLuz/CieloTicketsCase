package br.com.amandaluz.cielotickets.feature.receipt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.amandaluz.cielotickets.feature.receipt.usecase.ObservePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.receipt.ReceiptUiMapper
import br.com.amandaluz.cielotickets.feature.receipt.ReceiptUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Recupera o comprovante pela referência persistida e publica um modelo pronto
 * para renderização, sem transportar snapshots pela navegação.
 */
class ReceiptViewModel(
    reference: String,
    observePurchaseAttempt: ObservePurchaseAttemptUseCase,
    private val uiMapper: ReceiptUiMapper,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow<ReceiptUiState>(ReceiptUiState.Loading)
    val uiState: StateFlow<ReceiptUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePurchaseAttempt(reference).collect { attempt ->
                mutableUiState.value = attempt
                    ?.let(uiMapper::map)
                    ?.let(ReceiptUiState::Content)
                    ?: ReceiptUiState.NotFound
            }
        }
    }
}
