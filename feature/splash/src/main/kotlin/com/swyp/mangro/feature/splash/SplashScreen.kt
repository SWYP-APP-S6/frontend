package com.swyp.mangro.feature.splash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun SplashRoute(
    modifier: Modifier = Modifier,
) {
    SplashIcon(
        modifier = modifier,
    )
}

@Composable
fun SplashIcon(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_app_icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(80.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_mangro_wordmark),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(
                width = 75.dp,
                height = 24.dp,
            ),
        )
    }
}
