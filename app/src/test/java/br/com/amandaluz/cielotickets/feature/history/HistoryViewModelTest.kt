package br.com.amandaluz.cielotickets.feature.history

import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.feature.history.usecase.GetSalesHistoryUseCase
import br.com.amandaluz.cielotickets.feature.history.viewmodel.HistoryViewModel
import br.com.amandaluz.cielotickets.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun filtersPersistedHistoryBySelectedStatus() = runTest {
        val history = MutableStateFlow(
            listOf(
                attempt("approved", PaymentStatus.APPROVED),
                attempt("denied", PaymentStatus.DENIED),
            ),
        )
        val viewModel = HistoryViewModel(
            getSalesHistory = object : GetSalesHistoryUseCase {
                override fun invoke(): Flow<List<PurchaseAttempt>> = history
            },
            uiMapper = HistoryUiMapper(
                formatCurrency = { "$it cents" },
                formatDate = { "$it date" },
            ),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.selectFilter(HistoryStatusFilter.APPROVED)

        val state = viewModel.uiState.value
        assertEquals(HistoryStatusFilter.APPROVED, state.selectedFilter)
        assertEquals(listOf("approved"), state.sales.map { it.reference })
    }

    @Test
    fun mapsMultiEventSummaryWithoutLosingDomainTotals() = runTest {
        val history = MutableStateFlow(
            listOf(
                PurchaseAttempt.restore(
                    reference = "multi",
                    items = listOf(
                        PurchaseItem("one", "Show", 2, 1_000L),
                        PurchaseItem("two", "Teatro", 1, 2_000L),
                    ),
                    status = PaymentStatus.PROCESSING,
                    paymentMethod = PaymentMethod.CREDIT_CASH,
                    createdAt = 100L,
                    updatedAt = 200L,
                ),
            ),
        )
        val viewModel = HistoryViewModel(
            getSalesHistory = object : GetSalesHistoryUseCase {
                override fun invoke(): Flow<List<PurchaseAttempt>> = history
            },
            uiMapper = HistoryUiMapper(
                formatCurrency = { "$it cents" },
                formatDate = { "$it date" },
            ),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val item = viewModel.uiState.value.sales.single()
        assertEquals(2, item.eventCount)
        assertEquals(3, item.totalQuantity)
        assertEquals("4000 cents", item.totalPrice)
    }

    private fun attempt(
        reference: String,
        status: PaymentStatus,
    ): PurchaseAttempt = PurchaseAttempt.restore(
        reference = reference,
        items = listOf(PurchaseItem("event-$reference", "Event", 1, 1_000L)),
        status = status,
        paymentMethod = PaymentMethod.CREDIT_CASH,
        createdAt = 100L,
        updatedAt = 200L,
    )
}
