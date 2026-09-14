package com.swyp.mangro.feature.owner.setting.screen.policy

sealed interface OwnerPolicyEvent {
    data object NavigateBack : OwnerPolicyEvent
}
