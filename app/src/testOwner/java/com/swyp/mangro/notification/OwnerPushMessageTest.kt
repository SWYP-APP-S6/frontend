package com.swyp.mangro.notification

import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.notification.model.OwnerNotificationType
import com.swyp.mangro.notification.model.OwnerPushMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OwnerPushMessageTest {
    @Test fun stockReconfirmationUsesProductIdFromContractDeepLink() {
        val message = OwnerPushMessage.from(
            "STOCK_RECONFIRM_REQUEST",
            "재고를 확인해주세요",
            "서버 본문",
            "1024",
            "mangro://owner/products/77",
        )!!
        assertEquals(1024L, message.notificationId)
        assertEquals(77L, message.productId)
        val opened = OwnerNotificationOpen.from(message.type.name, "1024", message.deepLink)!!
        assertEquals(message.type, opened.type)
        assertEquals(1024L, opened.notificationId)
        assertEquals(77L, opened.productId)
    }

    @Test fun invalidReadIdsAreNeverUsedAndConsumerTapsAreIgnored() {
        listOf("-1", "0", "bad", "9223372036854775808", null).forEach {
            assertNull(OwnerNotificationOpen.from("HOLD_UNCONFIRMED", it)!!.notificationId)
        }
        assertNull(OwnerNotificationOpen.from("NEARBY_PRODUCT_REGISTERED", "1024"))
    }

    @Test fun malformedOrUnrelatedDeepLinksNeverTargetAStockDialog() {
        listOf(
            null,
            "",
            "mangro://owner/products/0",
            "mangro://owner/products/-1",
            "mangro://owner/products/7?source=push",
            "https://owner/products/7",
            "mangro://consumer/products/7",
        ).forEach { deepLink ->
            val message = OwnerPushMessage.from("STOCK_RECONFIRM_REQUEST", "제목", "본문", deepLink = deepLink)!!
            assertNull(message.productId)
            assertNull(OwnerNotificationOpen.from(message.type.name, null, deepLink)!!.productId)
        }
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
        listOf("HOLD_EXPIRING_SOON", "PICKUP_COMPLETED", "HOLD_CANCELED_BY_OWNER", "UNKNOWN", null).forEach {
            assertNull(OwnerPushMessage.from(it, "제목", "본문"))
        }
        assertNull(OwnerPushMessage.from("NEW_HOLD_RECEIVED", "", "본문"))
        assertNull(OwnerPushMessage.from("NEW_HOLD_RECEIVED", "제목", null))
    }
}
