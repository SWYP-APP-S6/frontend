package com.swyp.mangro.feature.owner.product.data

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import com.swyp.mangro.feature.owner.product.model.canCompletePickup
import com.swyp.mangro.feature.owner.product.model.pickupShortages
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

data class PickupSnapshot(
    val pickups: List<Pickup> = emptyList(),
    val stock: Map<String, Int> = emptyMap(),
    val storeName: String = "",
    val storePhone: String = "",
    val isDemo: Boolean = false,
)

/** API 연결 전 화면 간 상태를 공유하는 로컬 저장소. Release에는 샘플과 가짜 처리 결과가 없다. */
@Singleton
class OwnerPickupStore(initial: PickupSnapshot) {
    @Inject constructor() : this(initialPickupSnapshot())

    private val _snapshot = MutableStateFlow(initial.frozen())
    val snapshot = _snapshot.asStateFlow()
    private val catalogStock = mutableMapOf<String, Int>()

    @Synchronized
    fun updateProducts(products: List<OwnerProductModel>) {
        val changed = products.filter { catalogStock[it.id] != it.remainingQuantity }
        if (changed.isEmpty()) return
        changed.forEach { catalogStock[it.id] = it.remainingQuantity }
        _snapshot.value = _snapshot.value.copy(
            stock = _snapshot.value.stock + changed.associate { it.id to it.remainingQuantity },
        ).frozen()
    }

    @Synchronized
    fun complete(id: String, now: Long = System.currentTimeMillis()): Boolean {
        val state = snapshot.value
        val pickup = state.pickups.find { it.id == id } ?: return false
        if (!state.isDemo || !canCompletePickup(pickup, state.pickups, state.stock, now)) return false
        _snapshot.value = state.copy(
            pickups = state.pickups.map {
                if (it.id == id) it.copy(status = PickupStatus.COMPLETED, completedAt = now, isNew = false) else it
            },
            stock = state.stock + (pickup.productId to (state.stock.getValue(pickup.productId) - pickup.quantity)),
        ).frozen()
        return true
    }

    @Synchronized
    fun cancel(ids: Set<String>, now: Long = System.currentTimeMillis()): Boolean {
        val state = snapshot.value
        val current = pickupShortages(state.pickups, state.stock, now).flatMap { it.targets }.map { it.id }.toSet()
        if (!state.isDemo || ids.isEmpty() || !current.containsAll(ids)) return false
        _snapshot.value = state.copy(
            pickups = state.pickups.map { if (it.id in ids) it.copy(status = PickupStatus.UNAVAILABLE, isNew = false) else it },
        ).frozen()
        return true
    }

    private fun PickupSnapshot.frozen() = copy(
        pickups = Collections.unmodifiableList(pickups.toList()),
        stock = Collections.unmodifiableMap(stock.toMap()),
    )
}

internal fun pickupTime() = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(1_000)
    }
}
