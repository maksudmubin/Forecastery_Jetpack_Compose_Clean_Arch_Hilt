package com.mubin.forecastery.base.di

import android.content.Context
import com.mubin.forecastery.data.api.ApiService
import com.mubin.forecastery.data.repo.AppRepositoryImpl
import com.mubin.forecastery.domain.repo.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


/**
 * AppModule is a Dagger Hilt module that provides application-level dependencies.
 * This module is installed in the SingletonComponent, meaning the provided dependencies
 * will live as long as the application.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /**
     * Provides an implementation of the AppRepository interface.
     *
     * @param apiService An instance of ApiService for network operations.
     * @return An implementation of AppRepository (AppRepositoryImpl).
     */
    @Provides
    fun provideRepository(
        apiService: ApiService,
        @ApplicationContext context: Context
    ): AppRepository = AppRepositoryImpl(apiService, context)

}