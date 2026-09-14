package com.swyp.mangro.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.auth.login.LoginRoute
import com.swyp.mangro.feature.auth.terms.TermsRoute
import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Terms

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    navigateToHome: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
) {
    composable<Login> {
        LoginRoute(
            navigateToHome = { navController.navigate(Terms) },
            navigateToPrivacyPolicy = navigateToPrivacyPolicy,
            navigateToTerms = { navController.navigate(Terms) },
        )
    }
    composable<Terms> {
        TermsRoute(
            navigateToHome = navigateToHome,
            navigateToTermsDetail = { },
            onBackClick = { navController.popBackStack() },
        )
    }
}
