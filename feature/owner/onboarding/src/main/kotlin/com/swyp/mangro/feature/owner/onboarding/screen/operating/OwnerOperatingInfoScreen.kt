package com.swyp.mangro.feature.owner.onboarding.screen.operating

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroTextField
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.onboarding.R
import com.swyp.mangro.feature.owner.onboarding.components.OwnerOnboardingField
import com.swyp.mangro.feature.owner.onboarding.components.OwnerOnboardingScaffold
import kotlin.collections.orEmpty
import kotlinx.serialization.Serializable

@Serializable
data object OwnerOperatingInfoDestination

@Composable
internal fun OwnerOperatingInfoRoute(
    onComplete: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: OwnerOperatingInfoViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { viewModel.handleAction(OwnerOperatingInfoAction.NavigationBackClicked) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerOperatingInfoEvent.NavigateBack -> navigateBack()
                OwnerOperatingInfoEvent.CompleteOnboarding -> onComplete()
            }
        }
    }

    OwnerOperatingInfoScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun OwnerOperatingInfoScreen(
    uiState: OwnerOperatingInfoState,
    onAction: (OwnerOperatingInfoAction) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    OwnerOnboardingScaffold(
        buttonLabel = stringResource(R.string.submit),
        enabled = uiState.isSubmitEnabled,
        onContinue = {
            focusManager.clearFocus()
            onAction(OwnerOperatingInfoAction.SubmitClicked)
        },
        onBack = { onAction(OwnerOperatingInfoAction.NavigationBackClicked) },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.operating_intro),
                style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp),
            )
        }

        OwnerOnboardingField(label = stringResource(R.string.phone)) {
            MangroTextField(
                value = uiState.phoneNumber,
                onValueChange = { onAction(OwnerOperatingInfoAction.PhoneNumberChanged(it)) },
                placeholder = stringResource(R.string.phone_placeholder),
                modifier = Modifier.testTag("phone"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
            )

            if (uiState.phoneNumber.isNotEmpty() && !uiState.registration.isPhoneValid) {
                Text(
                    stringResource(R.string.phone_error),
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.caption.captionS,
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_onboarding_warning),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified,
                )

                Text(
                    text = stringResource(R.string.phone_public),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.primaryNormal,
                )
            }
        }

        OwnerOnboardingField(label = stringResource(R.string.hours)) {
            val hours = (0..23).map { it * 60 }
            val closingHours = uiState.openingMinutes?.let { start -> hours.filter { it >= start } }.orEmpty()
            val fontScale = LocalDensity.current.fontScale

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 300.dp * fontScale) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.opening),
                            style = MangroTheme.typography.caption.captionS,
                        )
                        TimeField(
                            minutes = uiState.openingMinutes,
                            placeholder = stringResource(R.string.opening),
                            modifier = Modifier.fillMaxWidth(),
                            options = hours,
                            onSelect = { onAction(OwnerOperatingInfoAction.OpeningTimeSelected(it)) },
                        )

                        Text(
                            stringResource(R.string.closing),
                            style = MangroTheme.typography.caption.captionS,
                        )
                        TimeField(
                            minutes = uiState.closingMinutes,
                            placeholder = stringResource(R.string.closing),
                            modifier = Modifier.fillMaxWidth(),
                            options = closingHours,
                            onSelect = { onAction(OwnerOperatingInfoAction.ClosingTimeSelected(it)) },
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TimeField(
                            minutes = uiState.openingMinutes,
                            placeholder = stringResource(R.string.opening),
                            modifier = Modifier.weight(1f),
                            options = hours,
                            onSelect = { onAction(OwnerOperatingInfoAction.OpeningTimeSelected(it)) },
                        )
                        Text(
                            text = "~",
                            style = MangroTheme.typography.heading.headingM,
                            color = MangroTheme.colors.textSubtitle,
                        )
                        TimeField(
                            minutes = uiState.closingMinutes,
                            placeholder = stringResource(R.string.closing),
                            modifier = Modifier.weight(1f),
                            options = closingHours,
                            onSelect = { onAction(OwnerOperatingInfoAction.ClosingTimeSelected(it)) },
                        )
                    }
                }
            }
            if (uiState.openingMinutes != null && uiState.closingMinutes != null && !uiState.registration.isTimeValid) {
                Text(
                    text = stringResource(R.string.hours_error),
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.caption.captionS,
                )
            }
        }

        OwnerOnboardingField(label = stringResource(R.string.days)) {
            val labels = stringResource(R.string.day_labels).split(",")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                labels.forEachIndexed { day, label ->
                    val selected = day in uiState.businessDays

                    val selectedContainerColor by animateColorAsState(
                        if (selected) MangroTheme.colors.primaryNormal else MangroTheme.colors.surfaceDisabled,
                    )
                    val selectedContentColor by animateColorAsState(
                        if (selected) MangroTheme.colors.textOnBrandWhite else MangroTheme.colors.textCanceled,
                    )

                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
                            .clip(CircleShape)
                            .background(selectedContainerColor)
                            .clickable(onClick = { onAction(OwnerOperatingInfoAction.BusinessDayClicked(day)) })
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MangroTheme.typography.body.bodyM,
                            color = selectedContentColor,
                        )
                    }
                }
            }
        }
    }

    MangroDialogContainer(
        show = uiState.isLoading,
        onDismissRequest = { /* Do Nothing */ },
        dialogProperties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Text(
                text = stringResource(R.string.submitting),
                style = MangroTheme.typography.title.titleL,
            )
        },
        content = {
            CircularProgressIndicator(color = MangroTheme.colors.primaryNormal)
        },
        actions = { /* Do Nothing */ },
    )

    MangroDialogContainer(
        show = (uiState.dialog == OwnerOperatingInfoDialog.Error),
        onDismissRequest = { onAction(OwnerOperatingInfoAction.DialogDismissed) },
        title = {
            Text(
                text = stringResource(R.string.submit_error_title),
                style = MangroTheme.typography.title.titleL,
            )
        },
        content = {
            Text(
                text = stringResource(R.string.submit_error_body),
                textAlign = TextAlign.Center,
                style = MangroTheme.typography.body.bodyM.copy(lineBreak = LineBreak.Paragraph),
            )
        },
        actions = {
            MangroButton(
                onClick = { onAction(OwnerOperatingInfoAction.DialogDismissed) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MangroTheme.typography.title.titleL,
                    )
                },
            )
        },
    )

    MangroDialogContainer(
        show = (uiState.dialog == OwnerOperatingInfoDialog.Submitted),
        onDismissRequest = { /* Do Nothing */ },
        dialogProperties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Text(
                text = stringResource(R.string.submitted),
                style = MangroTheme.typography.title.titleL,
            )
        },
        content = {
            Text(
                text = stringResource(R.string.submitted_body),
                textAlign = TextAlign.Center,
                style = MangroTheme.typography.body.bodyM.copy(lineBreak = LineBreak.Paragraph),
            )
        },
        actions = {
            MangroButton(
                onClick = { onAction(OwnerOperatingInfoAction.DialogDismissed) },
                style = MangroButtonStyle.ACTIVE,
                enabled = uiState.isCompletionHandled,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MangroTheme.typography.title.titleL,
                    )
                },
            )
        },
    )
}

@Composable
private fun TimeField(minutes: Int?, placeholder: String, modifier: Modifier, options: List<Int>, onSelect: (Int) -> Unit) {
    val labels = options.map { timeLabel(it) }
    MangroDropdownField(
        options = labels,
        selectedOption = minutes?.let { timeLabel(it) },
        onOptionSelected = { onSelect(options[labels.indexOf(it)]) },
        placeholder = placeholder,
        modifier = modifier,
    )
}

@Composable
private fun timeLabel(minutes: Int): String = stringResource(R.string.time_format, minutes / 60, minutes % 60)
