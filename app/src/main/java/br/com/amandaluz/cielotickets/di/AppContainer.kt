package br.com.amandaluz.cielotickets.di

import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import br.com.amandaluz.cielotickets.feature.events.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.feature.receipt.usecase.GetPurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.history.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase

/**
 * Contrato do composition root da aplicação.
 *
 * Features recebem abstrações prontas e não constroem repositories, banco ou
 * adapters de pagamento.
 */
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
