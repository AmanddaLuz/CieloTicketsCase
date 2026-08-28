package br.com.amandaluz.cielotickets.domain.repository

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import kotlinx.coroutines.flow.Flow

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

    /** Emits purchase attempts ordered from newest to oldest. */
    fun observeHistory(): Flow<List<PurchaseAttempt>>
}
