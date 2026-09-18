package com.swyp.mangro.feature.consumer.myinfo

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.data.auth.model.TermsKind

sealed interface MyInfoUiEvent {
    data object NavigateToLogin : MyInfoUiEvent
    data class NavigateToMenu(val menu: ConsumerMenu) : MyInfoUiEvent
    data class NavigateToPolicy(val kind: TermsKind) : MyInfoUiEvent
}
