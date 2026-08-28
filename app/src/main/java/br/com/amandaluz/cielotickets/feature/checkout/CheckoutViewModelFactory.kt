package br.com.amandaluz.cielotickets.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResultObserver
import br.com.amandaluz.cielotickets.domain.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.UpdatePurchaseStatusUseCase

class CheckoutViewModelFactory(
    private val createPurchaseAttempt: CreatePurchaseAttemptUseCase,
    private val savePurchaseAttempt: SavePurchaseAttemptUseCase,
    private val startPayment: StartPaymentUseCase,
    private val updatePurchaseStatus: UpdatePurchaseStatusUseCase,
    private val paymentResultObserver: PaymentResultObserver,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            "Unsupported ViewModel: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return CheckoutViewModel(
            createPurchaseAttempt = createPurchaseAttempt,
            savePurchaseAttempt = savePurchaseAttempt,
            startPayment = startPayment,
            updatePurchaseStatus = updatePurchaseStatus,
            paymentResultObserver = paymentResultObserver,
        ) as T
    }
}
