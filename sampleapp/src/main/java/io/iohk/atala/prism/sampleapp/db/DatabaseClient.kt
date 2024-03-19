package io.iohk.atala.prism.sampleapp.db

import android.content.Context
import androidx.room.Room

object DatabaseClient {
    @Volatile
    private var instance: AppDatabase? = null

    fun initializeInstance(context: Context) {
        if (instance == null) {
            synchronized(AppDatabase::class.java) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "database-name"
                    ).build()
                }
            }
        }
    }

    fun getInstance(): AppDatabase {
        return instance ?: throw IllegalStateException("Database has not been initialized.")
    }
}
