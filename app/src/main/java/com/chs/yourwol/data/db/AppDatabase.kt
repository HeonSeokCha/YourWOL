package com.chs.yourwol.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [DeviceEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
}
