package com.swyp.mangro.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.feature.auth.login.LoginRoute
import com.swyp.mangro.feature.auth.terms.TermsRoute
import com.swyp.mangro.feature.auth.terms.TermsType
import com.swyp.mangro.feature.auth.terms.detail.TermsDetailRoute
import com.swyp.mangro.feature.auth.util.kind
import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Terms

@Serializable
data class TermsDetail(val kind: String)

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    navigateToHome: () -> Unit,
    onOwnerOnboarding: ((SignupConsents) -> Unit)? = null,
) {
    composable<Login> {
        LoginRoute(
            navigateToHome = navigateToHome,
            navigateToPrivacyPolicy = {
                navController.navigate(TermsDetail(TermsType.PRIVACY.kind.name))
            },
            navigateToTerms = { navController.navigate(Terms) },
        )
    }
    composable<Terms> {
        TermsRoute(
            onSignupCompleted = navigateToHome,
            onOwnerOnboarding = onOwnerOnboarding,
            navigateToTermsDetail = { navController.navigate(TermsDetail(it.kind.name)) },
            navigateToLogin = { navController.popBackStack<Login>(inclusive = false) },
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<TermsDetail> {
        TermsDetailRoute(onBackClick = { navController.popBackStack() })
    }
}
