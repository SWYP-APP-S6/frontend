# 점주 FCM 알림

현재 브랜치에서 Owner 전용 Firebase Messaging 수신 서비스와 알림 권한, 기기 토큰 등록을 제공한다. Consumer에는 Firebase SDK·서비스·알림 권한을 추가하지 않는다.

## 서버/앱 책임

- 서버: 찜 생성, 등록 수량 60% 도달, 수령 미확인 판정 및 중복 발송 제어·배치 실행.
- 앱: `NEW_HOLD_RECEIVED`, `STOCK_RECONFIRM_REQUEST`, `HOLD_UNCONFIRMED` 표시. 미확인 타입과 Consumer 타입은 수신 서비스에서 무시한다.
- 앱은 수신으로 재고를 변경하거나 찜을 완료/취소하지 않는다.

## Firebase 설정

`com.swyp.mangro.owner` 앱의 설정 파일을 `app/src/owner/google-services.json`에 둔다(버전 관리 제외). 이 파일이 있을 때 Google Services 플러그인을 활성화한다. 파일이 없으면 빌드는 가능하지만 실제 FCM 등록·수신은 활성화되지 않는다. 실제 프로젝트 자격 증명을 코드에 넣지 않는다.

로그인 이후 Owner 메인 화면에서 Android 13+ 알림 권한을 한 번 요청한다. 화면 재진입 및 토큰 갱신 시 WorkManager가 최신 토큰을 `POST /notifications/device-tokens`에 `fcmToken`, `platform=ANDROID`로 전송한다. 네트워크 실패·서버 오류는 재시도한다. 권한을 끈 경우 로그인된 세션으로 같은 기기의 토큰을 DELETE한다.

현재 브랜치에는 로그아웃 구현이 없다. 로그아웃 변경을 통합할 때 인증 토큰 삭제 전에 `OwnerNotificationRepository.deleteToken()` 호출을 연결해야 한다. 미인증 상태에서는 새 토큰을 등록하지 않는다.

## 확인된 서버 payload

서버는 `notification.title/body` 및 문자열 `data.type`, `data.notificationId`를 전송한다. `deepLink`는 현재 모든 알림에서 생략되며 상품/찜 ID도 없다. 서버의 제목·본문을 그대로 표시한다. 수신 서비스는 data-only의 title/body도 지원하되 누락된 본문을 임의로 만들지 않는다.

Firebase 표준에 따라 notification 메시지는 백그라운드에서 SDK가 표시한다. 포그라운드와 data-only 메시지는 수신 서비스가 표시한다. 백그라운드 notification 메시지는 앱의 타입/세션 필터를 거치지 않으므로 서버의 올바른 사용자·기기 대상 지정이 필요하다.

알림 클릭은 점주 홈으로 이동하여 홈 정보를 다시 조회하고 `PATCH /notifications/{notificationId}/read`를 실행한다. 읽음 요청은 WorkManager로 네트워크 복구 시 재시도한다. MainActivity의 최초 Intent·onNewIntent를 모두 처리하며, 미로그인 상태에서는 로그인 완료까지 이동·읽음 처리를 보류한다. 화면 재생성 시 같은 Intent를 중복 처리하지 않는다. 읽음 처리 완료 시점은 비동기이므로 홈의 미확인 개수는 다음 조회에서 반영될 수 있다.

## 검증

- MockWebServer: 인증 헤더, Android 토큰 등록/삭제 경로·본문, 실패 및 빈 토큰 처리.
- Owner 단위 테스트: 점주 3종과 Consumer/알 수 없는 타입 구분.
- 기기 테스트: 채널·제목·본문·immutable PendingIntent·동일 메시지 중복 표시 방지.
- 사용자 요청으로 Firebase 설정 파일은 추가하지 않는다. 실제 프로젝트에서 발송·수신하는 검증은 파일을 추가한 뒤 수행한다.

서버가 channel_id를 지정한다면 앱의 `owner-store-alerts`와 맞춘다. 서버 env에 지정이 없으면 앱의 기본 채널 metadata를 사용한다.

공식 참고: [Android FCM 수신](https://firebase.google.com/docs/cloud-messaging/android/receive-messages), [Firebase Android 설정](https://firebase.google.com/docs/android/setup).
