package com.swyp.mangro.core.designsystem.component.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.collections.immutable.persistentListOf

// Figma 1151:12112의 원본 이미지. Debug Preview에만 포함됩니다.
@Preview(name = "Image viewer - Numbers", widthDp = 360, heightDp = 360)
@Composable
private fun ImageViewerNumbersPreview() {
    ImageViewerPreview(MangroImagePageController.Numbers)
}

@Preview(name = "Image viewer - Dots", widthDp = 360, heightDp = 360)
@Composable
private fun ImageViewerDotsPreview() {
    ImageViewerPreview(MangroImagePageController.Dots)
}

@Composable
private fun ImageViewerPreview(controller: MangroImagePageController) {
    MangroTheme {
        MangroImageViewer(
            images = persistentListOf(
                R.drawable.preview_slider_peach,
                R.drawable.preview_slider_detail,
                R.drawable.preview_slider_basket,
                R.drawable.preview_slider_fruit,
                R.drawable.preview_slider_box,
            ),
            controller = controller,
        )
    }
}
