package com.swyp.mangro.feature.owner.pickup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 목록/재고는 호출자가 소유한다. 콜백은 서버의 원자적 검증/처리 후 최신 목록을 반영해야 한다.
 * 취소 콜백의 정상 반환은 취소와 안내 발송 요청이 모두 접수되었음을 의미한다.
 * 실패 시 예외를 던지면 화면을 유지하며 재시도할 수 있다. UI 자체는 메시지를 발송하지 않는다.
 */
@Composable
fun OwnerPickupRoute(
    pickups: List<Pickup>,
    stock: Map<String, Int>,
    storeName: String,
    storePhone: String,
    onCompletePickup: suspend (String) -> Unit,
    onCancelPickups: suspend (Set<String>, String) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var cancellationVisible by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf(PickupFilter.ALL) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (isActive) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val selected = pickups.find { it.id == selectedId }
    val shortages = pickupShortages(pickups, stock, now)
    val cancelCount = shortages.sumOf { it.targets.size }
    fun back() {
        if (!busy) {
            selectedId = null
            cancellationVisible = false
            failed = false
        }
    }
    BackHandler(enabled = busy || selected != null || cancellationVisible) { back() }
    val complete: (Pickup) -> Unit = { pickup ->
        if (!busy && canCompletePickup(pickup, pickups, stock, System.currentTimeMillis())) {
            busy = true
            failed = false
            scope.launch {
                try {
                    onCompletePickup(pickup.id)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    failed = true
                } finally {
                    busy = false
                }
            }
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (selected != null) MangroTheme.colors.surfaceNormal else MangroTheme.colors.surfaceAlter,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                title = {
                    Text(
                        text = stringResource(
                            when {
                                cancellationVisible -> R.string.pickup_cancel_title
                                selected != null -> R.string.pickup_detail
                                else -> R.string.pickup_management
                            },
                        ),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (selected != null || cancellationVisible) back() else onHomeClick() }, modifier = Modifier.size(24.dp), enabled = !busy) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_left), stringResource(R.string.pickup_back), Modifier.size(24.dp))
                    }
                },
            )
        },
        bottomBar = {
            if (!cancellationVisible) {
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (selected != null) {
                        val status = selected.statusAt(now)
                        MangroButton(
                            text = if (busy) stringResource(R.string.pickup_processing) else pickupActionText(status),
                            onClick = { complete(selected) },
                            style = MangroButtonStyle.OUTLINED,
                            enabled = !busy && canCompletePickup(selected, pickups, stock, now),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        MangroButton(
                            stringResource(R.string.pickup_home),
                            {
                                back()
                                onHomeClick()
                            },
                            MangroButtonStyle.TEXT,
                            Modifier.fillMaxWidth(),
                            !busy,
                        )
                    } else if (cancelCount > 0) {
                        Row(
                            Modifier.fillMaxWidth().background(MangroTheme.colors.grayScale900, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Icon(painterResource(DesignR.drawable.ic_error), null, Modifier.size(20.dp), tint = MangroTheme.colors.primaryNormal)
                            Text(
                                stringResource(R.string.pickup_cancel_notice, cancelCount),
                                Modifier.weight(1f).padding(horizontal = 8.dp),
                                color = MangroTheme.colors.textOnBrandWhite,
                                style = MangroTheme.typography.caption.captionS,
                            )
                            MangroButton(stringResource(R.string.pickup_cancel_action), { cancellationVisible = true }, MangroButtonStyle.TEXT, enabled = !busy)
                        }
                    }
                    if (failed) Text(stringResource(R.string.pickup_error), color = MangroTheme.colors.dangerNormal)
                }
            }
        },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
        when {
            cancellationVisible -> PickupCancellationScreen(
                shortages = shortages,
                storeName = storeName,
                storePhone = storePhone,
                busy = busy,
                failed = failed,
                onCancel = { ids, message ->
                    val currentIds = pickupShortages(pickups, stock, System.currentTimeMillis()).flatMap { it.targets }.map { it.id }.toSet()
                    if (!busy && ids.isNotEmpty() && currentIds.containsAll(ids)) {
                        busy = true
                        failed = false
                        scope.launch {
                            try {
                                onCancelPickups(ids, message)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                failed = true
                            } finally {
                                busy = false
                            }
                        }
                    }
                },
                modifier = contentModifier,
            )
            selected != null -> PickupDetailScreen(selected, storeName, now, contentModifier)
            else -> PickupListScreen(
                pickups = filterPickups(pickups, filter, now),
                totalCount = pickups.size,
                filter = filter,
                now = now,
                busy = busy,
                canComplete = { canCompletePickup(it, pickups, stock, now) },
                onFilterClick = { filter = it },
                onDetailClick = { if (!busy) selectedId = it.id },
                onCompleteClick = complete,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
internal fun pickupActionText(status: PickupStatus): String = stringResource(
    when (status) {
        PickupStatus.WAITING -> R.string.pickup_complete_action
        PickupStatus.COMPLETED -> R.string.pickup_complete_done
        PickupStatus.UNAVAILABLE -> R.string.pickup_unavailable
        PickupStatus.CANCELED -> R.string.pickup_canceled
        PickupStatus.EXPIRED -> R.string.pickup_expired_done
    },
)
