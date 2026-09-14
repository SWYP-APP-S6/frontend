package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.ViewModelStore
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
    private lateinit var viewModel: TermsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = TermsViewModel().also { store.put("terms", it) }
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun confirmWithoutRequiredConsentDoesNotNavigate() = runTest {
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        runCurrent()
        assertTrue(events.isEmpty())
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        viewModel.handleAction(TermsUiAction.ItemToggled(TermsType.PRIVACY_THIRD_PARTY))
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        runCurrent()
        assertFalse(viewModel.uiState.value.isRequiredAllChecked)
        assertTrue(events.isEmpty())
    }

    @Test
    fun requiredConsentWithoutMarketingAllowsCompletion() = runTest {
        val events = mutableListOf<TermsUiEvent>()
        backgroundScope.launch { viewModel.event.collect { events.add(it) } }
        TermsType.entries.filter { it.isRequired }.forEach {
            viewModel.handleAction(TermsUiAction.ItemToggled(it))
        }
        assertTrue(viewModel.uiState.value.isRequiredAllChecked)
        assertFalse(viewModel.uiState.value.isAllChecked)
        viewModel.handleAction(TermsUiAction.ConfirmClicked)
        runCurrent()
        assertEquals(listOf(TermsUiEvent.NavigateToHome), events)
    }

    @Test
    fun allAgreeCanBeClearedAgain() {
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        assertTrue(viewModel.uiState.value.isAllChecked)
        viewModel.handleAction(TermsUiAction.AllAgreeClicked)
        assertTrue(viewModel.uiState.value.items.none { it.isChecked })
        assertFalse(viewModel.uiState.value.isRequiredAllChecked)
    }
}
