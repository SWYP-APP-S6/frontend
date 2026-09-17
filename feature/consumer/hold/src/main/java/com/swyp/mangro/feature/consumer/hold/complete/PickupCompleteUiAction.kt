package com.swyp.mangro.feature.consumer.hold.complete

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

sealed interface PickupCompleteUiAction {
    data class OnRecipeClick(val recipeId: Long) : PickupCompleteUiAction
    data class OnMenuClick(val menu: ConsumerMenu) : PickupCompleteUiAction
}
