package com.swyp.core.crypto

import com.swyp.core.crypto.impl.AndroidKeyStoreCryptoManagerImpl
import org.junit.Assert.assertThrows
import org.junit.Test

class CryptoEnvelopeTest {
    private val manager = AndroidKeyStoreCryptoManagerImpl()

    @Test
    fun truncatedEnvelopesAreRejectedBeforeAccessingKeystore() {
        for (size in 0 until 29) {
            assertThrows(IllegalArgumentException::class.java) { manager.decrypt(ByteArray(size)) }
        }
    }

    @Test
    fun unsupportedVersionIsRejectedBeforeAccessingKeystore() {
        val encrypted = ByteArray(29).apply { this[0] = 2 }
        assertThrows(IllegalArgumentException::class.java) { manager.decrypt(encrypted) }
    }
}
