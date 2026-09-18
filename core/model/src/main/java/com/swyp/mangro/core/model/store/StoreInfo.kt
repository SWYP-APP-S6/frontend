package com.swyp.mangro.core.model.store

data class StoreInfo(
    val id: Long,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val distanceMeters: Int,
    val travelInfo: String,
    val closingTime: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
