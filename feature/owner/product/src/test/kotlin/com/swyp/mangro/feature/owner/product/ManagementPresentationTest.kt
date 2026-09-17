package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.feature.owner.product.model.presentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagementPresentationTest {
    private val hold = ManagedHold(8, 1, 7, "복숭아", "손님", 2, 1000, 16000, HoldStatus.HOLDING)

    @Test fun serverClockControlsExpiryAndTimerUsesDeviceAdjustedEpoch() {
        val active = hold.presentation(now = 15000, offset = 5000, busy = false)
        assertEquals(11000L, active.request.endTimeMillis)
        assertTrue(active.canComplete)
        val expired = hold.presentation(now = 16000, offset = 5000, busy = false)
        assertEquals(OwnerPickupRequestStatus.EXPIRED, expired.request.status)
        assertFalse(expired.canComplete)
    }

    @Test fun inFlightRequestsAndCompletedStatusPreventPickupCompletion() {
        assertFalse(hold.presentation(2000, 0, true).canComplete)
        assertFalse(hold.copy(status = HoldStatus.COMPLETED).presentation(2000, 0, false).canComplete)
    }
}
