package com.swyp.mangro.remote.auth.di

import com.swyp.mangro.core.network.Constants.BASE_URL
import com.swyp.mangro.core.network.interceptor.BaseResponseInterceptor
import com.swyp.mangro.core.network.util.defaultTimeout
import com.swyp.mangro.remote.auth.service.AuthService
import com.swyp.mangro.remote.auth.service.TermsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AuthServiceModule {
    @Provides
    @Singleton
    @Named("login")
    fun provideLoginTermsService(json: Json): TermsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().defaultTimeout().addInterceptor(BaseResponseInterceptor(json)).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build().create(TermsService::class.java)

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    // 로그인 401은 기존 회원 토큰 갱신을 유발하지 않는다.
    @Provides
    @Singleton
    @Named("login")
    fun provideLoginAuthService(json: Json): AuthService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().defaultTimeout().addInterceptor(BaseResponseInterceptor(json)).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AuthService::class.java)
}
