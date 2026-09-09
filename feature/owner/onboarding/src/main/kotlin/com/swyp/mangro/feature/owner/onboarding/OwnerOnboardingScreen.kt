package com.swyp.mangro.feature.owner.onboarding

import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroTextField
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Search and submission are supplied by the host. Neither is simulated in this screen. */
@Composable
fun OwnerOnboardingScreen(
    categories: List<StoreCategory>,
    onSearchAddress: suspend (String) -> List<StoreAddress>,
    onSubmit: suspend (StoreRegistration) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    state: OwnerOnboardingState = rememberOwnerOnboardingState(),
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var searching by rememberSaveable { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var completionDelivered by rememberSaveable { mutableStateOf(false) }
    var submissionError by rememberSaveable { mutableStateOf(false) }
    val back = {
        if (!submitting) {
            if (state.step == 2) state.step = 1 else onBack()
        }
    }
    BackHandler(onBack = back)
    MangroTheme(typography = OwnerMangroTypography) {
        Scaffold(
            modifier = modifier.imePadding(),
            containerColor = MangroTheme.colors.surfaceNormal,
            topBar = {
                MangroDefaultStartAlignedTopAppBar(
                    title = { Text(stringResource(R.string.store_info), style = MangroTheme.typography.heading.headingS.copy(fontSize = 18.sp)) },
                    navigationIcon = {
                        Icon(
                            painterResource(DesignR.drawable.ic_arrow_left),
                            stringResource(R.string.back),
                            Modifier.size(24.dp).clickable(role = Role.Button, onClick = back),
                            tint = MangroTheme.colors.textTitle,
                        )
                    },
                )
            },
            bottomBar = {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp)) {
                    val valid = state.registration().let { if (state.step == 1) it.isBasicInfoValid else it.isValid }
                    MangroButton(
                        onClick = {
                            focusManager.clearFocus()
                            if (state.step == 1) {
                                state.step = 2
                            } else if (!submitting && !submitted) {
                                val request = state.registration()
                                if (request.isValid) {
                                    submitting = true
                                    submissionError = false
                                    scope.launch {
                                        try {
                                            onSubmit(request)
                                            submitted = true
                                        } catch (cancelled: CancellationException) {
                                            throw cancelled
                                        } catch (_: Exception) {
                                            submissionError = true
                                        } finally {
                                            submitting = false
                                        }
                                    }
                                }
                            }
                        },
                        style = MangroButtonStyle.ACTIVE,
                        enabled = valid && !submitting && !submitted,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("next"),
                    ) {
                        Text(stringResource(if (state.step == 1) R.string.next else R.string.submit), style = MangroTheme.typography.title.titleL)
                    }
                }
            },
        ) { padding ->
            key(state.step) {
                Column(
                    Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 20.dp).testTag("form"),
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                ) {
                    Column(Modifier.padding(top = 32.dp, bottom = 8.dp)) {
                        if (state.step == 1) {
                            Text(stringResource(R.string.intro_first), style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.intro_second), style = MangroTheme.typography.heading.headingM, modifier = Modifier.weight(1f, fill = false))
                                Icon(painterResource(R.drawable.ic_onboarding_phone), null, Modifier.size(32.dp).padding(3.dp), tint = Color.Unspecified)
                            }
                        } else {
                            Text(stringResource(R.string.operating_intro), style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp))
                        }
                    }
                    if (state.step == 1) {
                        BasicInfo(state, categories, onSearch = {
                            focusManager.clearFocus()
                            searching = true
                        })
                    } else {
                        OperatingInfo(state)
                    }
                }
            }
        }
        if (searching) {
            AddressSearchDialog(
                onSearch = onSearchAddress,
                onDismiss = { searching = false },
                onSelect = {
                    state.address = it
                    searching = false
                },
            )
        }
        if (submitting) {
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                title = { Text(stringResource(R.string.submitting)) },
                text = { LinearProgressIndicator(Modifier.fillMaxWidth()) },
                confirmButton = {},
            )
        }
        if (submissionError) {
            AlertDialog(
                onDismissRequest = { submissionError = false },
                title = { Text(stringResource(R.string.submit_error_title)) },
                text = { Text(stringResource(R.string.submit_error_body)) },
                confirmButton = { TextButton(onClick = { submissionError = false }) { Text(stringResource(R.string.confirm)) } },
            )
        }
        if (submitted) {
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                title = { Text(stringResource(R.string.submitted)) },
                text = { Text(stringResource(R.string.submitted_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        if (!completionDelivered) {
                            completionDelivered = true
                            onComplete()
                        }
                    }, enabled = !completionDelivered) { Text(stringResource(R.string.confirm)) }
                },
            )
        }
    }
}

