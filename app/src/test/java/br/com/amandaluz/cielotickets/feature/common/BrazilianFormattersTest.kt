package br.com.amandaluz.cielotickets.feature.common

import org.junit.Assert.assertTrue
import org.junit.Test

class BrazilianFormattersTest {

    @Test
    fun formatsCurrencyInBrazilianLocale() {
        val formatted = BrazilianCurrencyFormatter().format(1_234L)

        assertTrue(formatted.contains("12,34"))
    }

    @Test
    fun formatsTimestampInBrazilianLocale() {
        val formatted = BrazilianDateTimeFormatter().format(0L)

        assertTrue(formatted.isNotBlank())
    }
}
