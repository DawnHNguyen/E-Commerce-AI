package com.ptit.data.di

import com.google.gson.Gson
import com.ptit.data.BuildConfig
import com.ptit.data.remote.api.AuthApi
import com.ptit.data.remote.api.NoAuthInterceptApi
import com.ptit.data.remote.api.ProductApi
import com.ptit.data.remote.api.PurchaseApi
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
            .serializeNulls()
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
    fun providePurchaseApi(
        @AuthInterceptorRemoteService retrofit: Retrofit,
    ): PurchaseApi = retrofit.create(PurchaseApi::class.java)
}