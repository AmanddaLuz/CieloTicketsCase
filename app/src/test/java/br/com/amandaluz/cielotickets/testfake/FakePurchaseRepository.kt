package br.com.amandaluz.cielotickets.testfake

import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakePurchaseRepository(
    attempts: List<PurchaseAttempt> = emptyList(),
) : PurchaseRepository {
    private val attemptsByReference = attempts.associateByTo(
        linkedMapOf(),
        PurchaseAttempt::reference,
    )
    private val history = MutableStateFlow(sortedHistory())

    var nextStatusUpdateResult: PurchaseRepository.StatusUpdateResult? = null

    override suspend fun insert(
        attempt: PurchaseAttempt,
    ): PurchaseRepository.InsertResult {
        return if (attemptsByReference.containsKey(attempt.reference)) {
            PurchaseRepository.InsertResult.DuplicateReference
        } else {
            attemptsByReference[attempt.reference] = attempt
            emitHistory()
            PurchaseRepository.InsertResult.Inserted
        }
    }

    override suspend fun compareAndSetStatus(
        reference: String,
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
        updatedAt: Long,
    ): PurchaseRepository.StatusUpdateResult {
        nextStatusUpdateResult?.let { configuredResult ->
            nextStatusUpdateResult = null
            return configuredResult
        }

        val current = attemptsByReference[reference]
        return when {
            current == null -> PurchaseRepository.StatusUpdateResult.NotFound
            current.status != expectedStatus -> {
                PurchaseRepository.StatusUpdateResult.StatusMismatch(current.status)
            }
            else -> {
                attemptsByReference[reference] = current.withStatus(newStatus, updatedAt)
                emitHistory()
                PurchaseRepository.StatusUpdateResult.Updated
            }
        }
    }

    override suspend fun findByReference(reference: String): PurchaseAttempt? =
        attemptsByReference[reference]

    override fun observeByReference(
        reference: String,
    ): Flow<PurchaseAttempt?> = history.map { attempts ->
        attempts.firstOrNull { it.reference == reference }
    }

    override fun observeHistory(): Flow<List<PurchaseAttempt>> =
        history.asStateFlow()

    private fun emitHistory() {
        history.value = sortedHistory()
    }

    private fun sortedHistory(): List<PurchaseAttempt> =
        attemptsByReference.values.sortedByDescending(PurchaseAttempt::createdAt)
}
