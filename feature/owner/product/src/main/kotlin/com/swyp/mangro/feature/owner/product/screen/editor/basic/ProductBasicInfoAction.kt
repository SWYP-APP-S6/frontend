package com.swyp.mangro.feature.owner.product.screen.editor.basic

sealed interface ProductBasicInfoAction {
    data class NameChanged(val name: String) : ProductBasicInfoAction
    data class PhotosSelected(val photos: List<String>) : ProductBasicInfoAction
    data class PhotoRemoveClicked(val photo: String) : ProductBasicInfoAction
    data object ErrorDismissed : ProductBasicInfoAction
    data object PhotoPermissionFailed : ProductBasicInfoAction
    data object NextClicked : ProductBasicInfoAction
    data object NavigationBackClicked : ProductBasicInfoAction
    data object DiscardDismissed : ProductBasicInfoAction
    data object DiscardConfirmClicked : ProductBasicInfoAction
}
