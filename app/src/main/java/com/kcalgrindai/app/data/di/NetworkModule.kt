package com.kcalgrindai.app.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kcalgrindai.app.data.remote.api.KcalGrindAIAiApiService
import com.kcalgrindai.app.data.remote.api.OpenFoodFactsApiService
import com.kcalgrindai.app.data.remote.api.SyncApiService
import com.kcalgrindai.app.data.remote.api.UsdaApiService
import com.kcalgrindai.app.domain.repository.AuthRepository
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
        billingRepositoryProvider: Provider<com.kcalgrindai.app.domain.repository.BillingRepository>,
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
                val builder = original.newBuilder()
                if (!token.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $token")
                }
                // Send user local timezone ID (e.g. Asia/Kolkata, America/New_York)
                builder.header("X-User-Timezone", java.util.TimeZone.getDefault().id)

                // In debug/test builds only: send test Pro status for local verification
                if (com.kcalgrindai.app.BuildConfig.DEBUG) {
                    val isPro = runBlocking {
                        try {
                            billingRepositoryProvider.get().isPro()
                        } catch (e: Exception) {
                            false
                        }
                    }
                    if (isPro) {
                        builder.header("X-Test-Pro", "true")
                    }
                }

                chain.proceed(builder.build())
            }
            .build()

        val contentType = "application/json".toMediaType()
        // Backend base URL configured via BuildConfig (Dev loopback 10.0.2.2 or Cloud Run HTTPS)
        val baseUrl = com.kcalgrindai.app.BuildConfig.BACKEND_URL
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
    fun provideKcalGrindAIAiApiService(@Named("AiRetrofit") retrofit: Retrofit): KcalGrindAIAiApiService {
        return retrofit.create(KcalGrindAIAiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSyncApiService(@Named("AiRetrofit") retrofit: Retrofit): SyncApiService {
        return retrofit.create(SyncApiService::class.java)
    }
}
