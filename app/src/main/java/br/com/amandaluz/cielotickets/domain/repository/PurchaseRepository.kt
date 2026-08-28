package br.com.amandaluz.cielotickets.domain.repository

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência das tentativas de compra e de seus snapshots.
 *
 * A atualização de status usa compare-and-set para impedir que callbacks
 * concorrentes sobrescrevam um resultado terminal.
 */
interface PurchaseRepository {
    sealed interface InsertResult {
        data object Inserted : InsertResult
        data object DuplicateReference : InsertResult
    }

    sealed interface StatusUpdateResult {
        data object Updated : StatusUpdateResult
        data object NotFound : StatusUpdateResult
        data class StatusMismatch(val actualStatus: PaymentStatus) : StatusUpdateResult
    }

    suspend fun insert(attempt: PurchaseAttempt): InsertResult

    suspend fun compareAndSetStatus(
        reference: String,
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
        updatedAt: Long,
    ): StatusUpdateResult

    suspend fun findByReference(reference: String): PurchaseAttempt?

    /** Emite as tentativas da mais recente para a mais antiga. */
    fun observeHistory(): Flow<List<PurchaseAttempt>>
}
