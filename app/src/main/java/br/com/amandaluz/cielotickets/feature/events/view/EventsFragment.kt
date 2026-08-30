package br.com.amandaluz.cielotickets.feature.events.view

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.FragmentEventsBinding
import br.com.amandaluz.cielotickets.feature.checkout.view.CartBottomSheetFragment
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutPhase
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutUiState
import br.com.amandaluz.cielotickets.feature.checkout.viewmodel.CheckoutViewModel
import br.com.amandaluz.cielotickets.feature.checkout.viewmodel.CheckoutViewModelFactory
import br.com.amandaluz.cielotickets.feature.events.view.adapter.EventAdapter
import br.com.amandaluz.cielotickets.feature.events.EventsUiError
import br.com.amandaluz.cielotickets.feature.events.EventsUiState
import br.com.amandaluz.cielotickets.feature.events.viewmodel.EventsViewModel
import br.com.amandaluz.cielotickets.feature.events.viewmodel.EventsViewModelFactory
import br.com.amandaluz.cielotickets.payment.cielo.CieloPaymentResultObserverImpl
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.lifecycle.launchWhenViewStarted
import br.com.amandaluz.cielotickets.ui.state.StatePanelUiModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventsFragment : Fragment(R.layout.fragment_events) {
    private val binding by viewBinding(FragmentEventsBinding::bind)
    private val viewModelFactory: EventsViewModelFactory by lazy {
        val container =
            (requireActivity().application as CieloTicketsApplication).appContainer
        EventsViewModelFactory(
            getAvailableEvents = container.getAvailableEvents,
            buildCart = container.buildCart,
        )
    }
    private val viewModel by viewModels<EventsViewModel> { viewModelFactory }
    internal val checkoutViewModel: CheckoutViewModel by lazy(
        LazyThreadSafetyMode.NONE,
    ) {
        val container =
            (requireActivity().application as CieloTicketsApplication).appContainer
        val factory = CheckoutViewModelFactory(
            createPurchaseAttempt = container.createPurchaseAttempt,
            savePurchaseAttempt = container.savePurchaseAttempt,
            startPayment = container.startPayment,
            updatePurchaseStatus = container.updatePurchaseStatus,
            paymentResultObserver = CieloPaymentResultObserverImpl(
                requireContext(),
            ),
        )
        ViewModelProvider(this, factory)[CheckoutViewModel::class.java]
    }
    private val eventAdapter = EventAdapter(
        onAdd = { viewModel.addTicket(it) },
        onRemove = { viewModel.removeTicket(it) },
    )

    override val defaultViewModelProviderFactory
        get() = viewModelFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()

        launchWhenViewStarted {
            launch {
                viewModel.uiState.collectLatest(::render)
            }
            launch {
                checkoutViewModel.uiState.collectLatest(::handleCheckout)
            }
        }
    }

    private fun setView() {
        binding.toolbar.setTitle(R.string.events_title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.eventsList.adapter = eventAdapter
        binding.cartButton.setOnClickListener { viewModel.setCartOpen(true) }
    }

    private fun render(state: EventsUiState) = with(binding) {
        eventAdapter.submitList(state.events)
        eventsList.isVisible = !state.isLoading && state.events.isNotEmpty()
        statePanel.render(state.toPanelModel())
        cartSummary.isVisible = state.cart != null
        state.cart?.let { cart ->
            cartQuantity.text = cart.totalQuantityLabel
            cartTotal.text = cart.totalPrice
        }
        renderCartSheet(state.isCartOpen)
        state.error?.let(::showError)
        openApprovedReceiptIfReady()
    }

    private fun handleCheckout(state: CheckoutUiState) {
        if (state.phase == CheckoutPhase.TERMINAL &&
            viewModel.checkoutCart != null
        ) {
            viewModel.completeCheckout()
        }
        openApprovedReceiptIfReady()
    }

    private fun openApprovedReceiptIfReady() {
        val checkoutState = checkoutViewModel.uiState.value
        val navController = findNavController()
        val reference = checkoutState.reference
        val canNavigate = reference != null &&
            viewModel.uiState.value.cart == null &&
            checkoutState.receiptNavigationPending &&
            navController.currentDestination?.id == R.id.eventsFragment
        if (!canNavigate) return

        navController.navigate(
            R.id.action_events_to_receipt,
            bundleOf(RECEIPT_REFERENCE_ARGUMENT to requireNotNull(reference)),
        )
        checkoutViewModel.consumeReceiptNavigation()
    }

    private fun renderCartSheet(isOpen: Boolean) {
        val current = childFragmentManager.findFragmentByTag(CART_SHEET_TAG)
        when {
            isOpen && current == null -> {
                CartBottomSheetFragment().show(
                    childFragmentManager,
                    CART_SHEET_TAG,
                )
            }
            !isOpen && current is CartBottomSheetFragment -> current.dismiss()
        }
    }

    private fun showError(error: EventsUiError) {
        val message = when (error) {
            EventsUiError.CART_CHANGED -> R.string.cart_changed_error
            EventsUiError.AMOUNT_OVERFLOW -> R.string.cart_amount_overflow_error
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        viewModel.consumeError()
    }

    private fun EventsUiState.toPanelModel(): StatePanelUiModel? = when {
        isLoading -> StatePanelUiModel.Loading(getString(R.string.loading_events))
        events.isEmpty() -> StatePanelUiModel.Message(
            title = getString(R.string.empty_events_title),
            message = getString(R.string.empty_events_message),
            iconRes = R.drawable.ic_ticket,
        )
        else -> null
    }

    private companion object {
        const val CART_SHEET_TAG = "cart-sheet"
        const val RECEIPT_REFERENCE_ARGUMENT = "reference"
    }
}
