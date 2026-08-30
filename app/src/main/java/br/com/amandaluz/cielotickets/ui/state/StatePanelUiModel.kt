package br.com.amandaluz.cielotickets.ui.state

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Contrato imutável aceito pelo [StatePanelView].
 *
 * Permite reutilizar o painel sem expor seus componentes internos às features.
 */
sealed interface StatePanelUiModel {
    /** Exibe progresso indeterminado acompanhado de uma mensagem. */
    data class Loading(val message: String) : StatePanelUiModel

    /** Exibe ícone, título, mensagem e uma ação opcional. */
    data class Message(
        val title: String,
        val message: String,
        @DrawableRes val iconRes: Int,
        val actionLabel: String? = null,
        @ColorRes val iconTintRes: Int? = null,
    ) : StatePanelUiModel
}
