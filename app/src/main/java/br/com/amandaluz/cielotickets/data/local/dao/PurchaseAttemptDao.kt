package br.com.amandaluz.cielotickets.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptWithItems
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity
import br.com.amandaluz.cielotickets.data.local.mapper.PurchaseAttemptRecord
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PurchaseAttemptDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertAttempt(
        attempt: PurchaseAttemptEntity,
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertItems(items: List<PurchaseItemEntity>)

    @Query(
        """
        UPDATE purchase_attempts
        SET status = :newStatus, updatedAt = :updatedAt
        WHERE reference = :reference AND status = :expectedStatus
        """,
    )
    protected abstract suspend fun updateStatusIfExpected(
        reference: String,
        expectedStatus: String,
        newStatus: String,
        updatedAt: Long,
    ): Int

    @Query("SELECT status FROM purchase_attempts WHERE reference = :reference LIMIT 1")
    protected abstract suspend fun findStatus(reference: String): String?

    @Transaction
    open suspend fun insert(record: PurchaseAttemptRecord): Boolean {
        val rowId = insertAttempt(record.attempt)
        val inserted = rowId != INSERT_CONFLICT
        if (inserted) {
            insertItems(record.items)
        }
        return inserted
    }

    @Transaction
    open suspend fun compareAndSetStatus(
        reference: String,
        expectedStatus: String,
        newStatus: String,
        updatedAt: Long,
    ): PurchaseStatusUpdateDataResult {
        val updatedRows = updateStatusIfExpected(
            reference = reference,
            expectedStatus = expectedStatus,
            newStatus = newStatus,
            updatedAt = updatedAt,
        )
        return when {
            updatedRows > 0 -> PurchaseStatusUpdateDataResult.Updated
            else -> findStatus(reference)?.let(
                PurchaseStatusUpdateDataResult::StatusMismatch,
            ) ?: PurchaseStatusUpdateDataResult.NotFound
        }
    }

    @Transaction
    @Query("SELECT * FROM purchase_attempts WHERE reference = :reference LIMIT 1")
    abstract suspend fun findByReference(reference: String): PurchaseAttemptWithItems?

    @Transaction
    @Query("SELECT * FROM purchase_attempts ORDER BY createdAt DESC")
    abstract fun observeAll(): Flow<List<PurchaseAttemptWithItems>>

    private companion object {
        const val INSERT_CONFLICT = -1L
    }
}

