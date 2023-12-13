package io.iohk.atala.prism.sampleapp.db

import android.content.Context
import androidx.room.Room

object DatabaseClient {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun initializeInstance(context: Context) {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "database-name"
                    ).build()
                }
            }
        }
    }

    fun getInstance(): AppDatabase {
        return INSTANCE ?: throw IllegalStateException("Database has not been initialized.")
    }
}
