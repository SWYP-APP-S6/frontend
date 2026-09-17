package com.swyp.mangro.feature.auth.login

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.feature.auth.R

@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateToTerms: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    var failureMessage by rememberSaveable { mutableStateOf<Int?>(null) }
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
                    failureMessage = message
                }
                LoginUiEvent.LaunchKakaoLogin -> viewModel.launchKakaoLogin(context)

                is LoginUiEvent.ShowKakaoLoginResult -> {
                    val message = when (event.result) {
                        KakaoLoginResult.Success -> R.string.kakao_login_success
                        KakaoLoginResult.Failed -> R.string.kakao_login_failed
                        KakaoLoginResult.Cancelled -> R.string.kakao_login_cancelled
                        KakaoLoginResult.NotConfigured -> R.string.kakao_login_unconfigured
                    }
                    if (event.result == KakaoLoginResult.Failed || event.result == KakaoLoginResult.NotConfigured) {
                        failureMessage = message
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                LoginUiEvent.NavigateToHome -> navigateToHome()

                LoginUiEvent.NavigateToPrivacyPolicy -> navigateToPrivacyPolicy()

                LoginUiEvent.NavigateToTerms -> navigateToTerms()
            }
        }
    }

    MangroDialogContainer(
        show = failureMessage != null,
        onDismissRequest = { failureMessage = null },
        title = { Text(stringResource(R.string.login_failure_title)) },
        content = { failureMessage?.let { Text(stringResource(it)) } },
        actions = {
            MangroButton(
                text = stringResource(R.string.login_failure_confirm),
                onClick = { failureMessage = null },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    LoginScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
