package com.swyp.core.crypto.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.swyp.core.crypto.manager.CryptoManager
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class AndroidKeyStoreCryptoManagerImpl @Inject constructor() : CryptoManager {
    private val secretKey: SecretKey by lazy { getOrCreateSecretKey() }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.getKey(ALIAS, null)?.let { return it as SecretKey }

        val paramsBuilder = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )

        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        check(iv.size == IV_SIZE) { "Unexpected GCM IV length: ${iv.size}" }

        val header = byteArrayOf(FORMAT_VERSION) + iv
        cipher.updateAAD(header)

        return header + cipher.doFinal(plaintext)
    }

    override fun decrypt(ciphertext: ByteArray): ByteArray {
        require(ciphertext.size >= HEADER_SIZE + TAG_SIZE_BYTES) { "Invalid encrypted data length" }
        require(ciphertext[0] == FORMAT_VERSION) { "Unsupported encrypted data version" }

        val header = ciphertext.copyOfRange(0, HEADER_SIZE)
        val iv = header.copyOfRange(1, HEADER_SIZE)
        val cipher = getCipher()

        cipher.init(Cipher.DECRYPT_MODE, getExistingSecretKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
        cipher.updateAAD(header)
        return cipher.doFinal(ciphertext, HEADER_SIZE, ciphertext.size - HEADER_SIZE)
    }

    private fun getExistingSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        return keyStore.getKey(ALIAS, null) as? SecretKey ?: throw KeyStoreException("Encryption key is unavailable")
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"

        const val ALIAS = "mangro.local.aes.v1"
        const val KEY_SIZE = 256
        const val FORMAT_VERSION: Byte = 1
        const val IV_SIZE = 12
        const val TAG_SIZE_BITS = 128
        const val TAG_SIZE_BYTES = TAG_SIZE_BITS / 8
        const val HEADER_SIZE = 1 + IV_SIZE

        const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    }
}
