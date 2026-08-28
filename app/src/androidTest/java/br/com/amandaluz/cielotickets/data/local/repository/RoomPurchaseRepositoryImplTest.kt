package br.com.amandaluz.cielotickets.data.local.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.data.local.dao.PurchaseAttemptDao
import br.com.amandaluz.cielotickets.data.local.db.AppDatabase
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity
import br.com.amandaluz.cielotickets.data.local.mapper.PurchaseAttemptRecord
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.model.PurchaseItem
import br.com.amandaluz.cielotickets.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPurchaseRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: PurchaseAttemptDao
    private lateinit var repository: PurchaseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        ).build()
        dao = database.purchaseAttemptDao()
        repository = RoomPurchaseRepositoryImpl(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertsAndRestoresMultiEventAttempt() = runTest {
        val attempt = attempt(reference = "reference-1")

        assertEquals(
            PurchaseRepository.InsertResult.Inserted,
            repository.insert(attempt),
        )

        val restored = repository.findByReference(attempt.reference)
        assertEquals(attempt.reference, restored?.reference)
        assertEquals(attempt.items, restored?.items)
        assertEquals(attempt.totalInCents, restored?.totalInCents)
    }

    @Test
    fun duplicateReferenceDoesNotOverwriteOriginalAttempt() = runTest {
        val original = attempt(reference = "reference-1")
        repository.insert(original)
        val duplicate = attempt(
            reference = "reference-1",
            eventName = "Changed",
        )

        assertEquals(
            PurchaseRepository.InsertResult.DuplicateReference,
            repository.insert(duplicate),
        )
        assertEquals(original.items, repository.findByReference("reference-1")?.items)
    }

    @Test
    fun itemConstraintFailureRollsBackAttemptInsertion() = runTest {
        val reference = "invalid-items"
        val record = PurchaseAttemptRecord(
            attempt = PurchaseAttemptEntity(
                reference = reference,
                status = PaymentStatus.CREATED.name,
                createdAt = 100L,
                updatedAt = 100L,
            ),
            items = listOf(
                itemEntity(reference = reference, position = 0),
                itemEntity(reference = reference, position = 0),
            ),
        )

        val failure = runCatching { dao.insert(record) }.exceptionOrNull()
        assertTrue(failure is SQLiteConstraintException)
        assertNull(dao.findByReference(reference))
    }

    @Test
    fun compareAndSetUpdatesOnlyExpectedStatus() = runTest {
        repository.insert(attempt(reference = "reference-1"))

        assertEquals(
            PurchaseRepository.StatusUpdateResult.Updated,
            repository.compareAndSetStatus(
                reference = "reference-1",
                expectedStatus = PaymentStatus.CREATED,
                newStatus = PaymentStatus.PROCESSING,
                updatedAt = 200L,
            ),
        )
        assertEquals(
            PurchaseRepository.StatusUpdateResult.StatusMismatch(
                PaymentStatus.PROCESSING,
            ),
            repository.compareAndSetStatus(
                reference = "reference-1",
                expectedStatus = PaymentStatus.CREATED,
                newStatus = PaymentStatus.ERROR,
                updatedAt = 300L,
            ),
        )

        val restored = repository.findByReference("reference-1")
        assertEquals(PaymentStatus.PROCESSING, restored?.status)
        assertEquals(200L, restored?.updatedAt)
    }

    @Test
    fun compareAndSetReturnsNotFoundForUnknownReference() = runTest {
        assertEquals(
            PurchaseRepository.StatusUpdateResult.NotFound,
            repository.compareAndSetStatus(
                reference = "unknown",
                expectedStatus = PaymentStatus.CREATED,
                newStatus = PaymentStatus.PROCESSING,
                updatedAt = 200L,
            ),
        )
        assertNull(repository.findByReference("unknown"))
    }

    @Test
    fun historyIsNewestFirst() = runTest {
        val older = attempt(reference = "older", createdAt = 100L)
        val newer = attempt(reference = "newer", createdAt = 200L)
        repository.insert(older)
        repository.insert(newer)

        assertEquals(
            listOf("newer", "older"),
            repository.observeHistory().first().map(PurchaseAttempt::reference),
        )
    }

    private fun attempt(
        reference: String,
        eventName: String = "Festival",
        createdAt: Long = 100L,
    ) = PurchaseAttempt.restore(
        reference = reference,
        items = listOf(
            PurchaseItem(
                eventId = "event-1",
                eventName = eventName,
                quantity = 2,
                unitPriceInCents = 3_000L,
            ),
            PurchaseItem(
                eventId = "event-2",
                eventName = "Show",
                quantity = 1,
                unitPriceInCents = 5_000L,
            ),
        ),
        status = PaymentStatus.CREATED,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    private fun itemEntity(
        reference: String,
        position: Int,
    ) = PurchaseItemEntity(
        attemptReference = reference,
        position = position,
        eventId = "event-$position",
        eventName = "Event $position",
        quantity = 1,
        unitPriceInCents = 1_000L,
    )
}
