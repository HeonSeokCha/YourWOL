package com.chs.yourwol.di

import android.content.Context
import androidx.room3.Room
import com.chs.yourwol.data.db.AppDatabase
import com.chs.yourwol.data.db.DeviceDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan(
    "com.chs.yourwol.data",
    "com.chs.yourwol.domain",
    "com.chs.yourwol.presentation"
)
class AppModule {
    @Single
    fun provideDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wol.db").build()

    @Single
    fun provideDao(db: AppDatabase): DeviceDao = db.deviceDao()
}


@KoinApplication(
    modules = [AppModule::class]
)
@Configuration
class YorWolModule