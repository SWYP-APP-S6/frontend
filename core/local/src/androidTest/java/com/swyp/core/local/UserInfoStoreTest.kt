package com.swyp.core.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.core.crypto.impl.AndroidKeyStoreCryptoManagerImpl
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.impl.UserInfoStoreImpl
import java.io.File
import java.nio.file.Files
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserInfoStoreTest {
    private val crypto = AndroidKeyStoreCryptoManagerImpl()
    private val original = UserInfoFixtures.original
    private val replacement = UserInfoFixtures.replacement
    private lateinit var directory: File
    private lateinit var file: File
    private lateinit var job: Job
    private lateinit var preferences: DataStore<Preferences>
    private lateinit var store: UserInfoStoreImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = Files.createTempDirectory(context.noBackupFilesDir.toPath(), "user-info-store-test-").toFile()
        file = File(directory, "user_info.preferences_pb")
        openStore()
    }

    private fun openStore() {
        job = SupervisorJob()
        preferences = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job),
            produceFile = { file },
        )
        store = UserInfoStoreImpl(preferences, crypto)
    }

    @After
    fun tearDown() = runBlocking {
        job.cancelAndJoin()
        check(directory.deleteRecursively())
    }

    @Test
    fun emptyStoreAndRepeatedClearReturnNull() = runBlocking {
        assertNull(store.userInfo.first())
        store.clear()
        store.save(original)
        store.clear()
        store.clear()
        assertNull(store.userInfo.first())
        assertTrue(preferences.data.first().asMap().isEmpty())
    }

    @Test
    fun actualFieldsAreIndividuallyEncryptedAndRestoredAfterReopening() = runBlocking {
        store.save(original)
        val saved = preferences.data.first()
        assertEquals(UserInfoFixtures.plaintextFields.size, saved.asMap().size)
        UserInfoFixtures.plaintextFields.forEach { (name, value) ->
            val ciphertext = checkNotNull(saved[byteArrayPreferencesKey(name)])
            assertEquals(value, crypto.decrypt(ciphertext).decodeToString())
            assertFalse(ciphertext.contentEquals(value.encodeToByteArray()))
        }
        assertFalse(file.readBytes().toString(Charsets.UTF_8).contains(UserInfoFixtures.plaintextFields.getValue(UserInfoFixtures.REQUIRED_KEY)))
        job.cancelAndJoin()
        openStore()
        assertEquals(original, store.userInfo.first())
    }

    @Test
    fun replacementRemovesOldOptionalFields() = runBlocking {
        store.save(original)
        store.save(replacement)
        assertEquals(replacement, store.userInfo.first())
        UserInfoFixtures.removedKeys.forEach { name ->
            assertNull(preferences.data.first()[byteArrayPreferencesKey(name)])
        }
        job.cancelAndJoin()
        openStore()
        assertEquals(replacement, store.userInfo.first())
    }

    @Test
    fun encryptionFailurePreservesPreviousSnapshot() = runBlocking {
        store.save(original)
        val failingCrypto = object : CryptoManager by crypto {
            private var calls = 0

            override fun encrypt(plaintext: ByteArray): ByteArray {
                calls++
                if (calls == 2) throw GeneralSecurityException("Test encryption failure")
                return crypto.encrypt(plaintext)
            }
        }
        val failingStore = UserInfoStoreImpl(preferences, failingCrypto)
        assertTrue(runCatching { failingStore.save(replacement) }.exceptionOrNull() is GeneralSecurityException)
        assertEquals(original, store.userInfo.first())
    }

    @Test
    fun concurrentSavesNeverExposeMixedSnapshots() = runBlocking {
        val snapshots = listOf(original, replacement)
        store.save(original)
        coroutineScope {
            repeat(12) { index ->
                launch(Dispatchers.IO) {
                    store.save(snapshots[index % snapshots.size])
                    assertTrue(store.userInfo.first() in snapshots)
                }
            }
        }
    }

    @Test
    fun tamperedFieldFailsWithoutDeletingStoredData() = runBlocking {
        store.save(original)
        val key = byteArrayPreferencesKey(UserInfoFixtures.REQUIRED_KEY)
        preferences.edit { saved ->
            val damaged = checkNotNull(saved[key]).copyOf()
            damaged[damaged.lastIndex] = (damaged.last().toInt() xor 1).toByte()
            saved[key] = damaged
        }
        assertTrue(runCatching { store.userInfo.first() }.exceptionOrNull() is AEADBadTagException)
        assertNotNull(preferences.data.first()[key])
        store.clear()
        assertNull(store.userInfo.first())
    }

    @Test
    fun missingRequiredFieldFailsInsteadOfReturningNull() = runBlocking {
        store.save(original)
        preferences.edit { it.remove(byteArrayPreferencesKey(UserInfoFixtures.REQUIRED_KEY)) }
        assertTrue(runCatching { store.userInfo.first() }.exceptionOrNull() is IllegalStateException)
        assertFalse(preferences.data.first().asMap().isEmpty())
    }
}
