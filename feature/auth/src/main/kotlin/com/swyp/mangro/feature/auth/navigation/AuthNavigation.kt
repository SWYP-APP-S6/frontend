package com.swyp.mangro.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.auth.login.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
data object Login

fun NavGraphBuilder.authNavGraph(
    navigateToHome: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
) {
    composable<Login> {
        LoginRoute(
            navigateToHome = navigateToHome,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy,
        )
    }
}
