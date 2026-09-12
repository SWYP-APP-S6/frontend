package com.swyp.mangro.feature.owner.onboarding.util

import com.swyp.mangro.feature.owner.onboarding.model.StoreRegistrationModel

/** Host-provided registration operation. Normal return means the request was accepted. */
fun interface StoreRegistrationSubmitter {
    suspend fun submit(registration: StoreRegistrationModel)
}
