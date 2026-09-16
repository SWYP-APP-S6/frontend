package com.swyp.mangro.feature.consumer.hold.hold

import com.swyp.mangro.core.designsystem.component.card.timer.TimerCardPhase

sealed interface HoldUiAction {
    data class OnTimerPhaseChange(val phase: TimerCardPhase) : HoldUiAction
    data object OnCancelClick : HoldUiAction
    data object OnRetryClick : HoldUiAction
    data object OnDirectionsClick : HoldUiAction
    data object OnCopyAddressClick : HoldUiAction
    data object OnCallClick : HoldUiAction
    data object OnViewOtherProductsClick : HoldUiAction
}
