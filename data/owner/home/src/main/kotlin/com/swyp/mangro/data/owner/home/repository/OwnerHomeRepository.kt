package com.swyp.mangro.data.owner.home.repository

import com.swyp.mangro.data.owner.home.model.OwnerHome
import kotlinx.coroutines.flow.Flow

interface OwnerHomeRepository {
    fun fetchHome(): Flow<Result<OwnerHome>>
    fun markAsPickedUp(holdId: Long): Flow<Result<Unit>>
}
