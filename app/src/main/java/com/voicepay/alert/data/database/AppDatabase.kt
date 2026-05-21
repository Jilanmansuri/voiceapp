package com.voicepay.alert.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.voicepay.alert.data.dao.PaymentNotificationDao
import com.voicepay.alert.data.entity.PaymentNotificationEntity

@Database(
    entities = [PaymentNotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun paymentNotificationDao(): PaymentNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voicepay_alert.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
