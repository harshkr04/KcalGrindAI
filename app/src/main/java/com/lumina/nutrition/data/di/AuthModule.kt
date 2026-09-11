package com.lumina.nutrition.data.di

import com.google.firebase.auth.FirebaseAuth
import com.lumina.nutrition.BuildConfig
import com.lumina.nutrition.data.repository.AuthRepositoryImpl
import com.lumina.nutrition.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG) {
            try {
                auth.useEmulator("10.0.2.2", 9099)
            } catch (e: Exception) {
                // Already configured
            }
        }
        return auth
    }
}
