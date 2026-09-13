package com.swyp.mangro.remote.auth.di

import com.swyp.mangro.remote.auth.service.AuthService
import retrofit2.Retrofit

/** Service instances share the caller-supplied network configuration. */
class AuthServices(retrofit: Retrofit) {
    val auth: AuthService = retrofit.create(AuthService::class.java)
}
