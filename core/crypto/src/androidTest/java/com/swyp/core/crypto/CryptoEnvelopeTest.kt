package com.swyp.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.core.crypto.impl.AndroidKeyStoreCryptoManagerImpl
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoEnvelopeTest {
    private val manager = AndroidKeyStoreCryptoManagerImpl()

    @Before
    @After
    fun clearTestKey() {
        // Library instrumentation uses a separate test app's Keystore.
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry("mangro.local.aes.v1")
        }
    }

    @Test
    fun accessTokenAndRefreshTokenValuesAreEncryptedAndRestoredSeparately() {
        // Encrypt each token value directly, without JSON serialization.
        val accessToken = ACCESS_TOKEN
        val refreshToken = REFRESH_TOKEN

        val encryptedAccessToken = manager.encrypt(accessToken.toByteArray(Charsets.UTF_8))
        val encryptedRefreshToken = manager.encrypt(refreshToken.toByteArray(Charsets.UTF_8))

        assertFalse(accessToken.toByteArray(Charsets.UTF_8).contentEquals(encryptedAccessToken))
        assertFalse(refreshToken.toByteArray(Charsets.UTF_8).contentEquals(encryptedRefreshToken))

        val reader = AndroidKeyStoreCryptoManagerImpl()
        assertEquals(accessToken, reader.decrypt(encryptedAccessToken).toString(Charsets.UTF_8))
        assertEquals(refreshToken, reader.decrypt(encryptedRefreshToken).toString(Charsets.UTF_8))
    }

    @Test
    fun nicknamePreservesKoreanEmojiAndWhitespace() {
        val nickname = "  맹그로 사용자 🥭  "

        val encrypted = manager.encrypt(nickname.toByteArray(Charsets.UTF_8))
        val restoredNickname = manager.decrypt(encrypted).toString(Charsets.UTF_8)

        assertEquals(nickname, restoredNickname)
    }

    @Test
    fun truncatedEncryptedAccessTokenIsRejected() {
        val accessToken = ACCESS_TOKEN
        val encrypted = manager.encrypt(accessToken.toByteArray(Charsets.UTF_8))
        val truncated = encrypted.copyOf(encrypted.size - 1)

        assertThrows(AEADBadTagException::class.java) { manager.decrypt(truncated) }
        assertEquals(accessToken, manager.decrypt(encrypted).toString(Charsets.UTF_8))
    }

    @Test
    fun unsupportedVersionOfEncryptedRefreshTokenIsRejected() {
        val refreshToken = REFRESH_TOKEN
        val encrypted = manager.encrypt(refreshToken.toByteArray(Charsets.UTF_8))
        val unsupportedVersion = encrypted.copyOf().apply { this[0] = 2 }

        assertThrows(IllegalArgumentException::class.java) { manager.decrypt(unsupportedVersion) }
        assertEquals(refreshToken, manager.decrypt(encrypted).toString(Charsets.UTF_8))
    }
    private companion object {
        // Synthetic, expired JWT fixtures signed with a test-only key; not server-issued credentials.
        const val ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMDAwMSIsInJvbGUiOiJPV05FUiIsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAwMzYwMCwianRpIjoidGVzdC1hY2Nlc3MtMTAwMDEifQ." +
                "3hPP6758qnAg8jZyEesl7Qk6FIq_aGaqQ9ppkbJACEM"
        const val REFRESH_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMDAwMSIsInJvbGUiOiJPV05FUiIsInRva2VuX3R5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDEyMDk2MDAsImp0aSI6InRlc3QtcmVmcmVzaC0xMDAwMSJ9." +
                "al_cB9Ik7Paxm9sojTHAzeUM8M8ou6OtPGMw3SPVqxg"
    }
}
