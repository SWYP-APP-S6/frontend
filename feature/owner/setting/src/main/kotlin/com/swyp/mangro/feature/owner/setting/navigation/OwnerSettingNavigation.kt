package com.swyp.mangro.feature.owner.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingRoute
import com.swyp.mangro.feature.owner.setting.screen.policy.OwnerPolicyRoute
import kotlinx.serialization.Serializable

@Serializable
data object OwnerSettingDestination

@Serializable
data class OwnerPolicyDestination(val policy: OwnerPolicy)

fun NavGraphBuilder.ownerSettingNavGraph(
    navigateToHome: () -> Unit,
    navigateToProducts: () -> Unit,
    navigateToPolicy: (OwnerPolicy) -> Unit,
    navigateBack: () -> Unit,
) {
    composable<OwnerSettingDestination> {
        OwnerSettingRoute(
            navigateToHome = navigateToHome,
            navigateToProducts = navigateToProducts,
            navigateToPolicy = navigateToPolicy,
        )
    }
    composable<OwnerPolicyDestination> {
        OwnerPolicyRoute(navigateBack = navigateBack)
    }
}
