package com.swyp.mangro.core.network.di

import com.swyp.mangro.core.network.Constants.BASE_URL
import com.swyp.mangro.core.network.TokenAuthenticator
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.core.network.interceptor.BaseResponseInterceptor
import com.swyp.mangro.core.network.provider.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenProvider: TokenProvider,
    ): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(AuthorizationInterceptor { tokenProvider.accessToken() })
        .authenticator(TokenAuthenticator(tokenProvider))
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client.newBuilder().addInterceptor(BaseResponseInterceptor(json)).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
