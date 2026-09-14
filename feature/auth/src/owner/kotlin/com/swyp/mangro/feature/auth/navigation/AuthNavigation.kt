package com.swyp.mangro.feature.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.auth.login.LoginRoute
import com.swyp.mangro.feature.auth.terms.TermsRoute
import com.swyp.mangro.feature.auth.terms.TermsUiAction
import com.swyp.mangro.feature.auth.terms.TermsViewModel
import com.swyp.mangro.feature.auth.terms.detail.TermDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Terms

@Serializable
data class OwnerTermDetailDestination(val documentId: Long)

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    navigateToHome: () -> Unit,
    navigateToOnboarding: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
) {
    composable<Login> {
        LoginRoute(
            navigateToHome = navigateToHome,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy,
            navigateToTerms = { navController.navigate(Terms) { launchSingleTop = true } },
        )
    }
    composable<Terms> { entry ->
        val refresh by entry.savedStateHandle.getStateFlow("refreshTerms", false).collectAsStateWithLifecycle()
        val viewModel: TermsViewModel = hiltViewModel()
        LaunchedEffect(refresh) {
            if (refresh) {
                viewModel.handleAction(TermsUiAction.RetryClicked)
                entry.savedStateHandle["refreshTerms"] = false
            }
        }
        TermsRoute(
            viewModel = viewModel,
            navigateToLogin = {
                navController.navigate(Login) {
                    popUpTo<Login> { inclusive = true }
                    launchSingleTop = true
                }
            },
            navigateToOnboarding = navigateToOnboarding,
            navigateToTermsDetail = { id -> navController.navigate(OwnerTermDetailDestination(id)) { launchSingleTop = true } },
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<OwnerTermDetailDestination> {
        TermDetailRoute(
            navigateBack = { navController.popBackStack() },
            refreshTerms = {
                navController.previousBackStackEntry?.savedStateHandle?.set("refreshTerms", true)
                navController.popBackStack()
            },
        )
    }
}
