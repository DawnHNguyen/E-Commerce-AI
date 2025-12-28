package com.ptit.data.di

import com.ptit.data.repository.AuthRepositoryImpl
import com.ptit.data.repository.BrandRepositoryImpl
import com.ptit.data.repository.CartRepositoryImpl
import com.ptit.data.repository.FileUploadRepositoryImpl
import com.ptit.data.repository.HomeRepositoryImpl
import com.ptit.data.repository.PaymentMethodRepositoryImpl
import com.ptit.data.repository.ProductRepositoryImpl
import com.ptit.data.repository.ShopRepositoryImpl
import com.ptit.data.repository.UserRepositoryImpl
import com.ptit.data.repository.OrderRepositoryImpl
import com.ptit.data.repository.SellerRequestRepositoryImpl
import com.ptit.data.repository.ShippingRepositoryImpl
import com.ptit.data.repository.AddressRepositoryImpl
import com.ptit.data.repository.ReviewRepositoryImpl
import com.ptit.data.repository.DiscountRepositoryImpl
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.BrandRepository
import com.ptit.domain.repository.DiscountRepository
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.SellerRequestRepository
import com.ptit.domain.repository.ShippingRepository
import com.ptit.domain.repository.AddressRepository
import com.ptit.domain.repository.ReviewRepository
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

    @Singleton
    @Binds
    abstract fun bindShippingRepository(impl: ShippingRepositoryImpl): ShippingRepository

    @Singleton
    @Binds
    abstract fun bindSellerRequestRepository(impl: SellerRequestRepositoryImpl): SellerRequestRepository

    @Singleton
    @Binds
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    @Singleton
    @Binds
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Singleton
    @Binds
    abstract fun bindBrandRepository(impl: BrandRepositoryImpl): BrandRepository

    @Singleton
    @Binds
    abstract fun bindRecommendationRepository(impl: com.ptit.data.repository.RecommendationRepositoryImpl): com.ptit.domain.repository.RecommendationRepository

    @Singleton
    @Binds
    abstract fun bindDiscountRepository(impl: DiscountRepositoryImpl): DiscountRepository
}
