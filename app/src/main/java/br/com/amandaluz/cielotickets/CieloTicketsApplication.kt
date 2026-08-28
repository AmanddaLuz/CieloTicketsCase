package br.com.amandaluz.cielotickets

import android.app.Application
import br.com.amandaluz.cielotickets.di.AppContainer
import br.com.amandaluz.cielotickets.di.AppContainerImpl

class CieloTicketsApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainerImpl(this)
    }
}

