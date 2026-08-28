package br.com.amandaluz.cielotickets.payment.cielo

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CieloCallbackResponseParserTest {
    private val parser = CieloCallbackResponseParser()

    @Test
    fun mapsOrderResponseToApproved() {
        assertEquals(
            CieloCallbackResult(
                reference = "reference-1",
                status = PaymentStatus.APPROVED,
                paidAmountInCents = 6_000L,
                errorMessage = null,
            ),
            parser.parse(
                rawResponse = approvedResponse("reference-1"),
                fallbackReference = "reference-1",
            ),
        )
    }

    @Test
    fun usesCallbackReferenceForErrorResponse() {
        assertEquals(
            CieloCallbackResult(
                reference = "reference-1",
                status = PaymentStatus.CANCELLED,
                paidAmountInCents = null,
                errorMessage = "Cancelado pelo operador",
            ),
            parser.parse(
                rawResponse = """{"code":1,"reason":"Cancelado pelo operador"}""",
                fallbackReference = "reference-1",
            ),
        )
    }

    @Test
    fun rejectsMalformedOrUnidentifiedResponses() {
        assertNull(parser.parse("not-json", "reference-1"))
        assertNull(parser.parse("""{"code":2}""", null))
    }

    @Test
    fun rejectsUncorrelatedOrIncompleteApproval() {
        assertNull(
            parser.parse(
                approvedResponse("different-reference"),
                "reference-1",
            ),
        )
        assertNull(parser.parse("""{"reference":"reference-1"}""", "reference-1"))
    }

    @Test
    fun mapsGenericErrorToTechnicalError() {
        assertEquals(
            PaymentStatus.ERROR,
            parser.parse(
                rawResponse = """{"code":2,"reason":"Falha ao criar ordem"}""",
                fallbackReference = "reference-1",
            )?.status,
        )
    }

    @Test
    fun mapsPaymentRejectionAndUnknownCodes() {
        assertEquals(
            PaymentStatus.DENIED,
            parser.parse("""{"code":3}""", "reference-1")?.status,
        )
        val unknown = parser.parse("""{"code":99}""", "reference-1")
        assertEquals(PaymentStatus.ERROR, unknown?.status)
        assertEquals("Erro desconhecido", unknown?.errorMessage)
    }

    private fun approvedResponse(reference: String): String =
        """
        {
          "id":"order-1",
          "reference":"$reference",
          "paidAmount":6000,
          "items":[{"sku":"event-1"}],
          "payments":[{"paymentFields":{"statusCode":"1"}}]
        }
        """.trimIndent()
}
