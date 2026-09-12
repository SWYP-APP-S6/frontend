package com.swyp.mangro.feature.owner.home.screen

sealed interface OwnerHomeEvent {
    data object NavigateToDetail : OwnerHomeEvent
}
