package com.swyp.mangro.data.user.model

data class UserProfile(
    val id: Long,
    val role: String,
    val nickname: String,
    val phone: String?,
    val marketingOptIn: Boolean,
    val termsAgreedAt: String,
    val joinedAt: String,
)
