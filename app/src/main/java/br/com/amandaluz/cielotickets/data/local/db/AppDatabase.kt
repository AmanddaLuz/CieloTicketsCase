package br.com.amandaluz.cielotickets.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.amandaluz.cielotickets.data.local.dao.PurchaseAttemptDao
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod

/**
 * Banco Room da aplicação.
 *
 * Registra a tentativa como entidade pai e seus itens como entidades filhas.
 * O schema é exportado para versionar a estrutura e apoiar futuras migrations.
 */
@Database(
    entities = [
        PurchaseAttemptEntity::class,
        PurchaseItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    /** Fornece o DAO responsável pela persistência das tentativas de compra. */
    abstract fun purchaseAttemptDao(): PurchaseAttemptDao

    companion object {
        private const val DATABASE_NAME = "cielotickets.db"

        /**
         * Adiciona `paymentMethod` com `CREDIT_CASH` como padrão para
         * tentativas persistidas antes da seleção de modalidade de pagamento.
         *
         * Pública para ser exercitada diretamente em
         * `AppDatabaseMigrationTest` (androidTest), que roda em módulo de
         * compilação separado do `main`.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE purchase_attempts ADD COLUMN paymentMethod " +
                        "TEXT NOT NULL DEFAULT '${PaymentMethod.CREDIT_CASH.name}'",
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Retorna a instância única do banco usando o contexto da aplicação.
         *
         * O `synchronized` impede a criação concorrente de mais de uma instância.
         */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).addMigrations(MIGRATION_1_2).build().also { database ->
                    instance = database
                }
            }
    }
}
