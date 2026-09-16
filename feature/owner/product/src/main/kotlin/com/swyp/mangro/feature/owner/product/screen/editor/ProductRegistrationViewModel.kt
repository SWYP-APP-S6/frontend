package com.swyp.mangro.feature.owner.product.screen.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductRegistrationViewModel @Inject constructor(private val repository: StoreRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductRegistrationState())
    val uiState = _uiState.asStateFlow()
    private val _save = Channel<OwnerProductModel>(Channel.BUFFERED)
    val save = _save.receiveAsFlow()
    private var job: Job? = null
    private var submitted = false

    fun refresh() = checkApproval(null)

    fun submit(product: OwnerProductModel) {
        if (!_uiState.value.isChecking && _uiState.value.store?.canRegisterProduct == true) checkApproval(product)
    }

    private fun checkApproval(product: OwnerProductModel?) {
        if (job?.isActive == true || submitted) return
        job = viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, checkFailed = false) }
            val result = repository.fetchMyStore().first()
            val store = result.getOrNull()
            _uiState.value = ProductRegistrationState(store = store, isChecking = false, checkFailed = result.isFailure)
            if (product != null && store?.canRegisterProduct == true) {
                submitted = true
                _save.send(product)
            }
        }
    }
}
