# ADR-0002: 점주 알림 Provider의 타입별 이벤트 전달

- 상태: 대체됨
- 날짜: 2026-10-06
- 대체 기록: [ADR-0003](0003-owner-main-stock-reconfirmation.md)

## 배경

점주 푸시는 알림 종류에 따라 Main의 재고 다이얼로그 또는 홈 데이터 갱신을 요청한다.
서비스의 companion 상태와 화면 처리 분기를 Provider로 옮기고, 다른 화면에서도 유지 중인 홈 ViewModel을 갱신해야 한다.

## 결정

- app Owner의 `notification/provider`에 구체 `NotificationProvider` 클래스를 두고 Hilt `@Provides`로 Singleton을 제공한다. 별도 구독 인터페이스는 두지 않는다.
- Provider가 `STOCK_RECONFIRM_REQUEST`를 상품 ID가 있는 다이얼로그 이벤트로, `NEW_HOLD_RECEIVED`, `HOLD_EXPIRED`를 홈 갱신 이벤트로 구분한다. `HOLD_UNCONFIRMED`는 화면 처리 이벤트를 발행하지 않는다.
- SharedFlow로 여러 ViewModel에 이벤트를 전달한다. 최신 홈 갱신과 아직 전달 완료되지 않은 재고 요청은 타입별 상태로 보관하고, SharedFlow 구독 등록 후 전달한다. 상태 갱신에는 StateFlow의 원자적 갱신을 사용하며 직접 락을 두지 않는다.
- 서비스는 세션을 확인하고 Provider에 수신 정보를 넘긴 뒤 시스템 알림을 표시한다. 다이얼로그 분기와 대기 상태는 갖지 않는다.
- Main ViewModel은 다이얼로그 요청을 받아 UI가 활성화되면 재고 ViewModel에 전달한다. 전달한 요청만 소비하며 새 요청을 지우지 않는다. 로그아웃하면 대기 요청을 지운다.
- app은 홈에 갱신 Flow를 넘기고 HomeViewModel이 자기 `viewModelScope`에서 구독한다. 홈 Feature는 app 구현을 참조하지 않는다.
- 홈 갱신 Flow는 요청 번호 없이 `Unit` 신호를 전달한다. HomeViewModel에서 500ms debounce를 적용해 연속 요청을 모으고, 초기/진행 중 조회가 끝난 후 대기 중인 최신 신호를 처리한다. 자동 갱신은 기존 화면 데이터를 유지한다. 수동 새로고침과 재고 다이얼로그 요청은 debounce 대상에 포함하지 않는다.

## 결과

- 홈이 백스택에 남아 있으면 화면 활성 상태와 무관하게 갱신할 수 있다.
- 새 구독자는 마지막 홈 갱신과 마지막 대기 재고 요청을 받는다. 프로세스 종료 후에는 새 홈의 초기 서버 조회로 복원한다.
- Provider에 구독자 목록과 수동 동기화를 둘 필요가 없다.
- Main ViewModel의 활성 구독 동안 받은 다이얼로그 이벤트는 UI 전달 대기열에 보관한다. Main이 없던 동안에는 기존 동작과 같이 마지막 재고 요청을 보관한다.

## 검토한 대안

- 구독자별 Channel은 등록/해제와 별도 동기화가 필요하므로 SharedFlow를 사용한다.
- 별도 Provider 인터페이스는 두지 않고 app에서 구체 클래스를 제공한다. Feature에는 필요한 Flow만 전달한다.

## 검증

- 타입별 분기, 잘못된 상품 ID, 여러 구독자, 지연 구독, 재고 요청 소비/로그아웃, 동시 발행.
- Main UI 비활성 동안의 요청 보관과 ViewModel 재생성 시 미소비 요청 전달.
- 홈 UI 구독 없는 갱신, 최초/수동/자동 조회 중 추가 요청, ViewModel 제거 시 구독 종료.

## 재검토 조건

프로세스 종료 후의 요청 보존이나 Main이 없는 동안 모든 재고 요청의 개별 처리가 필요해지면 저장소와 소비 계약을 확장한다.
