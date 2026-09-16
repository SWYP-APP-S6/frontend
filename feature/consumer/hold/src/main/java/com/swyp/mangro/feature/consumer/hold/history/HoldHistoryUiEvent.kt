package com.swyp.mangro.feature.consumer.hold.history

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

sealed interface HoldHistoryUiEvent {
    data class NavigateToHoldDetail(val id: String) : HoldHistoryUiEvent
    data class NavigateToMenu(val menu: ConsumerMenu) : HoldHistoryUiEvent
}
