package br.com.amandaluz.cielotickets.feature.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.ItemCartEventBinding
import br.com.amandaluz.cielotickets.ui.quantity.QuantitySelectorUiModel

class CartItemAdapter(
    private val onAdd: (String) -> Unit,
    private val onRemove: (String) -> Unit,
) : ListAdapter<CartItemUiModel, CartItemAdapter.CartItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CartItemViewHolder = CartItemViewHolder(
        ItemCartEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartItemViewHolder(
        private val binding: ItemCartEventBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItemUiModel) = with(binding) {
            cartItemName.text = item.name
            cartItemUnitPrice.text = item.unitPriceLabel
            cartItemSubtotal.text = item.subtotal
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
                onAdd = { onAdd(item.eventId) },
                onRemove = { onRemove(item.eventId) },
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CartItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: CartItemUiModel,
            newItem: CartItemUiModel,
        ): Boolean = oldItem.eventId == newItem.eventId

        override fun areContentsTheSame(
            oldItem: CartItemUiModel,
            newItem: CartItemUiModel,
        ): Boolean = oldItem == newItem
    }
}
