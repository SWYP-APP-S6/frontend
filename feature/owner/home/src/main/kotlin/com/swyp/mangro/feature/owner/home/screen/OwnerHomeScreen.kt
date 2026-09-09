package com.swyp.mangro.feature.owner.home.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.banner.ActionBanner
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Red100
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeVisitor
import com.swyp.mangro.feature.owner.home.screen.model.remainingPickupMinutes
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun OwnerHomeScreen(
    state: OwnerHomeUiState,
    onAction: (OwnerHomeAction) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val nowMillis by produceState(System.currentTimeMillis(), lifecycleOwner, state.visitors.isNotEmpty()) {
        if (state.visitors.isNotEmpty()) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    value = System.currentTimeMillis()
                    delay(1_000.milliseconds)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                title = {
                    Text(
                        text = stringResource(R.string.owner_home_title),
                        style = MangroTheme.typography.label.labelL,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                actions = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_owner_notification),
                        contentDescription = stringResource(R.string.owner_home_notifications),
                        tint = MangroTheme.colors.textTitle,
                        modifier = Modifier.size(24.dp).clickable(role = Role.Button) {
                            onAction(OwnerHomeAction.ViewNotifications)
                        },
                    )
                },
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = MangroTheme.colors.surfaceAlter,
        floatingActionButton = {
            if (state.hasRegisteredProduct) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                    shape = CircleShape,
                    containerColor = MangroTheme.colors.primaryNormal,
                    contentColor = MangroTheme.colors.textOnBrandWhite,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_plus_24px),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MangroTheme.colors.textOnBrandWhite,
                    )
                    Text(
                        text = stringResource(R.string.owner_home_register),
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("owner-home-list"),
            contentPadding = PaddingValues(bottom = if (state.hasRegisteredProduct) 88.dp else 44.dp),
        ) {
            if (!state.hasRegisteredProduct) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.owner_home_welcome),
                            modifier = Modifier.padding(top = 48.dp, bottom = 12.dp),
                            style = MangroTheme.typography.heading.headingL,
                            color = MangroTheme.colors.primaryNormal,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.owner_home_welcome_description),
                            style = MangroTheme.typography.body.bodyM,
                            color = MangroTheme.colors.textTitle,
                            textAlign = TextAlign.Center,
                        )
                        Image(
                            painter = painterResource(R.drawable.welcome_store),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 48.dp),
                        )
                        Text(
                            text = stringResource(R.string.owner_home_welcome_hint),
                            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                            color = MangroTheme.colors.textBody,
                            textAlign = TextAlign.Center,
                        )
                        MangroButton(
                            text = stringResource(R.string.owner_home_first_product),
                            onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                            style = MangroButtonStyle.ACTIVE,
                            modifier = Modifier
                                .padding(top = 40.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            } else {
                if (state.hasNewPickup) {
                    item {
                        ActionBanner(
                            iconRes = DesignR.drawable.ic_alert,
                            stringRes = DesignR.string.banner_new_like,
                            iconSize = 24.dp,
                            modifier = Modifier.heightIn(min = 56.dp),
                        ) {
                            Text(
                                text = stringResource(DesignR.string.banner_action_confirm),
                                style = MangroTheme.typography.caption.captionS,
                                color = MangroTheme.colors.primaryNormal,
                                modifier = Modifier.clickable(role = Role.Button) { onAction(OwnerHomeAction.ViewNewPickups) }.padding(8.dp),
                            )
                        }
                    }
                }
                item(key = "attention") {
                    if (state.showAttention) {
                        AttentionCard(state, onAction, Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
                    } else {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 40.dp)) {
                            Text(stringResource(R.string.owner_home_greeting), style = MangroTheme.typography.heading.headingXXS, color = MangroTheme.colors.textTitle)
                            if (!state.hasAttention) {
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.owner_home_no_issues), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                            }
                        }
                    }
                }
                item(key = "dashboard") { Dashboard(state, onAction) }
                item(key = "visitors-heading") {
                    SectionHeading(
                        title = stringResource(if (state.visitors.isEmpty()) R.string.owner_home_no_visitors_title else R.string.owner_home_visitors),
                        onViewAll = if (state.visitors.isEmpty()) {
                            null
                        } else {
                            { onAction(OwnerHomeAction.ViewPickups) }
                        },
                        modifier = Modifier.padding(top = 32.dp),
                    )
                }
                if (state.visitors.isEmpty()) {
                    item(key = "empty-visitors") {
                        EmptyContent(R.drawable.empty_visitors, stringResource(R.string.owner_home_no_visitors), Modifier.heightIn(min = 176.dp))
                    }
                } else {
                    item(key = "visitors") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            items(state.visitors, key = { it.id }) { visitor ->
                                VisitorCard(visitor, nowMillis, onAction)
                            }
                        }
                    }
                }
                item(key = "products-heading") {
                    SectionHeading(
                        title = stringResource(R.string.owner_home_products),
                        onViewAll = if (state.products.isEmpty()) {
                            null
                        } else {
                            { onAction(OwnerHomeAction.ViewProducts) }
                        },
                        modifier = Modifier.padding(top = 48.dp),
                    )
                }
                if (state.products.isEmpty()) {
                    item(key = "empty-products") {
                        EmptyContent(R.drawable.empty_products, stringResource(R.string.owner_home_no_products), Modifier.padding(top = 32.dp, bottom = 24.dp))
                        MangroButton(
                            text = stringResource(R.string.owner_home_register_button),
                            onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                            style = MangroButtonStyle.OUTLINED,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            textStyle = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                        )
                    }
                } else {
                    items(state.products, key = { "product-${it.id}" }) { product ->
                        OwnerProductCard(
                            product = product,
                            modifier = Modifier.clickable(role = Role.Button) { onAction(OwnerHomeAction.ViewProduct(product.id)) }
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                        )
                        HorizontalDivider(color = MangroTheme.colors.borderDefault)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionCard(state: OwnerHomeUiState, onAction: (OwnerHomeAction) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .border(1.dp, Red100, RoundedCornerShape(16.dp))
            .background(MangroTheme.colors.dangerBg).padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_error),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MangroTheme.colors.dangerNormal,
            )
            IconButton(onClick = { onAction(OwnerHomeAction.DismissAttention) }) {
                Icon(painterResource(DesignR.drawable.ic_x_20px), stringResource(R.string.owner_home_dismiss_attention), tint = MangroTheme.colors.dangerNormal, modifier = Modifier.size(20.dp))
            }
        }
        Text(stringResource(R.string.owner_home_attention), style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM, color = MangroTheme.colors.dangerNormal)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.clip(RoundedCornerShape(8.dp)).background(MangroTheme.colors.surfaceNormal.copy(alpha = 0.5f))) {
            if (state.cancellationRequiredCount > 0) {
                AttentionLink(stringResource(R.string.owner_home_cancellation, state.cancellationRequiredCount)) { onAction(OwnerHomeAction.ViewCancellations) }
            }
            if (state.needsPickupConfirmation) {
                AttentionLink(stringResource(R.string.owner_home_confirmation)) { onAction(OwnerHomeAction.ConfirmPickups) }
            }
        }
    }
}

