package com.swyp.mangro.feature.owner.onboarding.screen.operating

sealed interface OwnerOperatingInfoEvent {
    data object NavigateBack : OwnerOperatingInfoEvent
    data object CompleteOnboarding : OwnerOperatingInfoEvent
}
