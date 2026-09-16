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
import com.swyp.core.local.impl.AuthStoreImpl
import com.swyp.core.local.model.AuthKey
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
class AuthStoreTest {
    private val crypto = AndroidKeyStoreCryptoManagerImpl()
    private val tokens = AuthKey(AuthTokenFixtures.ACCESS_TOKEN, AuthTokenFixtures.REFRESH_TOKEN)
    private lateinit var directory: File
    private lateinit var file: File
    private lateinit var job: Job
    private lateinit var preferences: DataStore<Preferences>
    private lateinit var store: AuthStoreImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = Files.createTempDirectory(context.noBackupFilesDir.toPath(), "auth-store-test-").toFile()
        file = File(directory, "auth.preferences_pb")
        openStore()
    }

    private fun openStore() {
        job = SupervisorJob()
        preferences = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job),
            produceFile = { file },
        )
        store = AuthStoreImpl(preferences, crypto)
    }

    @After
    fun tearDown() = runBlocking {
        job.cancelAndJoin()
        check(directory.deleteRecursively())
    }

    @Test
    fun emptyStoreAndRepeatedClearReturnNoSession() = runBlocking {
        assertNull(store.authKey.first())
        store.clear()
        store.save(tokens)
        store.clear()
        store.clear()
        assertNull(store.authKey.first())
        assertTrue(preferences.data.first().asMap().isEmpty())
    }

    @Test
    fun jwtValuesAreStoredAsSeparateCiphertextsAndRestoredAfterReopeningFile() = runBlocking {
        store.save(tokens)
        val saved = preferences.data.first()
        val access = checkNotNull(saved[byteArrayPreferencesKey("accessToken")])
        val refresh = checkNotNull(saved[byteArrayPreferencesKey("refreshToken")])
        assertEquals(2, saved.asMap().size)
        assertEquals(tokens.accessToken, crypto.decrypt(access).decodeToString())
        assertEquals(tokens.refreshToken, crypto.decrypt(refresh).decodeToString())
        assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains(tokens.accessToken))
        assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains(tokens.refreshToken))

        job.cancelAndJoin()
        openStore()
        assertEquals(tokens, store.authKey.first())
    }

    @Test
    fun replacingTokensUpdatesBothValues() = runBlocking {
        store.save(tokens)
        val refreshed = AuthKey(tokens.refreshToken, tokens.accessToken)
        store.save(refreshed)
        assertEquals(refreshed, store.authKey.first())
    }

    @Test
    fun failedRefreshEncryptionPreservesPreviouslySavedPair() = runBlocking {
        store.save(tokens)
        val failingCrypto = object : CryptoManager by crypto {
            private var calls = 0

            override fun encrypt(plaintext: ByteArray): ByteArray {
                calls++
                if (calls == 2) throw GeneralSecurityException("Test encryption failure")
                return crypto.encrypt(plaintext)
            }
        }
        val failingStore = AuthStoreImpl(preferences, failingCrypto)
        val failure = runCatching { failingStore.save(AuthKey(tokens.refreshToken, tokens.accessToken)) }.exceptionOrNull()
        assertTrue(failure is GeneralSecurityException)
        assertEquals(tokens, store.authKey.first())
    }

    @Test
    fun concurrentSavesNeverExposeMixedTokenPairs() = runBlocking {
        val pairs = listOf(tokens, AuthKey(tokens.refreshToken, tokens.accessToken))
        store.save(tokens)
        coroutineScope {
            repeat(12) { index ->
                launch(Dispatchers.IO) {
                    store.save(pairs[index % pairs.size])
                    assertTrue(store.authKey.first() in pairs)
                }
            }
        }
        assertTrue(store.authKey.first() in pairs)
    }

    @Test
    fun tamperedTokenFailsWithoutErasingDataAndCanBeCleared() = runBlocking {
        store.save(tokens)
        val key = byteArrayPreferencesKey("accessToken")
        preferences.edit { saved ->
            val damaged = checkNotNull(saved[key]).copyOf()
            damaged[damaged.lastIndex] = (damaged.last().toInt() xor 1).toByte()
            saved[key] = damaged
        }
        val failure = runCatching { store.authKey.first() }.exceptionOrNull()
        assertTrue(failure is AEADBadTagException)
        assertNotNull(preferences.data.first()[key])
        store.clear()
        assertNull(store.authKey.first())
    }

    @Test
    fun conditionalRefreshNeverRestoresLoggedOutOrReplacedSession() = runBlocking {
        val updated = AuthKey(tokens.refreshToken, tokens.accessToken)
        store.save(tokens)
        assertTrue(store.replaceIfMatches(tokens, updated))
        assertEquals(updated, store.authKey.first())
        assertFalse(store.replaceIfMatches(tokens, tokens))
        assertEquals(updated, store.authKey.first())
        store.clear()
        assertFalse(store.replaceIfMatches(updated, tokens))
        assertNull(store.authKey.first())
    }

    @Test
    fun incompletePairFailsInsteadOfBeingReportedAsLoggedOut() = runBlocking {
        store.save(tokens)
        preferences.edit { it.remove(byteArrayPreferencesKey("refreshToken")) }
        val failure = runCatching { store.authKey.first() }.exceptionOrNull()
        assertTrue(failure is IllegalStateException)
        assertNotNull(preferences.data.first()[byteArrayPreferencesKey("accessToken")])
    }
}
