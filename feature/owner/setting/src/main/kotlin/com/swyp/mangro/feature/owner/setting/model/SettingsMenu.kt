package com.swyp.mangro.feature.owner.setting.model

import androidx.annotation.StringRes
import com.swyp.mangro.feature.owner.setting.R

enum class SettingsMenu(@param:StringRes val titleRes: Int) {
    TERMS_OF_SERVICE(R.string.owner_setting_terms),
    PRIVACY_POLICY(R.string.owner_setting_privacy),

    LOGOUT(R.string.owner_setting_logout),

    WITHDRAW(R.string.owner_setting_withdraw),
}
