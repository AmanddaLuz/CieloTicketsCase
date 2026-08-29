package br.com.amandaluz.cielotickets.feature.receipt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCase
import br.com.amandaluz.cielotickets.feature.receipt.usecase.GetPurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.common.BrazilianCurrencyFormatter
import br.com.amandaluz.cielotickets.feature.common.BrazilianDateTimeFormatter
import br.com.amandaluz.cielotickets.feature.receipt.ReceiptUiMapper

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
