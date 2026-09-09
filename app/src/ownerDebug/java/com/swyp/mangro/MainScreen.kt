package com.swyp.mangro

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.swyp.mangro.feature.owner.onboarding.OwnerOnboardingScreen
import com.swyp.mangro.feature.owner.onboarding.StoreAddress
import com.swyp.mangro.feature.owner.onboarding.StoreCategory
import kotlinx.coroutines.delay

/** Debug fixtures only. No address is queried or registration transmitted to a real service. */
@Composable
internal fun MainScreen() {
    val activity = LocalActivity.current
    var showNotice by rememberSaveable { mutableStateOf(true) }
    val categories = remember {
        listOf("곡류", "과채류", "육류", "어류", "견과류", "기타").mapIndexed { index, label -> StoreCategory("debug-$index", label) }
    }
    OwnerOnboardingScreen(
        categories = categories,
        onSearchAddress = { query ->
            delay(300)
            listOf(StoreAddress("03965", "서울특별시 마포구 망원로 12"), StoreAddress("03965", "서울특별시 마포구 망원로 20"))
                .filter { it.address.contains(query) || it.postalCode.contains(query) }
        },
        onSubmit = { delay(500) },
        onComplete = { activity?.finish() },
        onBack = { activity?.finish() },
    )
    if (showNotice) {
        AlertDialog(
            onDismissRequest = { showNotice = false },
            title = { Text("점주 등록 UI 미리보기") },
            text = { Text("Debug 샘플입니다. 주소는 ‘망원’으로 검색할 수 있고 실제 등록 신청은 전송되지 않습니다.") },
            confirmButton = { TextButton(onClick = { showNotice = false }) { Text("확인") } },
        )
    }
}
