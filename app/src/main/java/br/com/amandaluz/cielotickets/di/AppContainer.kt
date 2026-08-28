package br.com.amandaluz.cielotickets.di

import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.domain.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.domain.usecase.BuildTicketQrContentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetPurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.domain.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.UpdatePurchaseStatusUseCase

interface AppContainer {
    val purchaseRepository: PurchaseRepository
    val getAvailableEvents: GetAvailableEventsUseCase
    val buildCart: BuildCartUseCase
    val buildTicketQrContent: BuildTicketQrContentUseCase
    val createPurchaseAttempt: CreatePurchaseAttemptUseCase
    val getPurchaseAttempt: GetPurchaseAttemptUseCase
    val savePurchaseAttempt: SavePurchaseAttemptUseCase
    val startPayment: StartPaymentUseCase
    val updatePurchaseStatus: UpdatePurchaseStatusUseCase
    val getSalesHistory: GetSalesHistoryUseCase
}
