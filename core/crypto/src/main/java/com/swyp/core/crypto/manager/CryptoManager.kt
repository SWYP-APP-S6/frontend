package com.swyp.core.crypto.manager

/**
 * ### CryptoManager
 *
 * Android Keystore의 키로 바이트 데이터를 암복호화합니다.
 *
 *  - encrypt: 버전·IV·인증 태그가 포함된 암호문을 반환합니다.
 *  - decrypt: encrypt의 반환값에서 원본을 복원합니다. 키가 없으면 생성하지 않습니다.
 *
 * 호출자는 메인 스레드 밖에서 실행하고 형식 오류·키 오류·암호화 예외를 처리해야 합니다.
 * 저장 데이터 삭제나 재로그인은 이 인터페이스의 책임이 아닙니다.
 */
interface CryptoManager {
    fun encrypt(plaintext: ByteArray): ByteArray

    fun decrypt(ciphertext: ByteArray): ByteArray
}
