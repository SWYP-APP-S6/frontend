package com.swyp.mangro.feature.owner.product.screen.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.product.model.ProductRegistration
import com.swyp.mangro.data.owner.product.repository.ProductRepository
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductRegistrationViewModel internal constructor(
    private val repository: StoreRepository,
    private val products: ProductRepository,
    private val clock: Clock,
) : ViewModel() {
    @Inject constructor(repository: StoreRepository, products: ProductRepository) : this(repository, products, Clock.system(ZoneId.of("Asia/Seoul")))
    private val _uiState = MutableStateFlow(ProductRegistrationState())
    val uiState = _uiState.asStateFlow()
    private val _save = Channel<OwnerProductModel>(Channel.BUFFERED)
    val save = _save.receiveAsFlow()
    private var job: Job? = null
    private var submitted = false

    fun refresh() = checkApproval(null)

    fun dismissError() {
        _uiState.update { it.copy(registrationFailed = false) }
    }

    fun submit(product: OwnerProductModel) {
        if (!_uiState.value.isChecking && _uiState.value.store?.canRegisterProduct == true) checkApproval(product)
    }

    private fun checkApproval(product: OwnerProductModel?) {
        if (job?.isActive == true || submitted) return
        job = viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, checkFailed = false, registrationFailed = false) }
            val result = repository.fetchMyStore().first()
            val store = result.getOrNull()

            _uiState.update {
                it.copy(store = store ?: it.store, isChecking = false, checkFailed = result.isFailure, registrationFailed = product != null && result.isFailure)
            }
            if (product != null && store?.canRegisterProduct == true) {
                _uiState.update { it.copy(isSubmitting = true) }
                try {
                    val zone = ZoneId.of("Asia/Seoul")
                    val pickupTime = LocalTime.parse(product.pickupEndTime)
                    require(!pickupTime.isBefore(LocalTime.parse(store.businessOpenTime)))
                    require(!pickupTime.isAfter(LocalTime.parse(store.businessCloseTime)))
                    val deadline = LocalDate.now(clock).atTime(pickupTime).atZone(zone).toOffsetDateTime()
                    if (!deadline.isAfter(OffsetDateTime.now(clock))) {
                        _uiState.update { it.copy(registrationFailed = true) }
                        return@launch
                    }
                    val registered = products.register(
                        ProductRegistration(
                            name = product.name,
                            photos = product.photos,
                            quantity = product.initialQuantity,
                            originalPrice = product.originalPrice,
                            salePrice = product.salePrice,
                            pickupEndAt = deadline.toString(),
                            category = requireNotNull(store.categories.singleOrNull()),
                            ingredientTags = emptyList(),
                        ),
                    ).first()
                    val saved = registered.getOrNull()
                    if (saved == null) {
                        _uiState.update { it.copy(registrationFailed = true) }
                    } else {
                        val savedProduct = OwnerProductModel(
                            id = saved.id.toString(),
                            name = saved.name,
                            photos = listOf(saved.photoUrl).filter { it.isNotBlank() },
                            originalPrice = saved.originalPrice,
                            salePrice = saved.salePrice,
                            initialQuantity = saved.initialQuantity,
                            remainingQuantity = saved.stockQuantity,
                            reservedQuantity = saved.heldQuantity.toLong(),
                            pickedUpQuantity = saved.completedQuantity.toLong(),
                            pickupEndTime = OffsetDateTime.parse(saved.pickupEndAt).atZoneSameInstant(zone).toLocalTime().toString().take(5),
                            tags = emptyList(),
                        )
                        submitted = true
                        _save.send(savedProduct)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    _uiState.update { it.copy(registrationFailed = true) }
                } finally {
                    _uiState.update { it.copy(isSubmitting = false) }
                }
            }
        }
    }
}
