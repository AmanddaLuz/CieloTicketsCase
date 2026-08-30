package br.com.amandaluz.cielotickets.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Valida a migração 1→2, que adiciona `paymentMethod` sem perder tentativas
 * persistidas antes da seleção de modalidade de pagamento.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2BackfillsPaymentMethodAsCreditCash() {
        migrationTestHelper.createDatabase(DATABASE_NAME, 1).apply {
            execSQL(
                "INSERT INTO purchase_attempts " +
                    "(reference, status, createdAt, updatedAt) VALUES " +
                    "('legacy-1', 'APPROVED', 100, 200)",
            )
            close()
        }

        val migratedDatabase: SupportSQLiteDatabase = migrationTestHelper.runMigrationsAndValidate(
            DATABASE_NAME,
            2,
            true,
            AppDatabase.MIGRATION_1_2,
        )

        migratedDatabase.query(
            "SELECT paymentMethod FROM purchase_attempts WHERE reference = 'legacy-1'",
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("CREDIT_CASH", cursor.getString(0))
        }
    }

    private companion object {
        const val DATABASE_NAME = "migration-test"
    }
}
