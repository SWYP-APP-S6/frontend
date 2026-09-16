package com.swyp.mangro.data.owner.store.repository

import com.swyp.mangro.data.owner.store.model.StoreRegistration
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun register(registration: StoreRegistration): Flow<Result<Unit>>
}
