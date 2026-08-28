package br.com.amandaluz.cielotickets.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.amandaluz.cielotickets.domain.usecase.GetPurchaseAttemptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptViewModel(
    reference: String,
    private val getPurchaseAttempt: GetPurchaseAttemptUseCase,
    private val uiMapper: ReceiptUiMapper,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow<ReceiptUiState>(ReceiptUiState.Loading)
    val uiState: StateFlow<ReceiptUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            mutableUiState.value = getPurchaseAttempt(reference)
                ?.let(uiMapper::map)
                ?.let(ReceiptUiState::Content)
                ?: ReceiptUiState.NotFound
        }
    }
}
