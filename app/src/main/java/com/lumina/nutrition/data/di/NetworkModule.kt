package com.lumina.nutrition.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.lumina.nutrition.data.remote.api.LuminaAiApiService
import com.lumina.nutrition.data.remote.api.OpenFoodFactsApiService
import com.lumina.nutrition.data.remote.api.SyncApiService
import com.lumina.nutrition.data.remote.api.UsdaApiService
import com.lumina.nutrition.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("UsdaRetrofit")
    fun provideUsdaRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("OffRetrofit")
    fun provideOffRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("AiRetrofit")
    fun provideAiRetrofit(
        okHttpClient: OkHttpClient,
        authRepositoryProvider: Provider<AuthRepository>,
        json: Json
    ): Retrofit {
        val authClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = runBlocking {
                    try {
                        authRepositoryProvider.get().getIdToken(forceRefresh = true)
                    } catch (e: Exception) {
                        null
                    }
                }
                val request = if (!token.isNullOrBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            }
            .build()

        val contentType = "application/json".toMediaType()
        // Backend base URL configured via BuildConfig (Dev loopback 10.0.2.2 or Cloud Run HTTPS)
        val baseUrl = com.lumina.nutrition.BuildConfig.BACKEND_URL
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideUsdaApiService(@Named("UsdaRetrofit") retrofit: Retrofit): UsdaApiService {
        return retrofit.create(UsdaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApiService(@Named("OffRetrofit") retrofit: Retrofit): OpenFoodFactsApiService {
        return retrofit.create(OpenFoodFactsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLuminaAiApiService(@Named("AiRetrofit") retrofit: Retrofit): LuminaAiApiService {
        return retrofit.create(LuminaAiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSyncApiService(@Named("AiRetrofit") retrofit: Retrofit): SyncApiService {
        return retrofit.create(SyncApiService::class.java)
    }
}
