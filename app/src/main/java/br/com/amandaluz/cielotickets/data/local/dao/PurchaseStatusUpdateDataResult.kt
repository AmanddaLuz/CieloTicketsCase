package br.com.amandaluz.cielotickets.data.local.dao

/**
 * Resultado técnico da atualização condicional de status no banco.
 *
 * Diferencia ausência da tentativa de uma divergência causada por outra
 * atualização, evitando o retorno ambíguo de um simples `Boolean`.
 */
sealed interface PurchaseStatusUpdateDataResult {
    /** O status persistido correspondia ao esperado e foi atualizado. */
    data object Updated : PurchaseStatusUpdateDataResult

    /** Nenhuma tentativa foi encontrada para a referência. */
    data object NotFound : PurchaseStatusUpdateDataResult

    /** A tentativa existe, mas já possui outro status. */
    data class StatusMismatch(val actualStatus: String) : PurchaseStatusUpdateDataResult
}
