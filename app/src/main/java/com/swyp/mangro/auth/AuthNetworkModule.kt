package com.swyp.mangro.auth

import com.swyp.mangro.core.network.Constants.BASE_URL
import com.swyp.mangro.core.network.provider.TokenProvider
import com.swyp.mangro.core.network.util.defaultTimeout
import com.swyp.mangro.remote.auth.service.TokenRefreshService
import dagger.Binds
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
abstract class AuthNetworkModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(implementation: StoreTokenProvider): TokenProvider

    companion object {
        /** 갱신 요청은 별도 dispatcher와 인증 재시도가 없는 클라이언트로 실행합니다. */
        @Provides
        @Singleton
        fun provideRefreshService(json: Json): TokenRefreshService {
            val client = OkHttpClient
                .Builder()
                .defaultTimeout()
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(TokenRefreshService::class.java)
        }
    }
}
