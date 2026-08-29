package br.com.amandaluz.cielotickets.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.amandaluz.cielotickets.data.local.dao.PurchaseAttemptDao
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity

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
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    /** Fornece o DAO responsável pela persistência das tentativas de compra. */
    abstract fun purchaseAttemptDao(): PurchaseAttemptDao

    companion object {
        private const val DATABASE_NAME = "cielotickets.db"

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
                ).build().also { database ->
                    instance = database
                }
            }
    }
}
