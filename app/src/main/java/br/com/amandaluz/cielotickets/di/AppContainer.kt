package br.com.amandaluz.cielotickets.di

import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.domain.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.domain.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.domain.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.domain.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.UpdatePurchaseStatusUseCase

interface AppContainer {
    val purchaseRepository: PurchaseRepository
    val getAvailableEvents: GetAvailableEventsUseCase
    val buildCart: BuildCartUseCase
    val createPurchaseAttempt: CreatePurchaseAttemptUseCase
    val savePurchaseAttempt: SavePurchaseAttemptUseCase
    val startPayment: StartPaymentUseCase
    val updatePurchaseStatus: UpdatePurchaseStatusUseCase
    val getSalesHistory: GetSalesHistoryUseCase
}