@Composable
private fun BasicInfo(state: OwnerOnboardingState, categories: List<StoreCategory>, onSearch: () -> Unit) {
    Field(stringResource(R.string.name), stringResource(R.string.name_hint)) {
        MangroTextField(state.name, stringResource(R.string.name_placeholder), Modifier.testTag("name"), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    Field(stringResource(R.string.category), stringResource(R.string.category_hint)) {
        MangroDropdownField(
            options = categories.map { it.label },
            selectedOption = state.category?.label,
            onOptionSelected = { label -> state.category = categories.first { it.label == label } },
            placeholder = stringResource(R.string.category_placeholder),
            modifier = Modifier.fillMaxWidth().testTag("category"),
            placeholderColor = MangroTheme.colors.textCanceled,
        )
    }
    Field(stringResource(R.string.address), stringResource(R.string.address_hint)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AddressValue(state.address?.postalCode, stringResource(R.string.postal_code), Modifier.weight(1f), onSearch)
            MangroButton(onClick = onSearch, style = MangroButtonStyle.ACTIVE) {
                Text(stringResource(R.string.search), style = MangroTheme.typography.label.labelL)
            }
        }
        AddressValue(state.address?.address, stringResource(R.string.address), Modifier.fillMaxWidth(), onSearch)
        MangroTextField(state.detailAddress, stringResource(R.string.detail_address), Modifier.testTag("detail"), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
    }
}

@Composable
private fun OperatingInfo(state: OwnerOnboardingState) {
    val registration = state.registration()
    Field(stringResource(R.string.phone)) {
        MangroTextField(
            state.phone,
            stringResource(R.string.phone_placeholder),
            Modifier.testTag("phone"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            inputTransformation = InputTransformation {
                if (length > 13 || asCharSequence().any { !it.isDigit() && it != '-' && it != ' ' }) revertAllChanges()
            },
        )
        if (state.phone.text.isNotEmpty() && !registration.isPhoneValid) {
            Text(stringResource(R.string.phone_error), color = MangroTheme.colors.primaryNormal, style = MangroTheme.typography.caption.captionS)
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(R.drawable.ic_onboarding_warning), null, Modifier.size(20.dp), tint = Color.Unspecified)
            Text(stringResource(R.string.phone_public), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.primaryNormal)
        }
    }
    Field(stringResource(R.string.hours)) {
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 300.dp * fontScale) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.opening), style = MangroTheme.typography.caption.captionS)
                    TimeField(state.openingMinutes, stringResource(R.string.opening), Modifier.fillMaxWidth()) { state.openingMinutes = it }
                    Text(stringResource(R.string.closing), style = MangroTheme.typography.caption.captionS)
                    TimeField(state.closingMinutes, stringResource(R.string.closing), Modifier.fillMaxWidth()) { state.closingMinutes = it }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField(state.openingMinutes, stringResource(R.string.opening), Modifier.weight(1f)) { state.openingMinutes = it }
                    Text("~", style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textSubtitle)
                    TimeField(state.closingMinutes, stringResource(R.string.closing), Modifier.weight(1f)) { state.closingMinutes = it }
                }
            }
        }
        if (state.openingMinutes != null && state.closingMinutes != null && !registration.isTimeValid) {
            Text(stringResource(R.string.hours_error), color = MangroTheme.colors.primaryNormal, style = MangroTheme.typography.caption.captionS)
        }
    }
    Field(stringResource(R.string.days)) {
        val labels = stringResource(R.string.day_labels).split(",")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { day, label ->
                val selected = day in state.businessDays
                Box(
                    Modifier.sizeIn(minWidth = 40.dp, minHeight = 40.dp).clip(CircleShape)
                        .background(if (selected) MangroTheme.colors.primaryNormal else MangroTheme.colors.surfaceDisabled)
                        .semantics { this.selected = selected }.testTag("day-$day")
                        .clickable(role = Role.Checkbox) {
                            state.businessDays = if (selected) state.businessDays - day else state.businessDays + day
                        }.padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MangroTheme.typography.body.bodyM, color = if (selected) MangroTheme.colors.textOnBrandWhite else MangroTheme.colors.textCanceled)
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, hint: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MangroTheme.typography.heading.headingXXS)
                Icon(painterResource(DesignR.drawable.ic_star), null, tint = Color.Unspecified)
            }
            if (hint != null) Text(hint, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        }
        content()
    }
}

@Composable
private fun AddressValue(value: String?, placeholder: String, modifier: Modifier, onClick: () -> Unit) {
    Text(
        value ?: placeholder,
        modifier.border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onClick).padding(12.dp),
        style = MangroTheme.typography.body.bodyM,
        color = if (value == null) MangroTheme.colors.textCanceled else MangroTheme.colors.textTitle,
    )
}

@Composable
private fun TimeField(minutes: Int?, placeholder: String, modifier: Modifier, onSelect: (Int) -> Unit) {
    val context = LocalContext.current
    var open by rememberSaveable { mutableStateOf(false) }
    val text = minutes?.let {
        stringResource(R.string.time_format, stringResource(if (it < 720) R.string.am else R.string.pm), (it / 60 % 12).let { hour -> if (hour == 0) 12 else hour }, it % 60)
    }
    MangroDropdownField(text = text ?: placeholder, onClick = { open = true }, modifier = modifier, isPlaceholder = minutes == null)
    if (open) {
        DisposableEffect(context) {
            val initial = minutes ?: 540
            val dialog = TimePickerDialog(context, { _, hour, minute ->
                onSelect(hour * 60 + minute)
                open = false
            }, initial / 60, initial % 60, false)
            dialog.setTitle(placeholder)
            dialog.setOnDismissListener { open = false }
            dialog.show()
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }
}
