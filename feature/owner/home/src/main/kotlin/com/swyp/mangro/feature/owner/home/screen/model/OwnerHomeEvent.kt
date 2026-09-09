package com.swyp.mangro.feature.owner.home.screen.model

sealed interface OwnerHomeEvent {
    data object NavigateToDetail : OwnerHomeEvent
}
