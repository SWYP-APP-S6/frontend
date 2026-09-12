# 점주 홈 (#64)

`OwnerHomeScreen(state, onAction)`은 최초 등록 안내, 빈 홈, 운영 중 홈을 표시한다.
`hasRegisteredProduct`는 상품 목록의 현재 크기와 별개다. 등록 이력이 있는 매장의 판매 상품이 모두 없어져도 최초 안내로 되돌리지 않는다.

- 홈 화면과 전용 구성요소: `src/main/.../OwnerHomeScreen.kt`
- 화면 상태·외부 액션: `src/main/.../OwnerHomeUiState.kt`
- 예제 데이터: `src/main/.../OwnerHomeSamples.kt`
- 세 상태의 Preview: `src/debug/.../OwnerHomePreview.kt`
- Owner 앱 조립: `app/src/owner/.../MainScreen.kt`
- Owner Debug와 Release 모두 운영 중 샘플 상태로 시작한다. 데이터 계층 연결 전의 임시 구성으로, 로그인·매장 등록·운영 현황을 실제 조회하지 않는다. Preview만 Debug에 포함한다.

## 연결 계약

| 액션                                                               | 담당 기능              |
|------------------------------------------------------------------|--------------------|
| RegisterProduct / ViewProducts / ViewProduct(productId)          | #65 등록·상품 목록·관리 상세 |
| ViewPickups / ViewNewPickups / ViewCompletedPickups              | #66 찜 목록과 해당 필터    |
| ViewPickup(pickupId) / CompletePickup(pickupId) / ConfirmPickups | #66 상세·픽업 처리·수령 확인 |
| ViewCancellations                                                | #66 취소 대상 확인       |
| DismissAttention                                                 | 호출자가 안내 표시 상태 변경   |
| ViewNotifications                                                | 목적지 명세 확인 필요       |

실제 내비게이션은 #56에서 조립한다. 현재 목적지 이동과 픽업 완료·취소 처리는 미연결이며, 클릭해도 Snackbar나 처리 결과를 표시하지 않는다. 홈 외 하단 메뉴도 아직 화면을 전환하지 않는다.

샘플 화면 확인을 위해 `RegisterProduct`는 등록 이력 표시를 전환하고, `ViewNewPickups`는 신규 찜 배너를 토글한다. 실제 등록·조회 동작 연결 시 교체해야 한다.

## 상태 처리

- 신규 찜 배너, 확인 필요 안내, 방문 예정·상품 목록은 독립적으로 표시한다.
- 안내를 닫아도 문제 건수는 유지하며, 문제가 남아 있으면 ‘확인해야 할 문제는 없어요’ 문구를 표시하지 않는다.
- 남은 시간은 종료 시각과 현재 시각의 차이를 분 단위 올림으로 표시한다. 화면이 보이는 동안 갱신하고, 복귀 시 다시 계산한다. 만료 시 픽업 완료를 비활성화한다.
- 홈 집계와 구매 불가 인원은 입력값을 그대로 표시한다. 집계 기간·단위와 부족 인원 계산은 데이터 계약 확정이 필요하다.
- 최초 안내 노출/완료 저장, 새로운 알림 수신 시 닫힘 상태 초기화는 향후 데이터 연결 책임이다.

## 디자인 원본

Figma 파일: `hqglQXERCwjFx1W4amjPHI`

- 운영 현황 `1318:26022`, 빈 홈 `1318:25961`, 최초 안내 `1318:25950`
- 환영 일러스트: `src/main/res/drawable/welcome_store.xml`
- 빈 상태 일러스트: `1288:24267`, `1288:24306`의 원본 SVG를 Android `Svg2Vector`로 변환
- 상품 사진은 Picsum 랜덤 이미지 URL을 사용하며 실제 상품과 무관하다. 네트워크와 이미지 캐시 상태에 따라 사진 또는 로딩 배경이 표시될 수 있다.
- 추가된 `ic_owner_*` 벡터는 Figma의 원본 SVG path와 색상을 기계적으로 VectorDrawable로 변환했다.

## 검증

모듈 단위 테스트와 Compose 계측 테스트는 `testDebugUnitTest`, `connectedDebugAndroidTest`로 실행한다.
앱 검증은 `testOwnerDebugUnitTest`, `testConsumerDebugUnitTest`, `lintOwnerDebug`, `lintConsumerDebug`, `assembleOwnerDebug`, `assembleConsumerDebug`처럼 Flavor를 명시한다.

`OwnerHomeScreenTest`는 최초 안내·빈 상태·운영 상태, 안내 닫기, 기본 글꼴에서 카드 크기·시간 정렬, 만료 후 클릭 차단, 1.3배 글꼴을 검증한다. `additionalTestOutputDir` 계측 인자를 전달하면 테스트에서 캡처한 PNG를 Gradle 추가 산출물로 수집할 수 있다. 캡처는 수동 시각 검토용이며 기준 이미지와 자동 비교하는 테스트는 아니다.
