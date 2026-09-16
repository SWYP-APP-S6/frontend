package com.swyp.mangro.notification

import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.notification.model.OwnerNotificationType
import com.swyp.mangro.notification.model.OwnerPushMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OwnerPushMessageTest {
    @Test fun payloadWithoutDeepLinkKeepsReadIdAndOpensOwnerHome() {
        val message = OwnerPushMessage.from("NEW_HOLD_RECEIVED", "새 찜이 들어왔어요", "서버 본문", "1024")!!
        assertEquals(1024L, message.notificationId)
        val opened = OwnerNotificationOpen.from(message.type.name, "1024")!!
        assertEquals(message.type, opened.type)
        assertEquals(1024L, opened.notificationId)
    }

    @Test fun invalidReadIdsAreNeverUsedAndConsumerTapsAreIgnored() {
        listOf("-1", "0", "bad", "9223372036854775808", null).forEach {
            assertNull(OwnerNotificationOpen.from("HOLD_UNCONFIRMED", it)!!.notificationId)
        }
        assertNull(OwnerNotificationOpen.from("HOLD_EXPIRED", "1024"))
    }

    @Test fun acceptsEveryOwnerTypeAndPreservesServerText() {
        OwnerNotificationType.entries.forEach {
            val message = OwnerPushMessage.from(it.name, "서버 제목", "서버 본문")!!
            assertEquals(it, message.type)
            assertEquals("서버 제목", message.title)
            assertEquals("서버 본문", message.body)
        }
    }

    @Test fun consumerUnknownAndIncompleteMessagesAreIgnored() {
        listOf("HOLD_EXPIRED", "HOLD_EXPIRING_SOON", "PICKUP_COMPLETED", "HOLD_CANCELED_BY_OWNER", "UNKNOWN", null).forEach {
            assertNull(OwnerPushMessage.from(it, "제목", "본문"))
        }
        assertNull(OwnerPushMessage.from("NEW_HOLD_RECEIVED", "", "본문"))
        assertNull(OwnerPushMessage.from("NEW_HOLD_RECEIVED", "제목", null))
    }
}
