package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CieloCallbackReferenceResolverTest {

    @Test
    fun keepsReferenceProvidedByCielo() {
        val resolver = CieloCallbackReferenceResolver(
            FakeActivePaymentStore("active-reference"),
        )
        val callback = callback(reference = "callback-reference")

        assertEquals(callback, resolver.resolve(callback))
    }

    @Test
    fun usesPersistedActiveReferenceWhenCallbackOmitsIt() {
        val resolver = CieloCallbackReferenceResolver(
            FakeActivePaymentStore("active-reference"),
        )

        assertEquals(
            callback(reference = "active-reference"),
            resolver.resolve(callback(reference = "")),
        )
    }

    private fun callback(reference: String) = CieloCallbackResult(
        reference = reference,
        status = PaymentStatus.DENIED,
        errorMessage = "Pagamento negado",
    )

    private class FakeActivePaymentStore(
        private val reference: String?,
    ) : CieloActivePaymentStore {
        override fun claim(reference: String): Boolean = false

        override fun currentReference(): String? = reference

        override fun replace(
            expectedReference: String,
            newReference: String,
        ): Boolean = false

        override fun clear(reference: String) = Unit
    }
}
