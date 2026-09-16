package com.swyp.mangro.feature.consumer.hold.history

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

sealed interface HoldHistoryUiAction {
    data class OnItemClick(val id: String) : HoldHistoryUiAction
    data class OnMenuClick(val menu: ConsumerMenu) : HoldHistoryUiAction
}
