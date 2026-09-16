package com.swyp.mangro.feature.consumer.recipe.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val recipeId: Long = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<RecipeDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadRecipeDetail(recipeId)
    }

    fun handleAction(action: RecipeDetailUiAction) {
        when (action) {
            is RecipeDetailUiAction.OnWishClick -> {
                viewModelScope.launch {
                    val productId = uiState.value.detail?.relatedProductId ?: return@launch
                    _uiEvent.send(RecipeDetailUiEvent.NavigateToProductDetail(productId))
                }
            }
        }
    }

    private fun loadRecipeDetail(recipeId: Long) {
        viewModelScope.launch {
            _uiState.update {
                dummyRecipeDetailUiState
            }
        }
    }
}
