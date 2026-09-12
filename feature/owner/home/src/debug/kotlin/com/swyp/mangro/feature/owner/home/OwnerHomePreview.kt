package com.swyp.mangro.feature.owner.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreen
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeUiState

private class OwnerHomePreviewProvider : PreviewParameterProvider<OwnerHomeUiState> {
    override val values = sequenceOf(OwnerHomeSamples.welcome, OwnerHomeSamples.empty, OwnerHomeSamples.operating())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Small screen, large font", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun OwnerHomePreview(@PreviewParameter(OwnerHomePreviewProvider::class) state: OwnerHomeUiState) {
    MangroTheme(typography = OwnerMangroTypography) {
        OwnerHomeScreen(state, onAction = {})
    }
}
