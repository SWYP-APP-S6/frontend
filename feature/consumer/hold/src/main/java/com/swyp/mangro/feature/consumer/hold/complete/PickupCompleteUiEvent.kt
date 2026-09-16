package com.swyp.mangro.feature.consumer.hold.complete

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

sealed interface PickupCompleteUiEvent {
    data class NavigateToRecipeDetail(val recipeId: Long) : PickupCompleteUiEvent
    data class NavigateToMenu(val menu: ConsumerMenu) : PickupCompleteUiEvent
}
