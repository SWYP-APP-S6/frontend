package com.swyp.mangro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.feature.owner.home.screen.OwnerHomePickupState
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class OwnerMainViewModel @Inject constructor(
    private val pickupStore: OwnerPickupStore,
) : ViewModel() {
    val homePickups = combine(
        pickupStore.snapshot,
        flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(1_000)
            }
        },
    ) { snapshot, now -> snapshot.toHomePickups(now) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OwnerHomePickupState())

    fun completePickup(id: String): Boolean = pickupStore.complete(id)
}
