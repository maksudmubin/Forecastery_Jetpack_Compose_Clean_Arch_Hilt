package com.mubin.forecastery.base.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


/**
 * AppModule is a Dagger Hilt module that provides application-level dependencies.
 * This module is installed in the SingletonComponent, meaning the provided dependencies
 * will live as long as the application.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

}