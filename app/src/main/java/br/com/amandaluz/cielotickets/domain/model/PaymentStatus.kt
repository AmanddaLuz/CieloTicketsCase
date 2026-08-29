package br.com.amandaluz.cielotickets.domain.model

/**
 * Estados persistidos da máquina de pagamento.
 *
 * Estados terminais não aceitam novas transições.
 * Impede que callbacks atrasados ou duplicados sobrescrevam um pagamento já
 * concluído. A validação acontece no domínio, enquanto o compare-and-set do DAO
 * garante essa proteção também diante de concorrência.
 */
enum class PaymentStatus {
    CREATED,
    PROCESSING,
    APPROVED,
    DENIED,
    CANCELLED,
    ERROR,
    ;

    val isTerminal: Boolean
        get() = this in TERMINAL_STATUSES

    fun canTransitionTo(newStatus: PaymentStatus): Boolean = when {
        this == CREATED -> newStatus == PROCESSING
        this == PROCESSING -> newStatus.isTerminal
        else -> false
    }

    private companion object {
        val TERMINAL_STATUSES = setOf(APPROVED, DENIED, CANCELLED, ERROR)
    }
}
