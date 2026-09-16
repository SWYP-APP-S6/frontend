package com.swyp.mangro.data.user.repository

import com.swyp.mangro.data.user.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun fetchMe(): Flow<Result<UserProfile>>
}
