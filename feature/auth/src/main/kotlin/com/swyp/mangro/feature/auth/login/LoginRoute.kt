package com.swyp.mangro.feature.auth.login

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.feature.auth.R

@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateToTerms: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isLoading) { }

    LaunchedEffect(viewModel, context) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginUiEvent.ShowAuthFailure -> {
                    val message = when (event.reason) {
                        AuthFailure.NETWORK -> R.string.auth_network_failed
                        AuthFailure.STORAGE -> R.string.auth_storage_failed
                        AuthFailure.INVALID_RESPONSE -> R.string.auth_response_failed
                        else -> R.string.kakao_login_failed
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                LoginUiEvent.LaunchKakaoLogin -> viewModel.launchKakaoLogin(context)

                is LoginUiEvent.ShowKakaoLoginResult -> {
                    val message = when (event.result) {
                        KakaoLoginResult.Success -> R.string.kakao_login_success
                        KakaoLoginResult.Failed -> R.string.kakao_login_failed
                        KakaoLoginResult.Cancelled -> R.string.kakao_login_cancelled
                        KakaoLoginResult.NotConfigured -> R.string.kakao_login_unconfigured
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                LoginUiEvent.NavigateToHome -> navigateToHome()

                LoginUiEvent.NavigateToPrivacyPolicy -> navigateToPrivacyPolicy()

                LoginUiEvent.NavigateToTerms -> navigateToTerms()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
