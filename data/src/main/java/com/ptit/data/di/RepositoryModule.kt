package com.ptit.data.di

import com.ptit.data.repository.AuthRepositoryImpl
import com.ptit.data.repository.HomeRepositoryImpl
import com.ptit.data.repository.ProductRepositoryImpl
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Singleton
    @Binds
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Singleton
    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}