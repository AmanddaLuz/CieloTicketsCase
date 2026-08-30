package br.com.amandaluz.cielotickets.feature.checkout.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.BottomSheetCartBinding
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutError
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutPhase
import br.com.amandaluz.cielotickets.feature.checkout.CheckoutUiState
import br.com.amandaluz.cielotickets.feature.checkout.viewmodel.CheckoutViewModel
import br.com.amandaluz.cielotickets.feature.checkout.view.adapter.CartItemAdapter
import br.com.amandaluz.cielotickets.feature.events.CartUiModel
import br.com.amandaluz.cielotickets.feature.events.view.EventsFragment
import br.com.amandaluz.cielotickets.feature.events.viewmodel.EventsViewModel
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.lifecycle.launchWhenViewStarted
import br.com.amandaluz.cielotickets.ui.state.StatePanelUiModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartBottomSheetFragment : BottomSheetDialogFragment(R.layout.bottom_sheet_cart) {
    private val binding by viewBinding(BottomSheetCartBinding::bind)
    private val viewModel: EventsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireParentFragment())[EventsViewModel::class.java]
    }
    private val checkoutViewModel: CheckoutViewModel by lazy(
        LazyThreadSafetyMode.NONE,
    ) {
        (requireParentFragment() as EventsFragment).checkoutViewModel
    }
    private val cartAdapter = CartItemAdapter(
        onAdd = { viewModel.addTicket(it) },
        onRemove = { viewModel.removeTicket(it) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cartItems.adapter = cartAdapter
        binding.clearCartButton.setOnClickListener { viewModel.clearCart() }
        binding.creditCashButton.setOnClickListener {
            viewModel.checkoutCart?.let {
                checkoutViewModel.start(it, PaymentMethod.CREDIT_CASH)
            }
        }
        binding.debitCashButton.setOnClickListener {
            viewModel.checkoutCart?.let {
                checkoutViewModel.start(it, PaymentMethod.DEBIT_CASH)
            }
        }

        launchWhenViewStarted {
            launch {
                viewModel.uiState.collectLatest { state ->
                    renderCart(state.cart)
                }
            }
            launch {
                checkoutViewModel.uiState.collectLatest(::renderCheckout)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.setCartOpen(false)
        checkoutViewModel.reset()
    }

    private fun renderCart(cart: CartUiModel?) {
        val checkoutActive =
            checkoutViewModel.uiState.value.phase != CheckoutPhase.IDLE
        if (cart == null && !checkoutActive) {
            dismiss()
            return
        }
        cartAdapter.submitList(cart?.items.orEmpty())
        binding.cartTotal.text = cart?.totalPrice.orEmpty()
        binding.creditCashButton.isEnabled = cart != null
        binding.debitCashButton.isEnabled = cart != null
    }

    private fun renderCheckout(state: CheckoutUiState) = with(binding) {
        val showingCart = state.phase == CheckoutPhase.IDLE
        cartTitle.isVisible = showingCart
        clearCartButton.isVisible = showingCart
        cartItems.isVisible = showingCart
        totalLabel.isVisible = showingCart
        cartTotal.isVisible = showingCart
        creditCashButton.isVisible = showingCart
        debitCashButton.isVisible = showingCart
        checkoutState.isVisible = !showingCart
        checkoutState.render(
            model = state.toPanelModel(),
            onAction = ::dismiss,
        )
    }

    private fun CheckoutUiState.toPanelModel(): StatePanelUiModel? = when (phase) {
        CheckoutPhase.IDLE -> null
        CheckoutPhase.STARTING -> StatePanelUiModel.Loading(
            getString(R.string.checkout_starting),
        )
        CheckoutPhase.PROCESSING -> StatePanelUiModel.Loading(
            getString(R.string.checkout_processing),
        )
        CheckoutPhase.TERMINAL -> terminalStatus.toPanelModel(callbackMessage)
        CheckoutPhase.ERROR -> StatePanelUiModel.Message(
            title = getString(R.string.checkout_error_title),
            message = getString(error.toMessageRes()),
            iconRes = R.drawable.ic_payment_error,
            actionLabel = getString(R.string.close),
            iconTintRes = R.color.status_error,
        )
    }

    private fun PaymentStatus?.toPanelModel(
        callbackMessage: String?,
    ): StatePanelUiModel.Message {
        val (titleRes, messageRes) = terminalText()
        return StatePanelUiModel.Message(
            title = getString(titleRes),
            message = callbackMessage?.takeIf(String::isNotBlank)
                ?: getString(messageRes),
            iconRes = terminalIconRes(),
            actionLabel = getString(R.string.close),
            iconTintRes = terminalColorRes(),
        )
    }

    private fun PaymentStatus?.terminalIconRes(): Int = when (this) {
        PaymentStatus.CANCELLED -> R.drawable.ic_payment_cancelled
        PaymentStatus.DENIED,
        PaymentStatus.ERROR,
        null,
        -> R.drawable.ic_payment_error
        else -> R.drawable.ic_ticket
    }

    private fun PaymentStatus?.terminalColorRes(): Int = when (this) {
        PaymentStatus.CANCELLED -> R.color.status_cancelled
        PaymentStatus.DENIED,
        PaymentStatus.ERROR,
        null,
        -> R.color.status_error
        else -> R.color.cielo_primary
    }

    private fun PaymentStatus?.terminalText(): Pair<Int, Int> = when (this) {
        PaymentStatus.APPROVED -> {
            R.string.checkout_approved_title to R.string.checkout_approved_message
        }
        PaymentStatus.DENIED -> {
            R.string.checkout_denied_title to R.string.checkout_denied_message
        }
        PaymentStatus.CANCELLED -> {
            R.string.checkout_cancelled_title to R.string.checkout_cancelled_message
        }
        else -> {
            R.string.checkout_error_title to
                R.string.checkout_callback_error_message
        }
    }

    private fun CheckoutError?.toMessageRes(): Int = when (this) {
        CheckoutError.APP_NOT_AVAILABLE -> R.string.checkout_app_unavailable
        CheckoutError.CREDENTIALS_NOT_CONFIGURED -> {
            R.string.checkout_credentials_missing
        }
        CheckoutError.DUPLICATE_REFERENCE,
        CheckoutError.INVALID_INITIAL_STATUS,
        CheckoutError.TECHNICAL_FAILURE,
        CheckoutError.ATTEMPT_NOT_FOUND,
        CheckoutError.INVALID_STATUS,
        CheckoutError.CALLBACK_REJECTED,
        null,
        -> R.string.checkout_technical_error
    }
}
