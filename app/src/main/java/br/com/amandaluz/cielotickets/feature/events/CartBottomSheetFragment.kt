package br.com.amandaluz.cielotickets.feature.events

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.BottomSheetCartBinding
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.lifecycle.launchWhenViewStarted
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest

class CartBottomSheetFragment : BottomSheetDialogFragment(R.layout.bottom_sheet_cart) {
    private val binding by viewBinding(BottomSheetCartBinding::bind)
    private val viewModel: EventsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireParentFragment())[EventsViewModel::class.java]
    }
    private val cartAdapter = CartItemAdapter(
        onAdd = { viewModel.addTicket(it) },
        onRemove = { viewModel.removeTicket(it) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cartItems.adapter = cartAdapter
        binding.clearCartButton.setOnClickListener { viewModel.clearCart() }

        launchWhenViewStarted {
            viewModel.uiState.collectLatest { state ->
                render(state.cart)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.closeCart()
    }

    private fun render(cart: CartUiModel?) {
        if (cart == null) {
            dismiss()
            return
        }
        cartAdapter.submitList(cart.items)
        binding.cartTotal.text = cart.totalPrice
        binding.checkoutButton.isVisible = false
    }
}
