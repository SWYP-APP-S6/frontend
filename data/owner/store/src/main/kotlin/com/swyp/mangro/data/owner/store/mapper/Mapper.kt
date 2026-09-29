package com.swyp.mangro.data.owner.store.mapper

import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.remote.owner.model.RegisterStoreRequest
import com.swyp.mangro.remote.owner.model.StoreDetailResponse

internal fun StoreRegistration.toRequest(): RegisterStoreRequest {
    require(name.isNotBlank() && name.length <= 100)
    require(postalCode.matches(Regex("[0-9]{5}")))
    require(address.isNotBlank() && address.length <= 255 && addressDetail.length <= 255)
    require(phone.matches(Regex("0[0-9]{8,10}")))
    require(openingMinutes in 0..1380 && closingMinutes in openingMinutes..1380)
    require(openingMinutes % 60 == 0 && closingMinutes % 60 == 0)
    require(businessDays.isNotEmpty() && businessDays.all { it in 0..6 })
    val days = listOf(
        RegisterStoreRequest.BusinessDays.SUNDAY,
        RegisterStoreRequest.BusinessDays.MONDAY,
        RegisterStoreRequest.BusinessDays.TUESDAY,
        RegisterStoreRequest.BusinessDays.WEDNESDAY,
        RegisterStoreRequest.BusinessDays.THURSDAY,
        RegisterStoreRequest.BusinessDays.FRIDAY,
        RegisterStoreRequest.BusinessDays.SATURDAY,
    )
    fun time(minutes: Int) = "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}:00"
    return RegisterStoreRequest(
        name = name,
        categories = setOf(RegisterStoreRequest.Categories.valueOf(categoryId)),
        postalCode = postalCode,
        address = address,
        addressDetail = addressDetail,
        phone = phone,
        businessOpenTime = time(openingMinutes),
        businessCloseTime = time(closingMinutes),
        businessDays = businessDays.sorted().map { days[it] }.toSet(),
        businessRegistrationNumber = "",
        applicationNote = "",
    )
}

internal fun StoreDetailResponse.toOwnerStore(): OwnerStore = OwnerStore(
    id = this.id,
    name = this.name,
    categories = this.categories.map { item -> item.value },
    status = StoreApprovalStatus.from(this.status),
    businessOpenTime = this.businessOpenTime,
    businessCloseTime = this.businessCloseTime,
    phone = this.phone,
)
