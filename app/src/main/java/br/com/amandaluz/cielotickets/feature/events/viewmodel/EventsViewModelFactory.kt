package br.com.amandaluz.cielotickets.feature.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.feature.events.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.feature.events.usecase.GetAvailableEventsUseCase
import br.com.amandaluz.cielotickets.feature.common.BrazilianCurrencyFormatter
import br.com.amandaluz.cielotickets.feature.events.EventsUiMapper

class EventsViewModelFactory(
    private val getAvailableEvents: GetAvailableEventsUseCase,
    private val buildCart: BuildCartUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            "Unsupported ViewModel: ${modelClass.name}"
        }
        val currencyFormatter = BrazilianCurrencyFormatter()
        @Suppress("UNCHECKED_CAST")
        return EventsViewModel(
            getAvailableEvents = getAvailableEvents,
            buildCart = buildCart,
            uiMapper = EventsUiMapper(currencyFormatter::format),
        ) as T
    }
}
