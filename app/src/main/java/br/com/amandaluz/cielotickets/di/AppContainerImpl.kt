package br.com.amandaluz.cielotickets.di

import android.content.Context
import br.com.amandaluz.cielotickets.BuildConfig
import br.com.amandaluz.cielotickets.data.catalog.LocalEventRepositoryImpl
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.data.local.repository.RoomPurchaseRepositoryImpl
import br.com.amandaluz.cielotickets.domain.repository.EventRepository
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
import br.com.amandaluz.cielotickets.feature.events.usecase.BuildCartUseCaseImpl
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCaseImpl
import br.com.amandaluz.cielotickets.feature.receipt.usecase.GetPurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.feature.history.usecase.GetSalesHistoryUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCaseImpl
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCaseImpl
import br.com.amandaluz.cielotickets.payment.cielo.CieloPaymentGatewayImpl
import br.com.amandaluz.cielotickets.payment.cielo.launcher.CieloPaymentIntentLauncherImpl
import br.com.amandaluz.cielotickets.payment.cielo.encoder.CieloPaymentRequestEncoderImpl

class AppContainerImpl(
    context: Context,
) : AppContainer {
    private val applicationContext = context.applicationContext
    private val eventRepository: EventRepository = LocalEventRepositoryImpl()

    override val purchaseRepository: PurchaseRepository by lazy {
        val dao = AppDatabase.getInstance(applicationContext).purchaseAttemptDao()
        RoomPurchaseRepositoryImpl(dao)
    }

    override val getAvailableEvents: GetAvailableEventsUseCase by lazy {
        GetAvailableEventsUseCaseImpl(eventRepository)
    }

    override val buildCart: BuildCartUseCase by lazy {
        BuildCartUseCaseImpl(eventRepository)
    }

    override val buildTicketQrContent: BuildTicketQrContentUseCase by lazy {
        BuildTicketQrContentUseCaseImpl()
    }

    override val createPurchaseAttempt: CreatePurchaseAttemptUseCase by lazy {
        CreatePurchaseAttemptUseCaseImpl()
    }

    override val getPurchaseAttempt: GetPurchaseAttemptUseCase by lazy {
        GetPurchaseAttemptUseCaseImpl(purchaseRepository)
    }

    override val savePurchaseAttempt: SavePurchaseAttemptUseCase by lazy {
        SavePurchaseAttemptUseCaseImpl(purchaseRepository)
    }

    override val updatePurchaseStatus: UpdatePurchaseStatusUseCase by lazy {
        UpdatePurchaseStatusUseCaseImpl(purchaseRepository)
    }

    override val startPayment: StartPaymentUseCase by lazy {
        val paymentGateway = CieloPaymentGatewayImpl(
            clientId = BuildConfig.CIELO_CLIENT_ID,
            accessToken = BuildConfig.CIELO_ACCESS_TOKEN,
            requestEncoder = CieloPaymentRequestEncoderImpl(),
            intentLauncher = CieloPaymentIntentLauncherImpl(applicationContext),
        )
        StartPaymentUseCaseImpl(paymentGateway, updatePurchaseStatus)
    }

    override val getSalesHistory: GetSalesHistoryUseCase by lazy {
        GetSalesHistoryUseCaseImpl(purchaseRepository)
    }
}
