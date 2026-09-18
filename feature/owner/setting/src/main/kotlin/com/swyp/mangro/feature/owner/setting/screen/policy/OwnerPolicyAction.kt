package com.swyp.mangro.feature.owner.setting.screen.policy

sealed interface OwnerPolicyAction {
    data object RetryClicked : OwnerPolicyAction
    data object NavigationBackClicked : OwnerPolicyAction
}
