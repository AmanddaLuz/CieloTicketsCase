package br.com.amandaluz.cielotickets.feature.receipt

import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.feature.receipt.usecase.BuildTicketQrContentUseCase
import br.com.amandaluz.cielotickets.feature.receipt.usecase.ObservePurchaseAttemptUseCase
import br.com.amandaluz.cielotickets.feature.receipt.viewmodel.ReceiptViewModel
import br.com.amandaluz.cielotickets.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReceiptViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun exposesPersistedReceiptAndApprovedQrContent() {
        val attempt = attempt(PaymentStatus.APPROVED)
        val viewModel = viewModel(attempt)

        val receipt = (viewModel.uiState.value as ReceiptUiState.Content).receipt
        assertEquals(attempt.reference, receipt.reference)
        assertEquals(2, receipt.totalQuantity)
        assertEquals("7000 cents", receipt.totalPrice)
        assertEquals("ticket:${attempt.reference}", receipt.qrContent)
        assertEquals("event-1", receipt.items.single().eventId)
    }

    @Test
    fun hidesQrContentForDeniedReceipt() {
        val viewModel = viewModel(attempt(PaymentStatus.DENIED))

        val receipt = (viewModel.uiState.value as ReceiptUiState.Content).receipt
        assertNull(receipt.qrContent)
    }

    @Test
    fun exposesNotFoundForUnknownReference() {
        val viewModel = viewModel(null)

        assertTrue(viewModel.uiState.value is ReceiptUiState.NotFound)
    }

    @Test
    fun updatesReceiptWhenPersistedStatusChanges() {
        val attempts = MutableStateFlow<PurchaseAttempt?>(
            attempt(PaymentStatus.PROCESSING),
        )
        val viewModel = viewModel(attempts)

        attempts.value = attempt(PaymentStatus.APPROVED)

        val receipt = (viewModel.uiState.value as ReceiptUiState.Content).receipt
        assertEquals(PaymentStatus.APPROVED, receipt.status)
        assertEquals("ticket:${receipt.reference}", receipt.qrContent)
    }

    private fun viewModel(attempt: PurchaseAttempt?): ReceiptViewModel =
        viewModel(MutableStateFlow(attempt))

    private fun viewModel(
        attempts: Flow<PurchaseAttempt?>,
    ): ReceiptViewModel =
        ReceiptViewModel(
            reference = "receipt-reference",
            observePurchaseAttempt = object : ObservePurchaseAttemptUseCase {
                override fun invoke(reference: String): Flow<PurchaseAttempt?> =
                    attempts
            },
            uiMapper = ReceiptUiMapper(
                formatCurrency = { "$it cents" },
                formatDate = { "$it date" },
                buildTicketQrContent = object : BuildTicketQrContentUseCase {
                    override fun invoke(attempt: PurchaseAttempt): String? =
                        if (attempt.status == PaymentStatus.APPROVED) {
                            "ticket:${attempt.reference}"
                        } else {
                            null
                        }
                },
            ),
        )

    private fun attempt(status: PaymentStatus): PurchaseAttempt =
        PurchaseAttempt.restore(
            reference = "receipt-reference",
            items = listOf(
                PurchaseItem("event-1", "Festival", 2, 3_500L),
            ),
            status = status,
            paymentMethod = PaymentMethod.CREDIT_CASH,
            createdAt = 100L,
            updatedAt = 200L,
        )
}
