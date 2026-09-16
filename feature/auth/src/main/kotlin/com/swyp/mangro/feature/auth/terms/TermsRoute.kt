package com.swyp.mangro.feature.auth.terms

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.feature.auth.R

@Composable
fun TermsRoute(
    onBackClick: () -> Unit,
    onSignupCompleted: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToTermsDetail: (TermsType) -> Unit,
    onOwnerOnboarding: ((SignupConsents) -> Unit)? = null,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isLoading) { }

    LaunchedEffect(viewModel, context) {
        viewModel.event.collect { event ->
            when (event) {
                is TermsUiEvent.ShowSignupFailure -> {
                    Toast.makeText(context, if (event.needsLogin) R.string.signup_login_required else R.string.signup_failed, Toast.LENGTH_SHORT).show()
                    if (event.needsLogin) navigateToLogin()
                }

                TermsUiEvent.TermsChanged -> Toast.makeText(context, R.string.terms_changed, Toast.LENGTH_SHORT).show()

                TermsUiEvent.LoadFailed -> Toast.makeText(context, R.string.terms_load_failed, Toast.LENGTH_SHORT).show()

                TermsUiEvent.SignupCompleted -> onSignupCompleted()

                is TermsUiEvent.OwnerOnboardingRequired -> checkNotNull(onOwnerOnboarding)(event.consents)

                is TermsUiEvent.NavigateToTermsDetail -> navigateToTermsDetail(event.type)
            }
        }
    }

    TermsScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = { if (!uiState.isLoading) onBackClick() },
    )
}
