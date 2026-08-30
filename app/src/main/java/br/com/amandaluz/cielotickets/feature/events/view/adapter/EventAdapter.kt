package br.com.amandaluz.cielotickets.feature.events.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.ItemEventBinding
import br.com.amandaluz.cielotickets.ui.quantity.QuantitySelectorUiModel
import androidx.core.view.isVisible
import br.com.amandaluz.cielotickets.feature.events.EventItemUiModel

class EventAdapter(
    private val onAdd: (String) -> Unit,
    private val onRemove: (String) -> Unit,
) : ListAdapter<EventItemUiModel, EventAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder =
        EventViewHolder(
            ItemEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventItemUiModel) = with(binding) {
            eventName.text = item.name
            eventDetails.text = item.venueAndDate
            eventPrice.text = item.price
            eventSubtotal.isVisible = item.subtotal != null
            eventSubtotal.text = item.subtotal?.let { subtotal ->
                root.context.getString(R.string.subtotal_value, subtotal)
            }
            quantitySelector.render(
                model = QuantitySelectorUiModel(
                    quantity = item.quantity,
                    canAdd = item.canAdd,
                    canRemove = item.canRemove,
                    addContentDescription = root.context.getString(
                        R.string.add_ticket_for,
                        item.name,
                    ),
                    removeContentDescription = root.context.getString(
                        R.string.remove_ticket_for,
                        item.name,
                    ),
                ),
                onAdd = { onAdd(item.id) },
                onRemove = { onRemove(item.id) },
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<EventItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: EventItemUiModel,
            newItem: EventItemUiModel,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: EventItemUiModel,
            newItem: EventItemUiModel,
        ): Boolean = oldItem == newItem
    }
}
