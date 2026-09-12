package com.swyp.mangro.feature.owner.onboarding.screen.operating

sealed interface OwnerOperatingInfoAction {
    data class PhoneNumberChanged(val value: String) : OwnerOperatingInfoAction
    data class OpeningTimeSelected(val minutes: Int) : OwnerOperatingInfoAction
    data class ClosingTimeSelected(val minutes: Int) : OwnerOperatingInfoAction
    data class BusinessDayClicked(val day: Int) : OwnerOperatingInfoAction
    data object DialogDismissed : OwnerOperatingInfoAction
    data object SubmitClicked : OwnerOperatingInfoAction
    data object CompletionConfirmed : OwnerOperatingInfoAction
    data object NavigationBackClicked : OwnerOperatingInfoAction
}
