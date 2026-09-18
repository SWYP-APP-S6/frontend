package com.swyp.mangro.feature.consumer.myinfo

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.data.auth.model.TermsKind

sealed interface MyInfoUiAction {
    data object RetryClicked : MyInfoUiAction
    data object LinkKakaoClicked : MyInfoUiAction
    data object LogoutClicked : MyInfoUiAction
    data object LogoutConfirmed : MyInfoUiAction
    data object LogoutDismissed : MyInfoUiAction
    data object LogoutErrorDismissed : MyInfoUiAction
    data class MenuClicked(val menu: ConsumerMenu) : MyInfoUiAction
    data class PolicyClicked(val kind: TermsKind) : MyInfoUiAction
}
