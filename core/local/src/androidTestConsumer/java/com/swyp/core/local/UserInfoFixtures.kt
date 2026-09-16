package com.swyp.core.local

import com.swyp.core.local.model.UserInfo

internal object UserInfoFixtures {
    val original = UserInfo(userId = 12345L, nickname = "망그러진 감자", phone = "010-1234-5678")
    val replacement = UserInfo(userId = 67890L, nickname = "행복한 당근", phone = null)
    val plaintextFields = mapOf("userId" to "12345", "nickname" to "망그러진 감자", "phone" to "010-1234-5678")
    val removedKeys = listOf("phone")
    const val REQUIRED_KEY = "nickname"
}
