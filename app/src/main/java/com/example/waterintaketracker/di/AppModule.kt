// FileName: MultipleFiles/AppModule.kt (Modified)
package com.example.waterintaketracker.di

import android.content.Context // Import Context
import com.example.waterintaketracker.WaterNotificationService // Import WaterNotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideWaterNotificationService(
        @ApplicationContext context: Context // Use @ApplicationContext for application-level context
    ): WaterNotificationService {
        return WaterNotificationService(context)
    }
}
