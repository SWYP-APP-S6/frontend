package com.swyp.mangro.feature.owner.setting.screen.policy

import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

data class OwnerPolicyUiState(
    val policy: OwnerPolicy = OwnerPolicy.TERMS_OF_SERVICE,
    val url: String? = null,
)
