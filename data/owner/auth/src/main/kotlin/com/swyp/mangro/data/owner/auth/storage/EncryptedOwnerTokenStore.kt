package com.swyp.mangro.data.owner.auth.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/** Tokens are encrypted with a non-exportable key and excluded from Android backup. */
internal class EncryptedOwnerTokenStore @Inject constructor(@ApplicationContext context: Context) : OwnerTokenStore {
    private val file = AtomicFile(File(context.noBackupFilesDir, "owner-session"))

    override fun read(): OwnerTokens? {
        if (!file.baseFile.exists()) return null
        val input = DataInputStream(ByteArrayInputStream(file.readFully()))
        val ivSize = input.readInt()
        require(ivSize == 12)
        val iv = ByteArray(ivSize).also(input::readFully)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return DataInputStream(ByteArrayInputStream(cipher.doFinal(input.readBytes()))).use {
            OwnerTokens(it.readUTF(), it.readUTF()).also { tokens ->
                require(tokens.accessToken.isNotBlank() && tokens.refreshToken.isNotBlank())
            }
        }
    }

    override fun write(tokens: OwnerTokens) {
        val plain = ByteArrayOutputStream().apply {
            DataOutputStream(this).use {
                it.writeUTF(tokens.accessToken)
                it.writeUTF(tokens.refreshToken)
            }
        }.toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = ByteArrayOutputStream().apply {
            DataOutputStream(this).use {
                it.writeInt(cipher.iv.size)
                it.write(cipher.iv)
                it.write(cipher.doFinal(plain))
            }
        }.toByteArray()
        val output = file.startWrite()
        try {
            output.write(encrypted)
            file.finishWrite(output)
        } catch (error: Exception) {
            file.failWrite(output)
            throw error
        }
    }

    override fun clear() = file.delete()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey(KEY_ALIAS, null) as? SecretKey ?: KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "mangro.owner.session"
    }
}
