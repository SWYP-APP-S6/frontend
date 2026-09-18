package com.swyp.mangro.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.notification.model.OwnerNotificationType
import com.swyp.mangro.notification.model.OwnerPushMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerNotificationDisplayTest {
    @Test fun ownerAlertsUseChannelServerTextAndImmutableAppIntent() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        val tag = "owner-notification-test"
        try {
            OwnerNotificationType.entries.forEach { type ->
                OwnerNotificationDisplay.show(context, OwnerPushMessage(type, "테스트 제목", "테스트 본문"), tag)
                val posted = manager.activeNotifications.single { it.tag == tag }.notification
                assertEquals(OwnerNotificationDisplay.CHANNEL_ID, posted.channelId)
                assertEquals("테스트 제목", posted.extras.getString(Notification.EXTRA_TITLE))
                assertEquals("테스트 본문", posted.extras.getString(Notification.EXTRA_TEXT))
                assertNotNull(posted.contentIntent)
                if (Build.VERSION.SDK_INT >= 31) assertTrue(posted.contentIntent.isImmutable)
            }
            assertEquals(1, manager.activeNotifications.count { it.tag == tag })
        } finally {
            manager.cancel(tag, 0)
        }
    }
}
