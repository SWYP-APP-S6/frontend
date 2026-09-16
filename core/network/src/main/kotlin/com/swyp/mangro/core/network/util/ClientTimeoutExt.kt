package com.swyp.mangro.core.network.util

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.defaultTimeout(): OkHttpClient.Builder = this
    .callTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
