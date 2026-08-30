package br.com.amandaluz.cielotickets.feature.common

import java.text.DateFormat
import java.util.Date
import java.util.Locale

class BrazilianDateTimeFormatter {
    private val formatter = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        Locale.forLanguageTag("pt-BR"),
    )

    fun format(timestamp: Long): String = formatter.format(Date(timestamp))
}
