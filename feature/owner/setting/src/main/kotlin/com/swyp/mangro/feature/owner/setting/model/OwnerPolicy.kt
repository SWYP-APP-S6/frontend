package com.swyp.mangro.feature.owner.setting.model

import androidx.annotation.StringRes
import com.swyp.mangro.feature.owner.setting.R
import kotlinx.serialization.Serializable

@Serializable
enum class OwnerPolicy(@param:StringRes val titleRes: Int) {
    TERMS_OF_SERVICE(R.string.owner_setting_terms),
    PRIVACY_POLICY(R.string.owner_setting_privacy),
}
