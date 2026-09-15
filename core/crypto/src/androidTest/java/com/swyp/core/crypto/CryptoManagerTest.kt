package com.swyp.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.core.crypto.impl.AndroidKeyStoreCryptoManagerImpl
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.AEADBadTagException
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {
    private val manager = AndroidKeyStoreCryptoManagerImpl()

    // Instrumentation runs in the library's test app, with its own Keystore namespace.
    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    @Before
    @After
    fun clearTestKey() {
        keyStore().deleteEntry("mangro.local.aes.v1")
    }

    @Test
    fun roundTripPreservesEmptyAndBinaryDataAcrossInstances() {
        for (plaintext in listOf(byteArrayOf(), ByteArray(256) { it.toByte() }, "맹그로".toByteArray())) {
            val encrypted = manager.encrypt(plaintext)
            assertArrayEquals(plaintext, AndroidKeyStoreCryptoManagerImpl().decrypt(encrypted))
        }
    }

    @Test
    fun repeatedEncryptionUsesDifferentIvs() {
        val plaintext = "test data".toByteArray()
        val first = manager.encrypt(plaintext)
        val second = manager.encrypt(plaintext)
        assertFalse(first.copyOfRange(1, 13).contentEquals(second.copyOfRange(1, 13)))
        assertArrayEquals(plaintext, manager.decrypt(first))
        assertArrayEquals(plaintext, manager.decrypt(second))
    }

    @Test
    fun tamperedIvCiphertextAndTagAreRejected() {
        val encrypted = manager.encrypt("test data".toByteArray())
        for (index in listOf(1, 13, encrypted.lastIndex)) {
            val tampered = encrypted.copyOf()
            tampered[index] = (tampered[index].toInt() xor 1).toByte()
            assertThrows(AEADBadTagException::class.java) { manager.decrypt(tampered) }
        }
    }

    @Test
    fun missingKeyDoesNotCreateReplacement() {
        val encrypted = manager.encrypt("test data".toByteArray())
        clearTestKey()
        assertThrows(KeyStoreException::class.java) { manager.decrypt(encrypted) }
        assertFalse(keyStore().containsAlias("mangro.local.aes.v1"))
    }

    @Test
    fun concurrentFirstUseOfSharedManagerPreservesEveryCiphertext() {
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val results = (0 until 8).map { index ->
                executor.submit(
                    Callable {
                        check(start.await(10, TimeUnit.SECONDS))
                        val plaintext = "test-$index".toByteArray()
                        plaintext to manager.encrypt(plaintext)
                    },
                )
            }
            start.countDown()
            for (result in results) {
                val (plaintext, encrypted) = result.get(30, TimeUnit.SECONDS)
                assertArrayEquals(plaintext, manager.decrypt(encrypted))
            }
        } finally {
            executor.shutdownNow()
            check(executor.awaitTermination(30, TimeUnit.SECONDS))
        }
    }
}
