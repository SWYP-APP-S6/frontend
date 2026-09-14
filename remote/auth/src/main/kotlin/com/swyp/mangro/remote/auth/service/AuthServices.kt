package com.swyp.mangro.remote.auth.service

import retrofit2.Retrofit

/** Service instances share the caller-supplied network configuration. */
class AuthServices(retrofit: Retrofit) {
    val terms: TermsService = retrofit.create(TermsService::class.java)
    val auth: AuthService = retrofit.create(AuthService::class.java)
}
