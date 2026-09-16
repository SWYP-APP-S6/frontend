package com.swyp.mangro.core.network.interceptor

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UnwrapBaseResponse(val allowNullData: Boolean = false)
