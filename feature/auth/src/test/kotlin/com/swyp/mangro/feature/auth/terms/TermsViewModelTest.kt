package com.swyp.mangro.feature.auth.terms

import com.swyp.mangro.data.auth.BuildConfig
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.auth.util.kind
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TermsViewModelTest {
    private val documents = TermsType.entries.mapIndexed { index, type -> TermsDocument(index + 1L, type.kind, type.name, 1, type.isRequired) }
    private val termsRepository = object : TermsRepository {
        override fun fetchTermsDocuments() = flowOf(AuthResult.Success(documents))
        override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
    }

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun confirmationRequiresConsentAndWaitsForServerBeforeNavigation() = runTest {
        org.junit.Assume.assumeFalse(BuildConfig.IS_OWNER)
        val pending = CompletableDeferred<AuthResult<Unit>>()
        var calls = 0
        val repository = object : AuthRepository {
            override fun hasSession(): Flow<Boolean> = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flow {
                calls++
                assertEquals(SignupConsents(true, true, true, true, false), consents)
                emit(pending.await())
            }
        }
        val viewModel = TermsViewModel(repository, termsRepository)
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        assertEquals(0, calls)
        TermsType.entries.filter { it.isRequired }.forEach { viewModel.handleAction(TermsUiAction.ItemToggled(it)) }
        repeat(2) { viewModel.handleAction(TermsUiAction.ConfirmClicked) }
        assertEquals(1, calls)
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(events.isEmpty())
        pending.complete(AuthResult.Success(Unit))
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(TermsUiEvent.SignupCompleted), events)
    }

    @Test fun missingSignupSessionKeepsTermsAndAsksForLogin() = runTest {
        org.junit.Assume.assumeFalse(BuildConfig.IS_OWNER)
        val repository = object : AuthRepository {
            override fun hasSession(): Flow<Boolean> = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flowOf(AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED))
        }
        val viewModel = TermsViewModel(repository, termsRepository)
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(TermsUiEvent.ShowSignupFailure(true)), events)
    }

    @Test fun changedTermsResetConsentAndDoNotSignup() = runTest {
        var listCalls = 0
        var signupCalls = 0
        val auth = object : AuthRepository {
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> {
                signupCalls++
                return flowOf(AuthResult.Success(Unit))
            }
        }
        val terms = object : TermsRepository {
            override fun fetchTermsDocuments() = flowOf(AuthResult.Success(if (listCalls++ == 0) documents else documents.map { it.copy(version = 2) }))
            override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
        }
        val viewModel = TermsViewModel(auth, terms)
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        assertEquals(0, signupCalls)
        assertFalse(viewModel.uiState.value.isRequiredAllChecked)
        assertEquals(listOf(TermsUiEvent.TermsChanged), events)
    }

    @Test fun ownerTermsLoadAndSubmitWithoutConsumerOnlyConsents() = runTest {
        org.junit.Assume.assumeTrue(BuildConfig.IS_OWNER)
        var submitted: SignupConsents? = null
        val auth = object : AuthRepository {
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> {
                submitted = consents
                return flowOf(AuthResult.Success(Unit))
            }
        }
        val terms = object : TermsRepository {
            override fun fetchTermsDocuments() = flowOf(AuthResult.Success(documents.filter { it.type in listOf(TermsType.SERVICE.kind, TermsType.PRIVACY.kind, TermsType.MARKETING.kind) }))
            override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
        }
        val viewModel = TermsViewModel(auth, terms)
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        assertTrue(viewModel.uiState.value.documentsLoaded)
        assertFalse(viewModel.uiState.value.loadFailed)
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        assertEquals(null, submitted)
        assertEquals(listOf(TermsUiEvent.OwnerOnboardingRequired(SignupConsents(true, true, false, false, true))), events)
    }

    @Test fun failedTermsLoadCanRetryAndDoesNotAllowConsent() = runTest {
        var listCalls = 0
        val auth = object : AuthRepository {
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("must not signup")
        }
        val terms = object : TermsRepository {
            override fun fetchTermsDocuments(): Flow<AuthResult<List<TermsDocument>>> = flowOf(if (listCalls++ == 0) AuthResult.Failure(AuthFailure.NETWORK) else AuthResult.Success(documents))
            override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
        }
        val viewModel = TermsViewModel(auth, terms)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { } }
        assertTrue(viewModel.uiState.value.loadFailed)
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        assertFalse(viewModel.uiState.value.isRequiredAllChecked)
        viewModel.handleAction(TermsUiAction.RetryClicked)
        assertTrue(viewModel.uiState.value.documentsLoaded)
        assertFalse(viewModel.uiState.value.loadFailed)
    }

    @Test fun ownerTermsDoNotRequireConsumerOnlyConsents() = runTest {
        org.junit.Assume.assumeTrue(BuildConfig.IS_OWNER)
        var submitted: SignupConsents? = null
        val auth = object : AuthRepository {
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> {
                submitted = consents
                return flowOf(AuthResult.Success(Unit))
            }
        }
        val ownerDocuments = documents.filter { it.type in setOf(TermsKind.SERVICE, TermsKind.PRIVACY_COLLECTION, TermsKind.MARKETING) }
        val terms = object : TermsRepository {
            override fun fetchTermsDocuments() = flowOf(AuthResult.Success(ownerDocuments))
            override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
        }
        val viewModel = TermsViewModel(auth, terms)
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        assertEquals(3, viewModel.uiState.value.items.size)
        viewModel.handleAction(TermsUiAction.ItemToggled(TermsType.SERVICE))
        viewModel.handleAction(TermsUiAction.ItemToggled(TermsType.PRIVACY))
        assertTrue(viewModel.uiState.value.isRequiredAllChecked)
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        assertEquals(null, submitted)
        assertEquals(listOf(TermsUiEvent.OwnerOnboardingRequired(SignupConsents(true, true, false, false, false))), events)
    }
}
