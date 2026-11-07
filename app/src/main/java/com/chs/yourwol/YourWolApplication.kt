package com.chs.yourwol

import android.app.Application
import com.chs.yourwol.di.YorWolModule
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin
import java.security.Security

class YourWolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<YorWolModule> {
            androidLogger()
            androidContext(this@YourWolApplication)
        }

        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
}