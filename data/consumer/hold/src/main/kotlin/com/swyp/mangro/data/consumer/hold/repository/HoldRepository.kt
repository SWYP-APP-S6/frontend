package com.swyp.mangro.data.consumer.hold.repository

import com.swyp.mangro.data.consumer.hold.model.HoldDetail
import com.swyp.mangro.data.consumer.hold.model.HoldHistory
import kotlinx.coroutines.flow.Flow

interface HoldRepository {
    fun fetchHolds(page: Int = 0, size: Int = 20): Flow<Result<HoldHistory>>
    fun fetchHold(holdId: Long): Flow<Result<HoldDetail>>
    fun cancelHold(holdId: Long): Flow<Result<HoldDetail>>
}
