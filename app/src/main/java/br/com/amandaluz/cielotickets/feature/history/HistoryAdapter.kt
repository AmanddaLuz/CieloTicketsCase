package br.com.amandaluz.cielotickets.feature.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.ItemHistoryBinding
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus

class HistoryAdapter(
    private val onSaleClick: (String) -> Unit,
) : ListAdapter<HistoryItemUiModel, HistoryAdapter.HistoryViewHolder>(Diff) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HistoryViewHolder = HistoryViewHolder(
        ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItemUiModel) = with(binding) {
            historyTitle.text = item.singleEventName ?: root.resources.getQuantityString(
                R.plurals.event_count,
                item.eventCount,
                item.eventCount,
            )
            historyQuantity.text = root.resources.getQuantityString(
                R.plurals.ticket_count,
                item.totalQuantity,
                item.totalQuantity,
            )
            historyDate.text = item.date
            historyTotal.text = item.totalPrice
            historyStatus.setText(item.status.labelRes())
            historyStatus.setTextColor(
                root.context.getColor(item.status.colorRes()),
            )
            root.setOnClickListener { onSaleClick(item.reference) }
        }
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

    private object Diff : DiffUtil.ItemCallback<HistoryItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: HistoryItemUiModel,
            newItem: HistoryItemUiModel,
        ): Boolean = oldItem.reference == newItem.reference

        override fun areContentsTheSame(
            oldItem: HistoryItemUiModel,
            newItem: HistoryItemUiModel,
        ): Boolean = oldItem == newItem
    }
}
