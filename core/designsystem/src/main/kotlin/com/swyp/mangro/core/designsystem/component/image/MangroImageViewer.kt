package com.swyp.mangro.core.designsystem.component.image

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive

enum class MangroImagePageController { Dots, Numbers }

/**
 * Coil 모델(URL, Uri, drawable ID 등)을 가로로 순환하는 이미지 뷰어입니다.
 * 기본 비율은 1:1이며 [modifier]로 높이 또는 비율을 지정할 수 있습니다.
 * 이미지 목록이 바뀌면 첫 장부터 시작하고, 0~1장은 자동 전환과 컨트롤러를 사용하지 않습니다.
 * [autoScrollEnabled]는 화면 내 비노출 상태 등 호출부에서 자동 재생을 제어할 때 사용합니다.
 */
@Composable
fun MangroImageViewer(
    images: ImmutableList<Any>,
    modifier: Modifier = Modifier,
    controller: MangroImagePageController = MangroImagePageController.Numbers,
    autoScrollEnabled: Boolean = true,
    placeholder: Painter? = null,
    error: Painter? = placeholder,
) {
    Box(modifier.fillMaxWidth().aspectRatio(1f).background(MangroTheme.colors.surfaceAlter)) {
        if (images.isNotEmpty()) {
            key(images) {
                ImagePages(images, controller, autoScrollEnabled, placeholder, error)
            }
        }
    }
}

@Composable
private fun ImagePages(
    images: ImmutableList<Any>,
    controller: MangroImagePageController,
    autoScrollEnabled: Boolean,
    placeholder: Painter?,
    error: Painter?,
) {
    val count = images.size
    // 양 끝에 마지막/첫 이미지를 복제하고, 정착 후 같은 이미지의 실제 페이지로 이동합니다.
    val pager = rememberPagerState(initialPage = if (count > 1) 1 else 0) {
        if (count > 1) count + 2 else 1
    }
    val dragged by pager.interactionSource.collectIsDraggedAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(pager, dragged, autoScrollEnabled, lifecycle) {
        if (count <= 1 || dragged) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                snapshotFlow { pager.isScrollInProgress }.first { !it }
                when (pager.currentPage) {
                    0 -> pager.scrollToPage(count)
                    count + 1 -> pager.scrollToPage(1)
                }
                if (autoScrollEnabled) {
                    delay(3_000)
                    if (!pager.isScrollInProgress) pager.animateScrollToPage(pager.currentPage + 1)
                } else {
                    snapshotFlow { pager.isScrollInProgress }.first { it }
                }
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = count > 1,
        ) { page ->
            val index = if (count == 1) 0 else Math.floorMod(page - 1, count)
            AsyncImage(
                model = images[index],
                contentDescription = stringResource(R.string.image_page_description, index + 1, count),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = error,
            )
        }
        val currentPage = if (count == 1) 0 else Math.floorMod(pager.currentPage - 1, count)
        when (controller) {
            MangroImagePageController.Dots -> MangroImagePageDots(
                currentPage = currentPage,
                pageCount = count,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
            MangroImagePageController.Numbers -> MangroImagePageNumbers(
                currentPage = currentPage,
                pageCount = count,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}
