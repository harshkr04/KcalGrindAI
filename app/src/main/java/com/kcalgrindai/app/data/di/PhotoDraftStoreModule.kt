package com.kcalgrindai.app.data.di

import com.kcalgrindai.app.core.util.ImageCompressor
import com.kcalgrindai.app.core.util.PhotoDraftStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoDraftStoreModule {

    @Binds
    @Singleton
    abstract fun bindPhotoDraftStore(
        impl: ImageCompressor
    ): PhotoDraftStore
}
