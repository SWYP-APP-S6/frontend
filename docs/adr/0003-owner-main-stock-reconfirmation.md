# ADR-0003: Main ViewModel의 재고 재확인 처리

- 상태: 승인됨
- 날짜: 2026-10-07
- 대체하는 ADR: [ADR-0002](0002-owner-notification-refresh-provider.md)

## 배경

재고 재확인 다이얼로그는 Main 전체에서 표시한다. Main ViewModel의 요청을 Channel과 Compose를 거쳐 별도 재고 ViewModel에 전달하는 구조를 단순화한다.

## 결정

- ADR-0002의 구체 Provider, 타입별 SharedFlow와 대기 요청 보관, 홈 Feature에 전달하는 갱신 Flow 및 500ms debounce는 유지한다.
- OwnerMainViewModel이 재고 요청을 직접 구독하고 상품 조회, 다이얼로그 상태, 확인 응답, 오류 후 재조회, 상품 수정 이동 이벤트를 처리한다.
- Main이 RESUMED 상태일 때 재고 조회를 시작한다. 비활성 상태 또는 다이얼로그 처리 중에는 가장 최신의 재고 요청을 보관한다. 활성화 시점은 Compose의 LifecycleResumeEffect로 전달한다.
- 처리할 요청을 활성화할 때 Provider에서 해당 요청만 소비한다. 활성화 전 대기 요청은 Main ViewModel이 다시 만들어져도 Provider에서 전달할 수 있다.
- OwnerNavHost의 인라인 다이얼로그가 Main ViewModel의 상태와 콜백을 사용한다. 별도 재고 ViewModel과 요청 전달용 Channel을 제거한다.
- 상품 수정 이동 이벤트는 RESUMED 상태에서 수집한다. 로그아웃하면 진행 중인 재고 작업과 대기 상태, 보관된 이동 이벤트를 정리한다.

## 결과

- Main이 재고 재확인 상태를 소유하고 Compose는 다이얼로그 표시와 화면 이동을 처리한다.
- 네/아니요 응답과 재시도, 중복 요청·응답 방지 동작을 유지한다. 불확실한 변경 응답을 자동 재전송하지 않는다.
- Main이 없던 동안과 처리 중에는 최신 요청 하나를 보관하며, 프로세스 종료 후의 요청 보존은 지원하지 않는다.

## 검증

- 비활성 Main의 최신 요청, 재활성화, Main ViewModel 재생성, 중복 푸시·알림 클릭, 중복 확인 응답.
- 아니요 응답 후 이동, 오류 후 동일 상품 재조회, 이미 확인된 상품 제외, 로그아웃 시 대기 요청 제거.
- 기존 홈 갱신 이벤트 전달과 Owner 빌드·lint 검증. 실제 FCM 및 기기 화면 검증은 별도다.
