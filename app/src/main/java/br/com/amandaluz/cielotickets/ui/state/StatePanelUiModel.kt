package br.com.amandaluz.cielotickets.ui.state

import androidx.annotation.DrawableRes

sealed interface StatePanelUiModel {
    data class Loading(val message: String) : StatePanelUiModel

    data class Message(
        val title: String,
        val message: String,
        @DrawableRes val iconRes: Int,
        val actionLabel: String? = null,
    ) : StatePanelUiModel
}

