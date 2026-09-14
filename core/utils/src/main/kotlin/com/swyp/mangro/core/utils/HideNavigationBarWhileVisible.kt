package com.swyp.mangro.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 이 Composable이 컴포지션에 남아있는 동안 하단 네비게이션 바를 숨기고,
 * 사라지면 자동으로 복원합니다.
 */
@Composable
fun HideNavigationBarWhileVisible(hidden: Boolean = true) {
    val context = LocalContext.current

    DisposableEffect(context, hidden) {
        val activity = context.findActivity()
        if (hidden) activity?.hideNavigationBar() else activity?.showNavigationBar()
        onDispose { activity?.showNavigationBar() }
    }
}

private fun Activity.hideNavigationBar() {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.navigationBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

private fun Activity.showNavigationBar() {
    WindowCompat.getInsetsController(window, window.decorView)
        .show(WindowInsetsCompat.Type.navigationBars())
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
