package br.com.amandaluz.cielotickets.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.domain.usecase.BuildTicketQrContentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetPurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.events.BrazilianCurrencyFormatter
import br.com.amandaluz.cielotickets.feature.history.BrazilianDateTimeFormatter

class ReceiptViewModelFactory(
    private val reference: String,
    private val getPurchaseAttempt: GetPurchaseAttemptUseCase,
    private val buildTicketQrContent: BuildTicketQrContentUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReceiptViewModel::class.java)) {
            "Unsupported ViewModel: ${modelClass.name}"
        }
        val currencyFormatter = BrazilianCurrencyFormatter()
        val dateFormatter = BrazilianDateTimeFormatter()
        @Suppress("UNCHECKED_CAST")
        return ReceiptViewModel(
            reference = reference,
            getPurchaseAttempt = getPurchaseAttempt,
            uiMapper = ReceiptUiMapper(
                formatCurrency = currencyFormatter::format,
                formatDate = dateFormatter::format,
                buildTicketQrContent = buildTicketQrContent,
            ),
        ) as T
    }
}
