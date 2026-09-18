package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.SavedStateHandle
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.auth.terms.detail.TermsDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class TermsDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val document = TermsDocument(1, TermsKind.SERVICE, "서비스 이용약관", 1, true, "약관 본문")
    private var result: AuthResult<List<TermsDocument>> = AuthResult.Failure(AuthFailure.NETWORK)
    private val repository = object : TermsRepository {
        override fun fetchTermsDocuments() = flowOf(result)
        override fun fetchTermsDocument(id: Long) = flowOf(AuthResult.Success(document))
    }

    @Before fun setup() = Dispatchers.setMain(dispatcher)

    @After fun teardown() = Dispatchers.resetMain()

    @Test fun failedLoadStopsSpinnerAndAllowsRetry() = runTest(dispatcher) {
        val viewModel = TermsDetailViewModel(SavedStateHandle(mapOf("kind" to TermsKind.SERVICE.name)), repository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.failed)
        result = AuthResult.Success(listOf(document))
        viewModel.fetchTerm()
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.failed)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.failed)
        assertEquals(document.content, viewModel.uiState.value.content)
    }
}
