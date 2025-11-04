package com.ptit.data.di

import com.ptit.data.repository.AuthRepositoryImpl
import com.ptit.data.repository.CartRepositoryImpl
import com.ptit.data.repository.FileUploadRepositoryImpl
import com.ptit.data.repository.HomeRepositoryImpl
import com.ptit.data.repository.PaymentMethodRepositoryImpl
import com.ptit.data.repository.ProductRepositoryImpl
import com.ptit.data.repository.ShopRepositoryImpl
import com.ptit.data.repository.UserRepositoryImpl
import com.ptit.data.repository.OrderRepositoryImpl
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.repository.OrderRepository
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

    @Singleton
    @Binds
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    abstract fun bindShopRepository(impl: ShopRepositoryImpl): ShopRepository

    @Singleton
    @Binds
    abstract fun bindFileRepository(impl: FileUploadRepositoryImpl): FileUploadRepository

    @Singleton
    @Binds
    abstract fun bindPaymentMethodRepository(impl: PaymentMethodRepositoryImpl): PaymentMethodRepository

    @Singleton
    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}
