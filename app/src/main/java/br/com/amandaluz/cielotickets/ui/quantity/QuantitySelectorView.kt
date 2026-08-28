package br.com.amandaluz.cielotickets.ui.quantity

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import br.com.amandaluz.cielotickets.databinding.ViewQuantitySelectorBinding

class QuantitySelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewQuantitySelectorBinding.inflate(
        LayoutInflater.from(context),
        this,
    )

    fun render(
        model: QuantitySelectorUiModel,
        onAdd: () -> Unit,
        onRemove: () -> Unit,
    ) = with(binding) {
        quantityText.text = model.quantity.toString()
        addButton.isEnabled = model.canAdd
        removeButton.isEnabled = model.canRemove
        addButton.contentDescription = model.addContentDescription
        removeButton.contentDescription = model.removeContentDescription
        addButton.setOnClickListener { onAdd() }
        removeButton.setOnClickListener { onRemove() }
    }
}

data class QuantitySelectorUiModel(
    val quantity: Int,
    val canAdd: Boolean,
    val canRemove: Boolean,
    val addContentDescription: String,
    val removeContentDescription: String,
)
