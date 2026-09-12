package com.swyp.mangro.feature.owner.home

import com.swyp.mangro.feature.owner.home.screen.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.utils.remainingPickupMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerHomeUiStateTest {
    @Test
    fun emptyInventoryDoesNotMeanFirstRegistration() {
        val state = OwnerHomeUiState(hasRegisteredProduct = true)
        assertTrue(state.hasRegisteredProduct)
        assertTrue(state.products.isEmpty())
    }

    @Test
    fun dismissedAttentionDoesNotClearUnderlyingProblems() {
        val state = OwnerHomeUiState(cancellationRequiredCount = 2, isAttentionDismissed = true)
        assertFalse(state.showAttention)
        assertTrue(state.hasAttention)
        assertTrue(state.copy(isAttentionDismissed = false).showAttention)
    }

    @Test
    fun pickupConfirmationAloneRequiresAttention() {
        assertTrue(OwnerHomeUiState(needsPickupConfirmation = true).showAttention)
        assertFalse(OwnerHomeUiState().showAttention)
    }

    @Test
    fun countdownRoundsUpWithoutShowingZeroBeforeExpiry() {
        assertEquals(8L, remainingPickupMinutes(480_000, 0))
        assertEquals(8L, remainingPickupMinutes(480_000, 1))
        assertEquals(1L, remainingPickupMinutes(480_000, 479_999))
        assertEquals(0L, remainingPickupMinutes(480_000, 480_000))
        assertEquals(0L, remainingPickupMinutes(480_000, 600_000))
    }
}
