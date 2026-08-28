package br.com.amandaluz.cielotickets.domain.gateway

import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

/**
 * Fronteira de domínio para iniciar uma cobrança externa.
 *
 * A implementação deve apenas abrir a cobrança. A reivindicação atômica da
 * tentativa e as transições de status pertencem aos casos de uso.
 */
interface PaymentGateway {
    sealed interface Result {
        data object Initiated : Result
        data object AppNotAvailable : Result
        data object CredentialsNotConfigured : Result
        data object TechnicalFailure : Result
    }

    fun initiatePayment(attempt: PurchaseAttempt): Result
}
