package com.swyp.mangro.feature.owner.product.screen.editor.basic

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductConfirmationBottomSheet
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.rememberProductTextFieldState
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import kotlinx.serialization.Serializable

@Serializable
internal data object ProductBasicInfoDestination

@Composable
internal fun ProductBasicInfoRoute(
    onNext: (ProductDraftModel) -> Unit,
    onExit: () -> Unit,
    viewModel: ProductBasicInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.initialize()
    }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ProductBasicInfoEvent.Next -> onNext(event.draft)
                ProductBasicInfoEvent.Exit -> onExit()
            }
        }
    }

    if (!state.isLoading) {
        ProductBasicInfoScreen(
            uiState = state,
            onAction = viewModel::handleAction,
        )
    }
}

@Composable
internal fun ProductBasicInfoScreen(
    uiState: ProductBasicInfoState,
    onAction: (ProductBasicInfoAction) -> Unit,
) {
    val name = rememberProductTextFieldState(value = uiState.name) {
        onAction(ProductBasicInfoAction.NameChanged(it))
    }
    val photos = uiState.photos
    val onBack = { onAction(ProductBasicInfoAction.NavigationBackClicked) }

    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = OwnerProductLimits.PHOTO_COUNT,
        ),
    ) { uris ->
        val selected = uris.mapNotNull { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                uri.toString()
            } catch (_: SecurityException) {
                onAction(ProductBasicInfoAction.PhotoPermissionFailed)
                null
            }
        }
        onAction(ProductBasicInfoAction.PhotosSelected(selected))
    }

    OwnerProductScaffold(
        title = stringResource(R.string.owner_product_register_title),
        onBack = onBack,
        contentSpacing = 0.dp,
        bottomBarContent = {
            MangroButton(
                text = stringResource(R.string.owner_product_next),
                onClick = { onAction(ProductBasicInfoAction.NextClicked) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canContinue,
                textStyle = MangroTheme.typography.title.titleL.copy(fontFamily = PretendardFont.Bold),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            stringResource(R.string.owner_product_editor_name_heading).lines().forEach { line ->
                Text(
                    text = line,
                    style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp),
                    color = MangroTheme.colors.textTitle,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OwnerProductLabel(
                text = stringResource(R.string.owner_product_photos_label),
                isRequired = true,
                textStyle = MangroTheme.typography.heading.headingXXS,
                hint = stringResource(R.string.owner_product_photos_hint, OwnerProductLimits.PHOTO_COUNT),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Column(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MangroTheme.colors.grayScale50)
                                .border(
                                    width = 1.dp,
                                    color = MangroTheme.colors.borderDefault,
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .clickable(
                                    enabled = photos.size < OwnerProductLimits.PHOTO_COUNT,
                                    role = Role.Button,
                                    onClick = {
                                        picker.launch(
                                            input = PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    },
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_camera_add),
                                contentDescription = stringResource(R.string.owner_product_photo_add),
                                modifier = Modifier.size(24.dp),
                                tint = MangroTheme.colors.textSubtitle,
                            )
                            Text(
                                text = stringResource(
                                    R.string.owner_product_photo_count,
                                    photos.size,
                                    OwnerProductLimits.PHOTO_COUNT,
                                ),
                                style = MangroTheme.typography.caption.captionS,
                                color = MangroTheme.colors.textSubtitle,
                            )
                        }
                    }
                    items(
                        items = photos,
                        key = { it },
                    ) { photo ->
                        Box(modifier = Modifier.size(66.dp)) {
                            AsyncImage(
                                model = photo,
                                contentDescription = stringResource(R.string.owner_product_photo_selected),
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            IconButton(
                                onClick = { onAction(ProductBasicInfoAction.PhotoRemoveClicked(photo)) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp),
                            ) {
                                Icon(
                                    painter = painterResource(DesignR.drawable.ic_x_circle),
                                    contentDescription = stringResource(R.string.owner_product_photo_delete),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
                MangroButton(
                    text = stringResource(R.string.owner_product_photo_upload),
                    onClick = {
                        picker.launch(
                            input = PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    style = MangroButtonStyle.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = photos.size < OwnerProductLimits.PHOTO_COUNT,
                    textStyle = MangroTheme.typography.label.labelM,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }

        MangroInputBox(
            label = stringResource(R.string.owner_product_name_label),
            hint = stringResource(R.string.owner_product_name_hint),
            state = name,
            placeholder = stringResource(R.string.owner_product_name_placeholder),
            modifier = Modifier.padding(top = 48.dp, bottom = 24.dp),
        )
    }
    uiState.error?.let { error ->
        MangroDialogContainer(
            show = true,
            onDismissRequest = { onAction(ProductBasicInfoAction.ErrorDismissed) },
            title = {
                Text(
                    text = stringResource(
                        when (error) {
                            ProductBasicInfoError.PHOTO_PERMISSION -> R.string.owner_product_photo_error
                            ProductBasicInfoError.INVALID_NAME -> R.string.owner_product_name_error
                        },
                    ),
                )
            },
            actions = {
                MangroButton(
                    text = stringResource(R.string.owner_product_confirm),
                    onClick = { onAction(ProductBasicInfoAction.ErrorDismissed) },
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
    if (uiState.showDiscard) {
        OwnerProductConfirmationBottomSheet(
            title = stringResource(R.string.owner_product_discard_title),
            description = stringResource(R.string.owner_product_discard_description),
            onDismiss = { onAction(ProductBasicInfoAction.DiscardDismissed) },
            onConfirm = { onAction(ProductBasicInfoAction.DiscardConfirmClicked) },
        )
    }
}
