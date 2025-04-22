package com.ptit.data.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorRemoteService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuthInterceptorRemoteService