package com.swyp.core.local.store

import com.swyp.core.local.model.AuthKey
import kotlinx.coroutines.flow.Flow

/**
 * Consumer와 Owner에서 공통으로 사용하는 인증 토큰 저장소입니다.
 *
 * [AuthKey]의 accessToken과 refreshToken을 각각 암호화하여 저장합니다.
 */
interface AuthStore {
    /**
     * 저장된 인증 토큰을 복호화하여 전달하는 [Flow]입니다.
     *
     * 두 토큰이 모두 없으면 `null`을 전달합니다. 일부 토큰 누락이나 읽기·복호화 오류는
     * `null`로 대체하지 않고 수집하는 호출자에게 예외로 전달합니다.
     */
    val authKey: Flow<AuthKey?>

    /**
     * 각 토큰을 개별 암호화한 뒤 하나의 저장 작업으로 두 값을 함께 저장하거나 교체합니다.
     *
     * 암호화가 실패하면 기존 저장값을 유지하며, 예외는 호출자에게 전달합니다.
     *
     * @param authKey 저장할 accessToken과 refreshToken입니다.
     */
    suspend fun save(authKey: AuthKey)

    /**
     * 저장된 accessToken과 refreshToken을 함께 삭제합니다.
     *
     * 복호화를 수행하지 않으므로 손상된 토큰도 삭제할 수 있습니다.
     * 저장된 토큰이 없으면 그대로 유지하며, 저장소 오류는 호출자에게 전달합니다.
     */
    suspend fun clear()
}
