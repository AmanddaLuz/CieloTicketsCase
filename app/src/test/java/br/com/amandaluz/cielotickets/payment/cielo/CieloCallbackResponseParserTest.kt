package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CieloCallbackResponseParserTest {
    private val parser = CieloCallbackResponseParser()

    @Test
    fun mapsCompleteOrderResponseToApproved() {
        assertEquals(
            CieloCallbackResult(
                reference = "reference-1",
                status = PaymentStatus.APPROVED,
                errorMessage = null,
            ),
            parser.parse("""{"reference":"reference-1","id":"order-1"}"""),
        )
    }

    @Test
    fun mapsCancellationWithoutRequiringAReference() {
        assertEquals(
            CieloCallbackResult(
                reference = "",
                status = PaymentStatus.CANCELLED,
                errorMessage = "Cancelado pelo operador",
            ),
            parser.parse("""{"code":1,"reason":"Cancelado pelo operador"}"""),
        )
    }

    @Test
    fun mapsEmulatorErrorCodes() {
        assertEquals(
            PaymentStatus.DENIED,
            parser.parse("""{"code":2}""")?.status,
        )
        assertEquals(
            PaymentStatus.DENIED,
            parser.parse("""{"code":3}""")?.status,
        )
        val unknown = parser.parse("""{"code":99}""")
        assertEquals(PaymentStatus.ERROR, unknown?.status)
        assertEquals("Erro desconhecido", unknown?.errorMessage)
    }

    @Test
    fun rejectsMalformedOrUnidentifiedApproval() {
        assertNull(parser.parse("not-json"))
        assertNull(parser.parse("""{"id":"order-1"}"""))
    }
}
