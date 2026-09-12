package com.swyp.mangro.feature.owner.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickupCancellationScreen(
    shortages: List<PickupShortage>,
    storeName: String,
    storePhone: String,
    busy: Boolean,
    failed: Boolean,
    onCancel: (Set<String>, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var excludedIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    val allTargets = shortages.flatMap { it.targets }
    val selectedIds = allTargets.map { it.id }.filterNot { it in excludedIds }.toSet()
    val selectedGroups = shortages.map { it.productName to it.targets.count { pickup -> pickup.id in selectedIds } }.filter { it.second > 0 }
    val message = stringResource(R.string.pickup_cancel_message, storeName, storePhone)
    LaunchedEffect(selectedIds) {
        if (selectedIds.isEmpty()) confirmationVisible = false
    }
    Column(modifier) {
        if (shortages.isEmpty()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.pickup_cancel_pending, 0), style = MangroTheme.typography.heading.headingXXS)
                Text(stringResource(R.string.pickup_allocation_rule), color = MangroTheme.colors.textSubtitle, style = MangroTheme.typography.caption.captionS)
            }
            PickupEmpty(true, Modifier.weight(1f))
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(
                        Modifier.padding(20.dp).fillMaxWidth().background(MangroTheme.colors.dangerBg, RoundedCornerShape(16.dp)).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(painterResource(R.drawable.pickup_shortage), null, Modifier.size(48.dp))
                        Text(stringResource(R.string.pickup_shortage_summary, shortages.size, allTargets.size), color = MangroTheme.colors.dangerNormal, style = MangroTheme.typography.body.bodyM)
                    }
                    Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.pickup_cancel_pending, selectedIds.size), style = MangroTheme.typography.heading.headingXXS)
                        Text(stringResource(R.string.pickup_allocation_rule), color = MangroTheme.colors.textSubtitle, style = MangroTheme.typography.caption.captionS)
                    }
                }
                items(shortages, key = { it.productId }) { shortage ->
                    Column(Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceNormal).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Image(painterResource(R.drawable.pickup_product), null, Modifier.size(20.dp))
                            Text(stringResource(R.string.pickup_shortage_product, shortage.productName, shortage.missingQuantity, shortage.targets.size), style = MangroTheme.typography.label.labelM)
                        }
                        shortage.targets.forEach { pickup ->
                            val description = stringResource(R.string.pickup_selection, pickup.customerName)
                            Row(
                                Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceAlter, RoundedCornerShape(12.dp))
                                    .toggleable(
                                        value = pickup.id in selectedIds,
                                        enabled = !busy,
                                        role = Role.Checkbox,
                                        onValueChange = { checked -> excludedIds = if (checked) excludedIds - pickup.id else excludedIds + pickup.id },
                                    ).semantics { contentDescription = description }.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = pickup.id in selectedIds,
                                    onCheckedChange = null,
                                    enabled = !busy,
                                    colors = CheckboxDefaults.colors(checkedColor = MangroTheme.colors.primaryNormal),
                                )
                                Column(Modifier.weight(1f).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(stringResource(R.string.pickup_position, shortage.requestPositions.getValue(pickup.id), pickupDate(pickup.requestedAt)), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                                    Text(stringResource(R.string.pickup_quantity, stringResource(R.string.pickup_customer, pickup.customerName), pickup.quantity), style = MangroTheme.typography.label.labelM)
                                }
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.navigationBarsPadding().padding(20.dp)) {
            MangroButton(
                stringResource(R.string.pickup_cancel_count_action, selectedIds.size),
                { confirmationVisible = true },
                MangroButtonStyle.ACTIVE,
                Modifier.fillMaxWidth(),
                enabled = selectedIds.isNotEmpty() && !busy,
            )
            if (failed) Text(stringResource(R.string.pickup_error), color = MangroTheme.colors.dangerNormal)
        }
    }
    if (confirmationVisible && selectedIds.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { if (!busy) confirmationVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { !busy }),
            containerColor = MangroTheme.colors.surfaceNormal,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.pickup_cancel_confirm, selectedIds.size), style = MangroTheme.typography.heading.headingS)
                selectedGroups.forEach { (product, count) ->
                    Text(stringResource(R.string.pickup_cancel_product_count, product, count), style = MangroTheme.typography.body.bodyM, color = MangroTheme.colors.textBody)
                }
                Row(Modifier.fillMaxWidth().background(MangroTheme.colors.dangerBg, RoundedCornerShape(4.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(DesignR.drawable.ic_error), null, Modifier.size(20.dp), tint = MangroTheme.colors.dangerNormal)
                    Text(stringResource(R.string.pickup_cancel_warning), color = MangroTheme.colors.dangerNormal, style = MangroTheme.typography.caption.captionS)
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.pickup_message_preview), style = MangroTheme.typography.label.labelM)
                Text(message, Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceAlter, RoundedCornerShape(12.dp)).padding(16.dp), style = MangroTheme.typography.body.bodyM, color = MangroTheme.colors.textBody)
                if (failed) Text(stringResource(R.string.pickup_error), color = MangroTheme.colors.dangerNormal)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    MangroButton(stringResource(R.string.pickup_return), { confirmationVisible = false }, MangroButtonStyle.OUTLINED, enabled = !busy)
                    MangroButton(
                        stringResource(if (busy) R.string.pickup_processing else R.string.pickup_cancel_send),
                        { onCancel(selectedIds, message) },
                        MangroButtonStyle.ACTIVE,
                        Modifier.weight(1f),
                        enabled = !busy,
                    )
                }
            }
        }
    }
}
