package br.com.amandaluz.cielotickets.data.local.repository

import br.com.amandaluz.cielotickets.data.local.dao.PurchaseAttemptDao
import br.com.amandaluz.cielotickets.data.local.dao.PurchaseStatusUpdateDataResult
import br.com.amandaluz.cielotickets.data.local.mapper.toDomain
import br.com.amandaluz.cielotickets.data.local.mapper.toRecord
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPurchaseRepositoryImpl(
    private val purchaseAttemptDao: PurchaseAttemptDao,
) : PurchaseRepository {

    override suspend fun insert(
        attempt: PurchaseAttempt,
    ): PurchaseRepository.InsertResult =
        if (purchaseAttemptDao.insert(attempt.toRecord())) {
            PurchaseRepository.InsertResult.Inserted
        } else {
            PurchaseRepository.InsertResult.DuplicateReference
        }

    override suspend fun compareAndSetStatus(
        reference: String,
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
        updatedAt: Long,
    ): PurchaseRepository.StatusUpdateResult =
        when (
            val result = purchaseAttemptDao.compareAndSetStatus(
                reference = reference,
                expectedStatus = expectedStatus.name,
                newStatus = newStatus.name,
                updatedAt = updatedAt,
            )
        ) {
            PurchaseStatusUpdateDataResult.Updated -> {
                PurchaseRepository.StatusUpdateResult.Updated
            }
            PurchaseStatusUpdateDataResult.NotFound -> {
                PurchaseRepository.StatusUpdateResult.NotFound
            }
            is PurchaseStatusUpdateDataResult.StatusMismatch -> {
                PurchaseRepository.StatusUpdateResult.StatusMismatch(
                    PaymentStatus.valueOf(result.actualStatus),
                )
            }
        }

    override suspend fun findByReference(reference: String): PurchaseAttempt? =
        purchaseAttemptDao.findByReference(reference)?.toDomain()

    override fun observeHistory(): Flow<List<PurchaseAttempt>> =
        purchaseAttemptDao.observeAll().map { attempts ->
            attempts.map { it.toDomain() }
        }
}

