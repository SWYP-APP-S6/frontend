package com.swyp.mangro.feature.owner.onboarding.screen.basic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroTextField
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.onboarding.R
import com.swyp.mangro.feature.owner.onboarding.components.OwnerOnboardingField
import com.swyp.mangro.feature.owner.onboarding.components.OwnerOnboardingScaffold
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import kotlinx.serialization.Serializable

@Serializable
data object OwnerBasicInfoDestination

@Composable
internal fun OwnerBasicInfoRoute(
    navigateNext: (StoreBasicInfoModel) -> Unit,
    navigateBack: () -> Unit,
    navigateToAddressSearch: () -> Unit,
    viewModel: OwnerBasicInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categories = remember {
        listOf("곡류", "과채류", "육류", "어류", "견과류", "기타").mapIndexed { index, label -> StoreCategoryModel("debug-$index", label) }
    }

    LaunchedEffect(viewModel, categories) {
        viewModel.handleAction(OwnerBasicInfoAction.CategoriesReceived(categories))
    }
    BackHandler { viewModel.handleAction(OwnerBasicInfoAction.NavigationBackClicked) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is OwnerBasicInfoEvent.NavigateToOperatingInfo -> navigateNext(event.basicInfo)
                OwnerBasicInfoEvent.NavigateToAddressSearch -> navigateToAddressSearch()
                OwnerBasicInfoEvent.NavigateBack -> navigateBack()
            }
        }
    }

    OwnerBasicInfoScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun OwnerBasicInfoScreen(
    uiState: OwnerBasicInfoState,
    onAction: (OwnerBasicInfoAction) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    OwnerOnboardingScaffold(
        buttonLabel = stringResource(R.string.next),
        enabled = uiState.isNextEnabled,
        onContinue = {
            focusManager.clearFocus()
            onAction(OwnerBasicInfoAction.NextClicked)
        },
        onBack = { onAction(OwnerBasicInfoAction.NavigationBackClicked) },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.intro_first),
                style = MangroTheme.typography.heading.headingM,
                color = MangroTheme.colors.textTitle,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.intro_second),
                    style = MangroTheme.typography.heading.headingM,
                    color = MangroTheme.colors.textTitle,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_onboarding_phone),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified,
                )
            }
        }

        OwnerOnboardingField(
            label = stringResource(R.string.name),
            hint = stringResource(R.string.name_hint),
        ) {
            MangroTextField(
                value = uiState.name,
                onValueChange = { onAction(OwnerBasicInfoAction.NameChanged(it)) },
                placeholder = stringResource(R.string.name_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
        }

        OwnerOnboardingField(
            label = stringResource(R.string.category),
            hint = stringResource(R.string.category_hint),
        ) {
            MangroDropdownField(
                options = uiState.categories.map { it.label },
                selectedOption = uiState.category?.label,
                onOptionSelected = { label ->
                    onAction(OwnerBasicInfoAction.CategorySelected(uiState.categories.first { it.label == label }))
                },
                placeholder = stringResource(R.string.category_placeholder),
                modifier = Modifier.fillMaxWidth(),
                placeholderColor = MangroTheme.colors.textCanceled,
            )
        }

        OwnerOnboardingField(
            label = stringResource(R.string.address),
            hint = stringResource(R.string.address_hint),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddressValue(
                    value = uiState.address?.postalCode,
                    placeholder = stringResource(R.string.postal_code),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        onAction(OwnerBasicInfoAction.AddressSearchClicked)
                    },
                )

                MangroButton(
                    onClick = {
                        focusManager.clearFocus()
                        onAction(OwnerBasicInfoAction.AddressSearchClicked)
                    },
                    style = MangroButtonStyle.ACTIVE,
                ) {
                    Text(
                        text = stringResource(R.string.search),
                        style = MangroTheme.typography.label.labelL,
                    )
                }
            }

            AddressValue(
                value = uiState.address?.address,
                placeholder = stringResource(R.string.address),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus()
                    onAction(OwnerBasicInfoAction.AddressSearchClicked)
                },
            )

            MangroTextField(
                value = uiState.detailedAddress,
                onValueChange = { onAction(OwnerBasicInfoAction.DetailedAddressChanged(it)) },
                placeholder = stringResource(R.string.detail_address),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
        }
    }
}

@Composable
private fun AddressValue(value: String?, placeholder: String, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text = value ?: placeholder,
        modifier = modifier
            .border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        style = MangroTheme.typography.body.bodyM,
        color = if (value == null) {
            MangroTheme.colors.textCanceled
        } else {
            MangroTheme.colors.textTitle
        },
    )
}
