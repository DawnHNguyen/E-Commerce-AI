package com.ptit.data.di

import com.google.gson.Gson
import com.ptit.data.BuildConfig
import com.ptit.data.remote.api.AuthApi
import com.ptit.data.remote.api.CartApi
import com.ptit.data.remote.api.NoAuthInterceptApi
import com.ptit.data.remote.api.OrderApi
import com.ptit.data.remote.api.ProductApi
import com.ptit.data.remote.api.UserService
import com.ptit.data.remote.api.RecommendApi
import javax.inject.Named
import com.ptit.data.remote.util.CallAdapterFactory
import com.ptit.data.remote.util.HeaderAuthorizationInterceptor
import com.ptit.data.remote.util.RefreshTokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteModule {
    private const val BASE_URL: String = BuildConfig.BASE_URL
    private const val RECOMMEND_BASE_URL: String = "https://recommend-system-722597103220.us-central1.run.app/"

    @Singleton
    @Provides
    @Named("RecommendRetrofit")
    fun provideRecommendRetrofit(
        @NoAuthInterceptorRemoteService client: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(RECOMMEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRecommendApi(
        @Named("RecommendRetrofit") retrofit: Retrofit,
    ): RecommendApi = retrofit.create(RecommendApi::class.java)

    @Singleton
    @Provides
    fun provideHttpLogging() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Singleton
    @Provides
    fun provideRefreshTokenAuthenticator(
        remoteService: NoAuthInterceptApi
    ): RefreshTokenAuthenticator = RefreshTokenAuthenticator(remoteService = remoteService)

    @Singleton
    @Provides
    @AuthInterceptorRemoteService
    fun provideClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        refreshTokenAuthenticator: RefreshTokenAuthenticator,
    ) =
        OkHttpClient.Builder()
            .addInterceptor(HeaderAuthorizationInterceptor())
            .addInterceptor(httpLoggingInterceptor)
            .authenticator(refreshTokenAuthenticator)
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()

    @Singleton
    @Provides
    @NoAuthInterceptorRemoteService
    fun provideNoAuthInterceptClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ) =
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()

    @Singleton
    @Provides
    fun provideGson(): Gson =
        Gson().newBuilder()
            .create()

    @Singleton
    @Provides
    @AuthInterceptorRemoteService
    fun provideRetrofit(
        @AuthInterceptorRemoteService client: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CallAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    @NoAuthInterceptorRemoteService
    fun provideNoAuthInterceptRetrofit(
        @NoAuthInterceptorRemoteService client: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CallAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideNoAuthInterceptApi(
        @NoAuthInterceptorRemoteService retrofit: Retrofit,
    ): NoAuthInterceptApi = retrofit.create(NoAuthInterceptApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProductApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun provideCartApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): CartApi = retrofit.create(CartApi::class.java)
    
    @Provides
    @Singleton
    fun provideUserService(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): UserService = retrofit.create(UserService::class.java)


    @Provides
    @Singleton
    fun provideOrderApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): OrderApi = retrofit.create(OrderApi::class.java)

    @Provides
    @Singleton
    fun provideShopApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.ShopApi = retrofit.create(com.ptit.data.remote.api.ShopApi::class.java)

    @Provides
    @Singleton
    fun provideFileUploadApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.api.FileUploadApi = retrofit.create(com.ptit.data.api.FileUploadApi::class.java)

    @Provides
    @Singleton
    fun provideShippingApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.ShippingApi = retrofit.create(com.ptit.data.remote.api.ShippingApi::class.java)

    @Provides
    @Singleton
    fun provideSellerRequestApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.SellerRequestApi = retrofit.create(com.ptit.data.remote.api.SellerRequestApi::class.java)

    @Provides
    @Singleton
    fun provideAddressService(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.AddressService = retrofit.create(com.ptit.data.remote.api.AddressService::class.java)

    @Provides
    @Singleton
    fun provideReviewApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.ReviewApi = retrofit.create(com.ptit.data.remote.api.ReviewApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.PaymentApi = retrofit.create(com.ptit.data.remote.api.PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideBrandApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): com.ptit.data.remote.api.BrandApi = retrofit.create(com.ptit.data.remote.api.BrandApi::class.java)
}