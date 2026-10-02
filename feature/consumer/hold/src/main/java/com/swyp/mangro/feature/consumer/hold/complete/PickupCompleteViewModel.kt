package com.swyp.mangro.feature.consumer.hold.complete

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.core.designsystem.component.card.purchase.PurchaseInfo
import com.swyp.mangro.core.model.recipe.Recipe
import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import com.swyp.mangro.data.consumer.product.model.ProductRecipe
import com.swyp.mangro.data.consumer.product.repository.ProductDetailRepository
import com.swyp.mangro.feature.consumer.hold.navigation.PickupCompleteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PickupCompleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HoldRepository,
    private val productRepository: ProductDetailRepository,
) : ViewModel() {
    private val holdId: Long = savedStateHandle.toRoute<PickupCompleteDestination>().holdId.toLong()

    private val _uiState = MutableStateFlow(PickupCompleteUiState())
    val uiState: StateFlow<PickupCompleteUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<PickupCompleteUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadPickupComplete()
    }

    fun handleAction(action: PickupCompleteUiAction) {
        when (action) {
            is PickupCompleteUiAction.OnRecipeClick -> {
                viewModelScope.launch {
                    _uiEvent.send(PickupCompleteUiEvent.NavigateToRecipeDetail(action.recipeId))
                }
            }
            is PickupCompleteUiAction.OnMenuClick -> {
                viewModelScope.launch {
                    _uiEvent.send(PickupCompleteUiEvent.NavigateToMenu(action.menu))
                }
            }
        }
    }

    private fun loadPickupComplete() {
        viewModelScope.launch {
            repository.fetchHold(holdId).collect { result ->
                result
                    .onSuccess { detail ->
                        val firstItem = detail.items.firstOrNull()
                        _uiState.update {
                            it.copy(
                                info = PickupCompleteInfo(
                                    purchaseInfo = PurchaseInfo(
                                        date = (detail.completedAtMillis ?: detail.heldAtMillis).toDateLabel(),
                                        storeName = detail.store.name,
                                        productName = firstItem?.name.orEmpty(),
                                        quantity = detail.totalQty,
                                        price = detail.totalPrice,
                                    ),
                                    recommendedRecipes = persistentListOf(),
                                ),
                            )
                        }
                        firstItem?.let { loadRecommendedRecipes(it.productId) }
                    }
                    .onFailure {
                        // TODO: 수령 완료 정보 조회 실패 처리
                    }
            }
        }
    }

    private fun loadRecommendedRecipes(productId: Long) {
        viewModelScope.launch {
            productRepository.fetchProduct(productId).collect { result ->
                result
                    .onSuccess { product ->
                        val recipes = product.recipes.map { it.toRecipe() }.toPersistentList()
                        _uiState.update { state ->
                            state.copy(info = state.info?.copy(recommendedRecipes = recipes))
                        }
                    }
                    .onFailure { error ->
                        Log.e("PickupRecipe", "fetchProduct failed", error)
                    }
            }
        }
    }
}

private fun ProductRecipe.toRecipe(): Recipe = Recipe(
    id = id,
    difficulty = when (difficulty?.uppercase()) {
        "EASY" -> RecipeDifficulty.LOW
        "HARD" -> RecipeDifficulty.HIGH
        else -> RecipeDifficulty.MEDIUM
    },
    name = title,
    ingredients = ingredientNames.toPersistentList(),
)

private fun Long.toDateLabel(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
