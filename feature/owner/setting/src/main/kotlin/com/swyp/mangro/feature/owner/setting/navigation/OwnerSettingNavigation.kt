package com.swyp.mangro.feature.owner.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.owner.setting.model.SettingsMenu
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingRoute
import com.swyp.mangro.feature.owner.setting.screen.policy.OwnerPolicyRoute
import kotlinx.serialization.Serializable

@Serializable
data object OwnerSettingDestination

@Serializable
data class OwnerPolicyDestination(val policy: SettingsMenu)

fun NavGraphBuilder.ownerSettingNavGraph(
    navigateToPolicy: (SettingsMenu) -> Unit,
    navigateBack: () -> Unit,
    navigateToLogin: () -> Unit,
) {
    composable<OwnerSettingDestination> {
        OwnerSettingRoute(
            navigateToLogin = navigateToLogin,
            navigateToPolicy = navigateToPolicy,
        )
    }
    composable<OwnerPolicyDestination> {
        OwnerPolicyRoute(navigateBack = navigateBack)
    }
}
