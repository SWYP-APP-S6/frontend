package com.swyp.core.local.model

/**
 * 소비자의 사용자 정보입니다.
 *
 * [userId]는 사용자 ID이며, [phone]은 미등록 상태일 수 있습니다.
 */
data class UserInfo(
    val userId: Long,
    val nickname: String,
    val phone: String?,
) {
    override fun toString(): String = "UserInfo(REDACTED)"
}
