package br.com.amandaluz.cielotickets.feature.checkout

interface ActivePaymentCoordinator {
    suspend fun activate(reference: String): Boolean

    fun clear(reference: String)
}
