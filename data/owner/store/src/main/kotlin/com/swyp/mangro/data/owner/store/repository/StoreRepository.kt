package com.swyp.mangro.data.owner.store.repository

import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import kotlinx.coroutines.flow.Flow

interface StoreRepository {

    @Deprecated("Legacy Method, need to be delete it")
    fun fetchMyStore(): Flow<Result<OwnerStore>>

    fun fetchMyStoreInformation(): Flow<OwnerStore>

    fun register(registration: StoreRegistration): Flow<Result<Unit>>
}
