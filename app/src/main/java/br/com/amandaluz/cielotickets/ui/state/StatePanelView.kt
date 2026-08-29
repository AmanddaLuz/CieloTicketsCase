package br.com.amandaluz.cielotickets.ui.state

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.ViewStatePanelBinding

/**
 * Componente passivo e reutilizável para estados de carregamento, mensagem e
 * ação, configurado integralmente por [StatePanelUiModel].
 */
class StatePanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewStatePanelBinding.inflate(
        LayoutInflater.from(context),
        this,
    )

    /**
     * Renderiza o modelo sem conhecer regras da feature chamadora.
     *
     * A ação só é exibida quando o modelo possui rótulo e o chamador fornece
     * um callback.
     */
    fun render(
        model: StatePanelUiModel?,
        onAction: (() -> Unit)? = null,
    ) {
        isVisible = model != null
        when (model) {
            null -> Unit
            is StatePanelUiModel.Loading -> renderLoading(model)
            is StatePanelUiModel.Message -> renderMessage(model, onAction)
        }
    }

    private fun renderLoading(model: StatePanelUiModel.Loading) = with(binding) {
        progressIndicator.isVisible = true
        stateIcon.isVisible = false
        stateTitle.isVisible = false
        stateMessage.text = model.message
        stateAction.isVisible = false
        stateAction.setOnClickListener(null)
    }

    private fun renderMessage(
        model: StatePanelUiModel.Message,
        onAction: (() -> Unit)?,
    ) = with(binding) {
        progressIndicator.isVisible = false
        stateIcon.isVisible = true
        stateIcon.setImageResource(model.iconRes)
        ImageViewCompat.setImageTintList(
            stateIcon,
            ContextCompat.getColorStateList(
                context,
                model.iconTintRes ?: R.color.cielo_primary,
            ),
        )
        stateTitle.isVisible = true
        stateTitle.text = model.title
        stateMessage.text = model.message
        stateAction.isVisible = model.actionLabel != null && onAction != null
        stateAction.text = model.actionLabel
        stateAction.setOnClickListener(
            if (onAction == null) null else {
                { onAction() }
            },
        )
    }
}
