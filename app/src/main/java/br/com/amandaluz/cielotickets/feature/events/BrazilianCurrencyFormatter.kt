package br.com.amandaluz.cielotickets.feature.events

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class BrazilianCurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR"),
    )

    fun format(valueInCents: Long): String =
        formatter.format(BigDecimal.valueOf(valueInCents, CURRENCY_SCALE))

    private companion object {
        const val CURRENCY_SCALE = 2
    }
}

