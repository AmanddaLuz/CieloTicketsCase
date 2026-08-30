package br.com.amandaluz.cielotickets.domain.model

/**
 * Modalidade de pagamento à vista suportada pelo terminal Cielo Smart.
 *
 * `cieloCode` é o valor exigido pelo campo `paymentCode` do payload de
 * pagamento da Cielo e não deve ser reutilizado fora do encoder.
 */
enum class PaymentMethod(val cieloCode: String) {
    CREDIT_CASH("CREDITO_AVISTA"),
    DEBIT_CASH("DEBITO_AVISTA"),
}
