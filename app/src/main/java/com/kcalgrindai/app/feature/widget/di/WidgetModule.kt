package com.kcalgrindai.app.feature.widget.di

import com.kcalgrindai.app.core.util.WidgetUpdater
import com.kcalgrindai.app.feature.widget.util.GlanceWidgetUpdater
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindWidgetUpdater(
        impl: GlanceWidgetUpdater
    ): WidgetUpdater
}
