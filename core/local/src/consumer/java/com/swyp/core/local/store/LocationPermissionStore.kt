package com.swyp.core.local.store

import kotlinx.coroutines.flow.Flow

interface LocationPermissionStore {
    val isIntroShown: Flow<Boolean>
    val isRequested: Flow<Boolean>

    suspend fun markIntroShown()
    suspend fun markRequested()
}
