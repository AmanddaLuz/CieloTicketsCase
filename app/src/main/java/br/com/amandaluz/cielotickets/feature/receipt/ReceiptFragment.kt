package br.com.amandaluz.cielotickets.feature.receipt

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import br.com.amandaluz.cielotickets.CieloTicketsApplication
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.FragmentReceiptBinding
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.lifecycle.launchWhenViewStarted
import br.com.amandaluz.cielotickets.ui.state.StatePanelUiModel
import kotlinx.coroutines.flow.collectLatest

class ReceiptFragment : Fragment(R.layout.fragment_receipt) {
    private val binding by viewBinding(FragmentReceiptBinding::bind)
    private val reference: String by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(requireArguments().getString(ARG_REFERENCE)) {
            "Missing purchase reference"
        }
    }
    private val viewModel by viewModels<ReceiptViewModel> {
        val container =
            (requireActivity().application as CieloTicketsApplication).appContainer
        ReceiptViewModelFactory(
            reference = reference,
            getPurchaseAttempt = container.getPurchaseAttempt,
            buildTicketQrContent = container.buildTicketQrContent,
        )
    }
    private val receiptItemAdapter = ReceiptItemAdapter()
    private val qrCodeRenderer = QrCodeBitmapRenderer()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setTitle(R.string.receipt_title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.receiptItems.adapter = receiptItemAdapter
        binding.receiptItems.isNestedScrollingEnabled = false

        launchWhenViewStarted {
            viewModel.uiState.collectLatest(::render)
        }
    }

    override fun onDestroyView() {
        binding.receiptItems.adapter = null
        super.onDestroyView()
    }

    private fun render(state: ReceiptUiState) = with(binding) {
        receiptContent.isVisible = state is ReceiptUiState.Content
        statePanel.render(state.toPanelModel())
        if (state is ReceiptUiState.Content) {
            renderReceipt(state.receipt)
        }
    }

    private fun renderReceipt(receipt: ReceiptUiModel) = with(binding) {
        receiptStatus.setText(receipt.status.labelRes())
        receiptStatus.setTextColor(requireContext().getColor(receipt.status.colorRes()))
        receiptDate.text = receipt.date
        receiptReference.text = receipt.reference
        receiptQuantity.text = resources.getQuantityString(
            R.plurals.ticket_count,
            receipt.totalQuantity,
            receipt.totalQuantity,
        )
        receiptTotal.text = receipt.totalPrice
        receiptItemAdapter.submitList(receipt.items)

        qrSection.isVisible = receipt.qrContent != null
        receiptQrCode.setImageBitmap(
            receipt.qrContent?.let {
                qrCodeRenderer.render(it, QR_CODE_SIZE)
            },
        )
    }

    private fun ReceiptUiState.toPanelModel(): StatePanelUiModel? = when (this) {
        ReceiptUiState.Loading -> StatePanelUiModel.Loading(
            getString(R.string.loading_receipt),
        )
        ReceiptUiState.NotFound -> StatePanelUiModel.Message(
            title = getString(R.string.receipt_not_found_title),
            message = getString(R.string.receipt_not_found_message),
            iconRes = R.drawable.ic_ticket,
        )
        is ReceiptUiState.Content -> null
    }

    private fun PaymentStatus.labelRes(): Int = when (this) {
        PaymentStatus.CREATED -> R.string.status_created
        PaymentStatus.PROCESSING -> R.string.status_processing
        PaymentStatus.APPROVED -> R.string.status_approved
        PaymentStatus.DENIED -> R.string.status_denied
        PaymentStatus.CANCELLED -> R.string.status_cancelled
        PaymentStatus.ERROR -> R.string.status_error
    }

    private fun PaymentStatus.colorRes(): Int = when (this) {
        PaymentStatus.APPROVED -> R.color.status_approved
        PaymentStatus.DENIED,
        PaymentStatus.ERROR,
        -> R.color.status_error
        PaymentStatus.CANCELLED -> R.color.status_cancelled
        PaymentStatus.CREATED,
        PaymentStatus.PROCESSING,
        -> R.color.status_processing
    }

    private companion object {
        const val ARG_REFERENCE = "reference"
        const val QR_CODE_SIZE = 512
    }
}
