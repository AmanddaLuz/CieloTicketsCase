package br.com.amandaluz.cielotickets.payment.cielo

class CieloCallbackReferenceResolver(
    private val activePaymentStore: CieloActivePaymentStore,
) {
    fun resolve(callback: CieloCallbackResult): CieloCallbackResult =
        if (callback.reference.isNotBlank()) {
            callback
        } else {
            callback.copy(
                reference = activePaymentStore.currentReference().orEmpty(),
            )
        }
}
