package br.com.amandaluz.cielotickets.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.amandaluz.cielotickets.data.local.dao.PurchaseAttemptDao
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseAttemptEntity
import br.com.amandaluz.cielotickets.data.local.entity.PurchaseItemEntity

@Database(
    entities = [
        PurchaseAttemptEntity::class,
        PurchaseItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseAttemptDao(): PurchaseAttemptDao

    companion object {
        private const val DATABASE_NAME = "cielotickets.db"

        @Volatile
        private var instance: AppDatabase? = null

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

