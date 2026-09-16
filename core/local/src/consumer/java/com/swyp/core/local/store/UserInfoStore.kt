package com.swyp.core.local.store

import com.swyp.core.local.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 소비자의 사용자 정보를 암호화하여 보관하는 저장소입니다.
 */
interface UserInfoStore {
    /**
     * 저장된 정보를 관찰합니다. 저장된 정보가 없으면 null을 반환합니다.
     *
     * 필수 필드 누락이나 복호화 실패는 예외로 전달하며 저장된 데이터를 자동 삭제하지 않습니다.
     */
    val userInfo: Flow<UserInfo?>

    /**
     * 각 필드를 암호화한 뒤 전체 정보를 한 번에 교체합니다.
     *
     * 암호화에 실패하면 기존 정보를 유지하며, null인 선택 필드는 기존 값을 제거합니다.
     */
    suspend fun save(userInfo: UserInfo)

    /** 로그아웃이나 계정 변경 시 저장된 정보를 삭제합니다. 반복 호출할 수 있습니다. */
    suspend fun clear()
}
