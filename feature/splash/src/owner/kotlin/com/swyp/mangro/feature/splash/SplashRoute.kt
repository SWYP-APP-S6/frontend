package com.swyp.mangro.feature.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SplashRoute(navigateToLogin: () -> Unit, navigateToHome: () -> Unit, viewModel: SplashViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect {
            when (it) {
                SplashUiEvent.NavigateToLogin -> navigateToLogin()
                SplashUiEvent.NavigateToHome -> navigateToHome()
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        SplashScreen()
        if (uiState.hasError) {
            TextButton(onClick = { viewModel.handleAction(SplashUiAction.RetryClicked) }, modifier = Modifier.align(Alignment.BottomCenter)) {
                Text(stringResource(R.string.owner_session_retry))
            }
        }
    }
}
