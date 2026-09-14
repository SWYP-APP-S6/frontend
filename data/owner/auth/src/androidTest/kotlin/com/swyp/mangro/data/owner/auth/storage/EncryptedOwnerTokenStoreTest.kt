package com.swyp.mangro.data.owner.auth.storage

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class EncryptedOwnerTokenStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val store = EncryptedOwnerTokenStore(context)

    @After fun cleanup() {
        store.clear()
    }

    @Test fun tokensSurviveStoreRecreationAndAreEncryptedOutsideBackup() {
        store.write(OwnerTokens("test-access-token", "test-refresh-token"))
        val restored = EncryptedOwnerTokenStore(context).read()
        assertEquals("test-access-token", restored?.accessToken)
        assertEquals("test-refresh-token", restored?.refreshToken)
        val encrypted = File(context.noBackupFilesDir, "owner-session").readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(encrypted.contains("test-access-token"))
        assertFalse(encrypted.contains("test-refresh-token"))
        store.clear()
        assertNull(EncryptedOwnerTokenStore(context).read())
    }
}
