package br.com.amandaluz.cielotickets.feature.history.view

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.FragmentHistoryBinding
import br.com.amandaluz.cielotickets.feature.history.view.adapter.HistoryAdapter
import br.com.amandaluz.cielotickets.feature.history.HistoryStatusFilter
import br.com.amandaluz.cielotickets.feature.history.HistoryUiState
import br.com.amandaluz.cielotickets.feature.history.viewmodel.HistoryViewModel
import br.com.amandaluz.cielotickets.feature.history.viewmodel.HistoryViewModelFactory
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.lifecycle.launchWhenViewStarted
import br.com.amandaluz.cielotickets.ui.state.StatePanelUiModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Renderiza o histórico persistido e aplica um filtro único por status.
 *
 * Os Chips representam o conjunto fechado da máquina de estados e apenas
 * despacham a seleção; a filtragem ocorre no [HistoryViewModel].
 */
class HistoryFragment : Fragment(R.layout.fragment_history) {
    private val binding by viewBinding(FragmentHistoryBinding::bind)
    private val viewModel by viewModels<HistoryViewModel> {
        val container =
            (requireActivity().application as CieloTicketsApplication).appContainer
        HistoryViewModelFactory(container.getSalesHistory)
    }
    private val historyAdapter = HistoryAdapter(::openReceipt)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
        bindFilters()

        launchWhenViewStarted {
            viewModel.uiState.collectLatest(::render)
        }
    }

    private fun setView() {
        binding.toolbar.setTitle(R.string.history_title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_history_to_home)
        }
        binding.historyList.adapter = historyAdapter
    }

    override fun onDestroyView() {
        binding.historyList.adapter = null
        super.onDestroyView()
    }

    private fun bindFilters() {
        binding.statusFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.firstOrNull()
                ?.let(FILTERS::get)
                ?.let(viewModel::selectFilter)
        }
    }

    private fun render(state: HistoryUiState) = with(binding) {
        historyAdapter.submitList(state.sales)
        historyList.isVisible = !state.isLoading && state.sales.isNotEmpty()
        statusFilters.check(state.selectedFilter.chipId())
        statePanel.render(state.toPanelModel())
    }

    private fun HistoryUiState.toPanelModel(): StatePanelUiModel? = when {
        isLoading -> StatePanelUiModel.Loading(
            getString(R.string.loading_history),
        )
        sales.isEmpty() -> StatePanelUiModel.Message(
            title = getString(
                if (selectedFilter == HistoryStatusFilter.ALL) {
                    R.string.empty_history_title
                } else {
                    R.string.empty_filtered_history_title
                },
            ),
            message = getString(
                if (selectedFilter == HistoryStatusFilter.ALL) {
                    R.string.empty_history_message
                } else {
                    R.string.empty_filtered_history_message
                },
            ),
            iconRes = R.drawable.ic_history,
        )
        else -> null
    }

    private fun openReceipt(reference: String) {
        findNavController().navigate(
            R.id.action_history_to_receipt,
            bundleOf(RECEIPT_REFERENCE_ARGUMENT to reference),
        )
    }

    private fun HistoryStatusFilter.chipId(): Int =
        FILTERS.entries.first { it.value == this }.key

    private companion object {
        const val RECEIPT_REFERENCE_ARGUMENT = "reference"

        val FILTERS = mapOf(
            R.id.filterAll to HistoryStatusFilter.ALL,
            R.id.filterApproved to HistoryStatusFilter.APPROVED,
            R.id.filterDenied to HistoryStatusFilter.DENIED,
            R.id.filterCancelled to HistoryStatusFilter.CANCELLED,
            R.id.filterError to HistoryStatusFilter.ERROR,
            R.id.filterProcessing to HistoryStatusFilter.PROCESSING,
            R.id.filterCreated to HistoryStatusFilter.CREATED,
        )
    }
}
