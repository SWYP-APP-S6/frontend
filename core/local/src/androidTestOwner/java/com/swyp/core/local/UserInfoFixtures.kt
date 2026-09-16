package com.swyp.core.local

import com.swyp.core.local.model.BusinessDay
import com.swyp.core.local.model.UserInfo

internal object UserInfoFixtures {
    val original = UserInfo(
        id = 12345L,
        name = "망그로 채소가게",
        phone = "02-1234-5678",
        postalCode = "01234",
        address = "서울특별시 마포구 월드컵로 123",
        addressDetail = "1층 101호",
        businessOpenTime = "09:00",
        businessCloseTime = "21:30",
        businessDays = listOf(BusinessDay.MONDAY, BusinessDay.WEDNESDAY, BusinessDay.SUNDAY),
    )
    val replacement = original.copy(
        id = 67890L,
        name = "망그로 과일가게",
        postalCode = null,
        addressDetail = null,
        businessDays = emptyList(),
    )
    val plaintextFields = mapOf(
        "id" to "12345",
        "name" to "망그로 채소가게",
        "phone" to "02-1234-5678",
        "postalCode" to "01234",
        "address" to "서울특별시 마포구 월드컵로 123",
        "addressDetail" to "1층 101호",
        "businessOpenTime" to "09:00",
        "businessCloseTime" to "21:30",
        "businessDays" to "MONDAY,WEDNESDAY,SUNDAY",
    )
    val removedKeys = listOf("postalCode", "addressDetail")
    const val REQUIRED_KEY = "name"
}
