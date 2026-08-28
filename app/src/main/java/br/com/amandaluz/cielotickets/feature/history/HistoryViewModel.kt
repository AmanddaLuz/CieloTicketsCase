package br.com.amandaluz.cielotickets.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.amandaluz.cielotickets.domain.usecase.GetSalesHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    getSalesHistory: GetSalesHistoryUseCase,
    private val uiMapper: HistoryUiMapper,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(HistoryStatusFilter.ALL)

    val uiState: StateFlow<HistoryUiState> = combine(
        getSalesHistory(),
        selectedFilter,
    ) { attempts, filter ->
        val filteredAttempts = filter.status?.let { status ->
            attempts.filter { it.status == status }
        } ?: attempts
        HistoryUiState(
            isLoading = false,
            selectedFilter = filter,
            sales = filteredAttempts.map(uiMapper::map),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HistoryUiState(),
    )

    fun selectFilter(filter: HistoryStatusFilter) {
        selectedFilter.value = filter
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
