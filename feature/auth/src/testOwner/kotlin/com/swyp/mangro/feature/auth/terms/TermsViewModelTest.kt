package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val documents = listOf(
        OwnerTerm(7, "SERVICE", 1, "이용약관", TermsRequirement.REQUIRED, "2026-09-16"),
        OwnerTerm(9, "MARKETING", 1, "마케팅 동의", TermsRequirement.OPTIONAL, "2026-09-16"),
        OwnerTerm(10, "PRIVACY_POLICY", 1, "개인정보처리방침", TermsRequirement.NOTICE, "2026-09-16"),
    )
    private var response: TermsResult<List<OwnerTerm>> = TermsResult.Success(documents)
    private val repository = object : OwnerTermsRepository {
        override suspend fun fetchTerms() = response
        override suspend fun fetchTerm(id: Long): TermsResult<OwnerTermDetail> = TermsResult.Failure(TermsFailure.NOT_FOUND)
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = TermsViewModel(handle, repository).also { store.put("terms", it) }

    @Test fun loadingAndErrorCannotContinueAndRetryRestoresTheList() = runTest {
        response = TermsResult.Failure(TermsFailure.NETWORK)
        val model = model()
        assertFalse(model.uiState.value.isRequiredAllChecked)
        runCurrent()
        assertEquals(TermsFailure.NETWORK, model.uiState.value.failure)
        model.handleAction(TermsUiAction.AllAgreeClicked)
        assertFalse(model.uiState.value.isRequiredAllChecked)
        response = TermsResult.Success(documents)
        model.handleAction(TermsUiAction.RetryClicked)
        runCurrent()
        assertEquals(documents, model.uiState.value.items.map { it.document })
    }

    @Test fun allAgreeExcludesNoticeAndCanBeCleared() = runTest {
        val model = model()
        runCurrent()
        model.handleAction(TermsUiAction.AllAgreeClicked)
        assertEquals(listOf(true, true, false), model.uiState.value.items.map { it.isChecked })
        assertTrue(model.uiState.value.isAllChecked)
        model.handleAction(TermsUiAction.ItemToggled(10))
        assertFalse(model.uiState.value.items.last().isChecked)
        model.handleAction(TermsUiAction.AllAgreeClicked)
        assertTrue(model.uiState.value.items.none { it.isChecked })
    }

    @Test fun onlyRequiredConsentAllowsCompletionAndNoticeStillOpensDetail() = runTest {
        val model = model()
        runCurrent()
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch { model.event.collect { events.add(it) } }
        model.handleAction(TermsUiAction.ConfirmClicked)
        runCurrent()
        assertTrue(events.isEmpty())
        model.handleAction(TermsUiAction.ItemToggled(7))
        assertTrue(model.uiState.value.isRequiredAllChecked)
        assertFalse(model.uiState.value.isAllChecked)
        model.handleAction(TermsUiAction.ConfirmClicked)
        model.handleAction(TermsUiAction.ItemDetailClicked(10))
        runCurrent()
        assertEquals(listOf(TermsUiEvent.NavigateToHome, TermsUiEvent.NavigateToTermsDetail(10)), events)
    }

    @Test fun selectionRestoresOnlyForTheSameDocumentVersion() = runTest {
        val handle = SavedStateHandle()
        val first = model(handle)
        runCurrent()
        first.handleAction(TermsUiAction.AllAgreeClicked)
        response = TermsResult.Success(documents.map { if (it.id == 7L) it.copy(version = 2) else it })
        val restored = model(SavedStateHandle(mapOf("selectedTerms" to handle.get<ArrayList<String>>("selectedTerms"))))
        runCurrent()
        assertEquals(listOf(false, true, false), restored.uiState.value.items.map { it.isChecked })
        assertFalse(restored.uiState.value.isRequiredAllChecked)
    }
}
