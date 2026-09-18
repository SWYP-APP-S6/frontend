package com.swyp.mangro.feature.consumer.store.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.core.model.recipe.Recipe
import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import com.swyp.mangro.core.model.store.StoreInfo
import com.swyp.mangro.core.utils.LocationProvider
import com.swyp.mangro.data.consumer.product.model.ProductDetail
import com.swyp.mangro.data.consumer.product.repository.ProductDetailRepository
import com.swyp.mangro.feature.consumer.store.navigation.ProductDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductDetailRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val productId: Long = savedStateHandle.toRoute<ProductDetailRoute>().productId.toLong()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<ProductDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadProductDetail()
    }

    fun handleAction(action: ProductDetailUiAction) {
        when (action) {
            is ProductDetailUiAction.OnWishButtonClick -> {
                _uiState.update { it.copy(isWishBottomSheetVisible = true) }
            }

            is ProductDetailUiAction.OnQuantityChange -> {
                _uiState.update { it.copy(wishState = it.wishState.copy(quantity = action.quantity)) }
            }

            is ProductDetailUiAction.OnWishConfirmClick -> {
                viewModelScope.launch {
                    val qty = _uiState.value.wishState.quantity
                    repository.registerHold(productId, qty).collect { result ->
                        result
                            .onSuccess { holdId ->
                                _uiState.update { it.copy(isWishBottomSheetVisible = false) }
                                _uiEvent.send(ProductDetailUiEvent.WishConfirmed(holdId.toString()))
                            }
                            .onFailure {
                                android.util.Log.e("ProductDetailViewModel", "registerHold failed", it)
                            }
                    }
                }
            }

            is ProductDetailUiAction.OnWishBottomSheetDismiss -> {
                _uiState.update { it.copy(isWishBottomSheetVisible = false) }
            }

            is ProductDetailUiAction.OnStoreInfoClick -> {
                viewModelScope.launch {
                    _uiState.value.productInfo?.store?.let { store ->
                        _uiEvent.send(ProductDetailUiEvent.OpenMapDirections(store))
                    }
                }
            }
        }
    }

    private fun loadProductDetail() {
        viewModelScope.launch {
            val location = locationProvider.fetchCurrentLocation()
            repository.fetchProduct(productId, location?.latitude, location?.longitude).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update { it.copy(productInfo = detail.toProductInfo()) }
                    }
                    .onFailure {
                        // TODO: 실패 처리
                    }
            }
        }
    }
}
private fun ProductDetail.toProductInfo(): ProductInfo = ProductInfo(
    product = Product(
        id = id.toString(),
        imageUrl = photoUrls.firstOrNull().orEmpty(),
        discountRate = discountRate,
        name = name,
        price = salePrice,
        originalPrice = originalPrice.takeIf { it != salePrice },
        category = ProductCategory.fromStoreCategory(category),
        remainingCount = availableQty,
    ),
    images = photoUrls.toPersistentList(),
    tags = tags.toPersistentList(),
    store = StoreInfo(
        id = store.id,
        name = store.name,
        address = store.address,
        phoneNumber = store.phone,
        distanceMeters = store.distanceMeters ?: 0,
        travelInfo = store.walkingMinutes?.let { "도보 ${it}분" } ?: "",
        closingTime = store.businessCloseTime.toHourMinuteOrEmpty(),
        latitude = store.latitude,
        longitude = store.longitude,
    ),
    recipes = recipes.map { recipe ->
        Recipe(
            id = recipe.id,
            difficulty = recipe.difficulty.toRecipeDifficulty(),
            name = recipe.title,
            ingredients = recipe.ingredientNames.toPersistentList(),
        )
    }.toPersistentList(),
)

private fun String?.toRecipeDifficulty(): RecipeDifficulty = when (this) {
    "EASY" -> RecipeDifficulty.LOW
    "NORMAL" -> RecipeDifficulty.MEDIUM
    "HARD" -> RecipeDifficulty.HIGH
    else -> RecipeDifficulty.LOW
}

private fun String.toHourMinuteOrEmpty(): String = takeIf { it.isNotBlank() }
    ?.split(":")
    ?.take(2)
    ?.joinToString(":")
    ?.let { "오늘 $it 까지" }
    ?: ""
