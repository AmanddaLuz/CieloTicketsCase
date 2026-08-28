package br.com.amandaluz.cielotickets.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.amandaluz.cielotickets.domain.usecase.BuildCartUseCase
import br.com.amandaluz.cielotickets.domain.usecase.GetAvailableEventsUseCase

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
