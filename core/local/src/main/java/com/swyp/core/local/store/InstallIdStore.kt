package com.swyp.core.local.store

interface InstallIdStore {
    suspend fun getOrCreate(): String
}