@Composable
private fun AttentionLink(text: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, Modifier.weight(1f), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textBody)
        Icon(
            painter = painterResource(DesignR.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MangroTheme.colors.dangerNormal,
        )
    }
}

@Composable
private fun Dashboard(state: OwnerHomeUiState, onAction: (OwnerHomeAction) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_store_front),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MangroTheme.colors.textTitle,
            )
            Text(state.storeName, Modifier.weight(1f, fill = false), style = MangroTheme.typography.body.bodyL, color = MangroTheme.colors.textTitle)
            MangroLabel(state.storeCategory, MangroTheme.colors.vegetablesNormal, MangroTheme.colors.vegetablesBg)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardCard(stringResource(R.string.owner_home_expected), stringResource(R.string.owner_home_cases, state.expectedVisitCount), DesignR.drawable.ic_owner_heart, Color(0xFFFFB480), MangroTheme.colors.primaryLight, Modifier.weight(1f)) { onAction(OwnerHomeAction.ViewPickups) }
            DashboardCard(stringResource(R.string.owner_home_completed), stringResource(R.string.owner_home_cases, state.completedPickupCount), DesignR.drawable.ic_pickup, Color(0xFFFFDD7D), MangroTheme.colors.primaryLight, Modifier.weight(1f)) { onAction(OwnerHomeAction.ViewCompletedPickups) }
            DashboardCard(stringResource(R.string.owner_home_selling), stringResource(R.string.owner_home_units, state.sellingCount), DesignR.drawable.ic_register, MangroTheme.colors.vegetablesNormal, MangroTheme.colors.vegetablesBg, Modifier.weight(1f)) { onAction(OwnerHomeAction.ViewProducts) }
        }
    }
}

@Composable
private fun DashboardCard(title: String, value: String, @DrawableRes icon: Int, start: Color, end: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.shadow(3.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(start, end))).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(36.dp).background(MangroTheme.colors.surfaceNormal, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(icon), null, Modifier.size(24.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textBody, textAlign = TextAlign.Center)
            Text(value, style = MangroTheme.typography.number.numberL, color = MangroTheme.colors.textTitle, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SectionHeading(title: String, onViewAll: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MangroTheme.typography.heading.headingXXS, color = MangroTheme.colors.textTitle)
        if (onViewAll != null) {
            Row(Modifier.clickable(role = Role.Button, onClick = onViewAll).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.owner_home_view_all), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                Icon(
                    painter = painterResource(DesignR.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(@DrawableRes image: Int, text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painterResource(image), null, Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(text, Modifier.padding(horizontal = 20.dp), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle, textAlign = TextAlign.Center)
    }
}

@Composable
private fun VisitorCard(visitor: OwnerHomeVisitor, nowMillis: Long, onAction: (OwnerHomeAction) -> Unit) {
    val minutes = remainingPickupMinutes(visitor.pickupDeadlineMillis, nowMillis)
    Column(Modifier.width(150.dp).clip(RoundedCornerShape(16.dp)).background(MangroTheme.colors.surfaceNormal)) {
        Column(
            Modifier.fillMaxWidth().clickable(role = Role.Button) { onAction(OwnerHomeAction.ViewPickup(visitor.id)) }
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        ) {
            Text(stringResource(R.string.owner_home_customer, visitor.customerName), style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(visitor.productName, Modifier.weight(1f), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.owner_home_quantity, visitor.quantity), maxLines = 1, softWrap = false, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(if (minutes > 0) R.string.owner_home_remaining else R.string.owner_home_expired, minutes),
                Modifier.align(Alignment.End),
                style = MangroTheme.typography.heading.headingXXS,
                color = if (minutes > 0) MangroTheme.colors.primaryNormal else MangroTheme.colors.textCanceled,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (minutes > 0) Color(0xFFFFD2B2) else MangroTheme.colors.borderDefault)
                .clickable(enabled = minutes > 0, role = Role.Button) {
                    onAction(OwnerHomeAction.CompletePickup(visitor.id))
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_owner_check),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MangroTheme.colors.textTitle,
            )
            Text(stringResource(R.string.owner_home_completed), style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle)
        }
    }
}
