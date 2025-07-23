package com.ldlywt.note.hilt

import android.app.Application
import android.content.Context
import com.ldlywt.note.api.MemosApiService
import com.ldlywt.note.backup.SyncManager
import com.ldlywt.note.backup.api.Encryption
import com.ldlywt.note.backup.utils.DefaultEncryption
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun providesSyncManager(
        @ApplicationContext context: Context,
    ) = SyncManager(context)

    @Provides
    fun provideEncryption(): Encryption {
        return DefaultEncryption()
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideMemosApiService(): MemosApiService {
        return MemosApiService()
    }

}
