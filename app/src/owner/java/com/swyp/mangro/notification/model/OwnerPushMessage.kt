package com.swyp.mangro.notification.model

/**
 * 점주 앱에서 처리하는 푸시 알림 종류. 각 이름은 서버의 `data.type` 값과 일치한다.
 *
 * @property NEW_HOLD_RECEIVED 새로운 찜이 접수된 경우
 * @property STOCK_RECONFIRM_REQUEST 특정 상품의 실제 재고를 다시 확인하는 요청
 * @property HOLD_UNCONFIRMED 찜의 픽업 여부를 확인하도록 요청
 * @property HOLD_EXPIRED 찜이 만료됨을 알림
 * */
enum class OwnerNotificationType {
    NEW_HOLD_RECEIVED,
    STOCK_RECONFIRM_REQUEST,
    HOLD_UNCONFIRMED,
    HOLD_EXPIRED,
}

/**
 * 수신한 점주 푸시를 시스템 알림 표시와 재고 재확인 요청에 전달하는 모델.
 *
 * 알림 클릭 후 화면 이동에 사용하는 [OwnerNotificationOpen]과 달리 표시할 제목과 본문을 함께 보관한다.
 * 문자열 형태의 수신 데이터는 [from]을 통해 변환하며, 상품 ID는 딥링크에서 계산한다.
 *
 * @property type 점주 앱에서 처리할 알림 종류.
 * @property title 시스템 알림에 표시할 서버 제목.
 * @property body 시스템 알림에 표시할 서버 본문.
 * @property notificationId 알림 식별과 클릭 후 읽음 처리에 사용하는 서버 알림 ID.
 * @property deepLink 알림 클릭 시 전달하며 재고 재확인 대상 상품을 식별하는 데 사용하는 딥링크.
 */
data class OwnerPushMessage(
    val type: OwnerNotificationType,
    val title: String,
    val body: String,
    val notificationId: Long? = null,
    val deepLink: String? = null,
) {
    /**
     * 재고 재확인 알림의 딥링크에서 추출한 상품 ID.
     *
     * [type]이 [OwnerNotificationType.STOCK_RECONFIRM_REQUEST]이고 딥링크가
     * `mangro://owner/products/{productId}` 형식의 양수 상품 ID를 포함할 때만 반환한다.
     * 다른 알림 종류이거나 딥링크 검증에 실패하면 `null`이며, 대상 상품을 임의로 추정하지 않는다.
     */
    val productId: Long?
        get() = if (type == OwnerNotificationType.STOCK_RECONFIRM_REQUEST) {
            parseOwnerProductDeepLink(deepLink)?.productId
        } else {
            null
        }

    companion object {
        /**
         * 문자열 형태의 푸시 수신 데이터를 점주 알림 모델로 변환한다.
         *
         * [type]이 [OwnerNotificationType]의 이름과 정확히 일치하지 않거나
         * [title] 또는 [body]가 없거나 공백뿐이면 `null`을 반환한다. 유효한 제목과 본문은 원문을 유지한다.
         *
         * [notificationId]는 양수 [Long]으로 변환할 수 있을 때만 보관하며, 그렇지 않으면 `null`로 둔다.
         * [deepLink]는 앞뒤 공백을 제거하고 빈 값은 `null`로 둔다. 상품 대상 검증은 [productId]에서 수행한다.
         *
         * @return 표시할 내용을 갖춘 점주 푸시 모델. 지원하지 않는 종류이거나 제목·본문이 누락되면 `null`.
         */
        fun from(
            type: String?,
            title: String?,
            body: String?,
            notificationId: String? = null,
            deepLink: String? = null,
        ): OwnerPushMessage? {
            val ownerType = OwnerNotificationType.entries.firstOrNull { it.name == type } ?: return null
            if (title.isNullOrBlank() || body.isNullOrBlank()) return null

            return OwnerPushMessage(
                ownerType,
                title,
                body,
                notificationId?.toLongOrNull()?.takeIf { it > 0 },
                deepLink?.trim()?.takeIf { it.isNotEmpty() },
            )
        }
    }
}
