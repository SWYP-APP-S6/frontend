package com.swyp.mangro.feature.owner.product.screen.editor.pickup

sealed interface ProductPickupInfoAction {
    data class PickupTimeChanged(val value: String?) : ProductPickupInfoAction
    data class TagChanged(val value: String) : ProductPickupInfoAction
    data class TagRemoveClicked(val value: String) : ProductPickupInfoAction
    data object TagSubmitted : ProductPickupInfoAction
    data object RegisterClicked : ProductPickupInfoAction
    data object PreviewDismissed : ProductPickupInfoAction
    data object EditBasicInfoClicked : ProductPickupInfoAction
    data object SaveClicked : ProductPickupInfoAction
    data object NavigationBackClicked : ProductPickupInfoAction
}
