package br.com.amandaluz.cielotickets.feature.checkout

import androidx.lifecycle.ViewModelStore
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResult
import br.com.amandaluz.cielotickets.domain.gateway.PaymentResultObserver
import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.CartItem
import br.com.amandaluz.cielotickets.domain.model.Event
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.feature.checkout.usecase.CreatePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.SavePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.feature.checkout.usecase.UpdatePurchaseStatusUseCase
import br.com.amandaluz.cielotickets.feature.checkout.viewmodel.CheckoutViewModel
import br.com.amandaluz.cielotickets.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val event = Event(
        id = "event-1",
        name = "Festival",
        venue = "Praça",
        date = "12 set",
        priceInCents = 3_500L,
        maxTicketsPerPurchase = 4,
    )
    private val cart = Cart(listOf(CartItem(event, 2)))
    private val attempt = PurchaseAttempt.create(
        reference = REFERENCE,
        cart = cart,
        createdAt = 100L,
    )

    @Test
    fun persistsAttemptBeforeStartingPayment() {
        val calls = mutableListOf<String>()
        val observer = FakePaymentResultObserver()
        val viewModel = viewModel(
            observer = observer,
            save = {
                calls += "save"
                SavePurchaseAttemptUseCase.Result.Saved(it)
            },
            start = {
                calls += "start"
                StartPaymentUseCase.Result.Started(it.reference)
            },
        )

        viewModel.start(cart)

        assertEquals(listOf("save", "start"), calls)
        assertEquals(CheckoutPhase.PROCESSING, viewModel.uiState.value.phase)
        assertEquals(REFERENCE, viewModel.uiState.value.reference)
        assertTrue(observer.started)
    }

    @Test
    fun repeatedClicksCreateOnlyOneAttempt() {
        val releaseStart = CompletableDeferred<Unit>()
        var createCalls = 0
        var saveCalls = 0
        var startCalls = 0
        val viewModel = viewModel(
            create = {
                createCalls += 1
                attempt
            },
            save = {
                saveCalls += 1
                SavePurchaseAttemptUseCase.Result.Saved(it)
            },
            start = {
                startCalls += 1
                releaseStart.await()
                StartPaymentUseCase.Result.Started(it.reference)
            },
        )

        viewModel.start(cart)
        viewModel.start(cart)
        releaseStart.complete(Unit)

        assertEquals(1, createCalls)
        assertEquals(1, saveCalls)
        assertEquals(1, startCalls)
    }

    @Test
    fun appliesReferenceLessCallbackToCurrentAttempt() {
        val observer = FakePaymentResultObserver()
        val updates = mutableListOf<Pair<String, PaymentStatus>>()
        val viewModel = viewModel(
            observer = observer,
            update = { reference, status ->
                updates += reference to status
                UpdatePurchaseStatusUseCase.Result.Updated(reference, status)
            },
        )
        viewModel.start(cart)

        observer.emit(
            PaymentResult(
                reference = "",
                status = PaymentStatus.APPROVED,
                errorMessage = null,
            ),
        )

        assertEquals(
            listOf(REFERENCE to PaymentStatus.APPROVED),
            updates,
        )
        assertEquals(CheckoutPhase.TERMINAL, viewModel.uiState.value.phase)
        assertEquals(
            PaymentStatus.APPROVED,
            viewModel.uiState.value.terminalStatus,
        )
        assertTrue(viewModel.uiState.value.receiptNavigationPending)

        viewModel.reset()

        assertTrue(viewModel.uiState.value.receiptNavigationPending)

        viewModel.consumeReceiptNavigation()

        assertFalse(viewModel.uiState.value.receiptNavigationPending)
    }

    @Test
    fun ignoresCallbackForAnotherAttempt() {
        val observer = FakePaymentResultObserver()
        var updateCalls = 0
        val viewModel = viewModel(
            observer = observer,
            update = { reference, status ->
                updateCalls += 1
                UpdatePurchaseStatusUseCase.Result.Updated(reference, status)
            },
        )
        viewModel.start(cart)

        observer.emit(
            PaymentResult(
                reference = "another-reference",
                status = PaymentStatus.DENIED,
                errorMessage = null,
            ),
        )

        assertEquals(0, updateCalls)
        assertEquals(CheckoutPhase.PROCESSING, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.terminalStatus)
    }

    @Test
    fun mapsGatewayFailuresWithoutClearingCurrentCart() {
        val viewModel = viewModel(
            start = {
                StartPaymentUseCase.Result.AppNotAvailable(it.reference)
            },
        )

        viewModel.start(cart)

        assertEquals(CheckoutPhase.ERROR, viewModel.uiState.value.phase)
        assertEquals(
            CheckoutError.APP_NOT_AVAILABLE,
            viewModel.uiState.value.error,
        )
    }

    @Test
    fun stopsCallbackObserverWhenViewModelIsCleared() {
        val observer = FakePaymentResultObserver()
        val store = ViewModelStore()
        val viewModel = viewModel(observer = observer)
        store.put("checkout", viewModel)

        store.clear()

        assertTrue(observer.stopped)
    }

    private fun viewModel(
        observer: FakePaymentResultObserver = FakePaymentResultObserver(),
        create: (Cart) -> PurchaseAttempt = { attempt },
        save: suspend (PurchaseAttempt) -> SavePurchaseAttemptUseCase.Result = {
            SavePurchaseAttemptUseCase.Result.Saved(it)
        },
        start: suspend (PurchaseAttempt) -> StartPaymentUseCase.Result = {
            StartPaymentUseCase.Result.Started(it.reference)
        },
        update: suspend (
            String,
            PaymentStatus,
        ) -> UpdatePurchaseStatusUseCase.Result = { reference, status ->
            UpdatePurchaseStatusUseCase.Result.Updated(reference, status)
        },
    ): CheckoutViewModel = CheckoutViewModel(
        createPurchaseAttempt = object : CreatePurchaseAttemptUseCase {
            override fun invoke(cart: Cart): PurchaseAttempt = create(cart)
        },
        savePurchaseAttempt = object : SavePurchaseAttemptUseCase {
            override suspend fun invoke(
                attempt: PurchaseAttempt,
            ): SavePurchaseAttemptUseCase.Result = save(attempt)
        },
        startPayment = object : StartPaymentUseCase {
            override suspend fun invoke(
                attempt: PurchaseAttempt,
            ): StartPaymentUseCase.Result = start(attempt)
        },
        updatePurchaseStatus = object : UpdatePurchaseStatusUseCase {
            override suspend fun invoke(
                reference: String,
                newStatus: PaymentStatus,
            ): UpdatePurchaseStatusUseCase.Result = update(reference, newStatus)
        },
        paymentResultObserver = observer,
    )

    private class FakePaymentResultObserver : PaymentResultObserver {
        var started = false
        var stopped = false
        private var listener: ((PaymentResult) -> Unit)? = null

        override fun start(onResult: (PaymentResult) -> Unit) {
            started = true
            listener = onResult
        }

        override fun stop() {
            stopped = true
            listener = null
        }

        fun emit(result: PaymentResult) {
            requireNotNull(listener)(result)
        }
    }

    private companion object {
        const val REFERENCE = "checkout-reference"
    }
}
