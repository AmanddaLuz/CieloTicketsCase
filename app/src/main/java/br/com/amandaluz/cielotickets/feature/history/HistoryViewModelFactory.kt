package br.com.amandaluz.cielotickets.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.domain.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.feature.events.BrazilianCurrencyFormatter

class HistoryViewModelFactory(
    private val getSalesHistory: GetSalesHistoryUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            "Unsupported ViewModel: ${modelClass.name}"
        }
        val currencyFormatter = BrazilianCurrencyFormatter()
        val dateFormatter = BrazilianDateTimeFormatter()
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(
            getSalesHistory = getSalesHistory,
            uiMapper = HistoryUiMapper(
                formatCurrency = currencyFormatter::format,
                formatDate = dateFormatter::format,
            ),
        ) as T
    }
}
