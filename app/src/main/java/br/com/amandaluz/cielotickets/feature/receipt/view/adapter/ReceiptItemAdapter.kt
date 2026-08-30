package br.com.amandaluz.cielotickets.feature.receipt.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.ItemReceiptBinding
import br.com.amandaluz.cielotickets.feature.receipt.ReceiptItemUiModel

/**
 * Renderiza os snapshots de eventos que compõem o comprovante.
 *
 * A identidade do `DiffUtil` usa `eventId`; textos visíveis não são tratados
 * como chaves porque podem se repetir ou mudar sem representar outro item.
 */
class ReceiptItemAdapter :
    ListAdapter<ReceiptItemUiModel, ReceiptItemAdapter.ReceiptViewHolder>(Diff) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ReceiptViewHolder = ReceiptViewHolder(
        ItemReceiptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReceiptViewHolder(
        private val binding: ItemReceiptBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReceiptItemUiModel) = with(binding) {
            receiptEventName.text = item.eventName
            receiptItemQuantity.text = root.resources.getQuantityString(
                R.plurals.ticket_count,
                item.quantity,
                item.quantity,
            )
            receiptUnitPrice.text = root.resources.getString(
                R.string.unit_price_value,
                item.unitPrice,
            )
            receiptSubtotal.text = item.subtotal
        }
    }

    private object Diff : DiffUtil.ItemCallback<ReceiptItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: ReceiptItemUiModel,
            newItem: ReceiptItemUiModel,
        ): Boolean = oldItem.eventId == newItem.eventId

        override fun areContentsTheSame(
            oldItem: ReceiptItemUiModel,
            newItem: ReceiptItemUiModel,
        ): Boolean = oldItem == newItem
    }
}
