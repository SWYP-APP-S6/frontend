# 점주 홈 (#64)

홈 진입·복귀 시, 또는 상품 등록 이력이 있는 상태에서 당겨서 새로고침 시 `GET /users/me`, `GET /owner/stores/me`, `GET /owner/home`을 함께 조회한다. Repository는 `Flow<Result<T>>`, 화면은 `StateFlow<OwnerHomeUiState>`를 사용한다. 사용자 프로필과 상점 정보는 별도 모델이다.

## 서버 데이터와 화면

- `hasRegisteredProduct`로 최초 안내와 운영 홈을 구분한다. 현재 상품 목록이 비어도 등록 이력이 있으면 운영 홈을 유지한다.
- 상점 이름·분류·승인 상태는 상점 조회에서, 방문 예정·오늘 픽업 완료·판매 수량은 홈의 `summary`에서 읽는다.
- 방문 카드의 `expiresAt`은 시간대가 포함된 절대 시각으로 변환한다. 남은 분은 올림 표시하며, 만료되거나 완료 요청 중이면 픽업 완료 버튼을 비활성화한다.
- 픽업 완료는 `POST /owner/holds/{id}/complete`를 호출한다. 중복 클릭을 차단하고 성공 후 세 조회를 갱신한다. 실패해도 완료로 표시하지 않는다.
- 상품 경고는 응답의 `shortfallQty > 0`일 때 “찜한 수량보다 재고가 N개 부족해요”로 표시한다. 인원수·취소 찜 건수로 변환하거나 재고와 찜 수량으로 재계산하지 않는다.
- `completedTodayCount`, `activeHoldQty`는 서버의 Long 값을 보존한다.
- 로딩·조회 실패는 최초 등록 안내 및 빈 목록과 구분하고 재시도를 제공한다. 새로고침 중에는 등록할 수 없다.

## 승인과 등록 제한

서버 상태 `PENDING / APPROVED / REJECTED` 중 `APPROVED`만 등록을 허용한다. 미확인 문자열·조회 실패·사용자 역할 또는 상점 ID 불일치는 등록을 허용하지 않는다. 홈과 상점 API의 승인 상태가 다르면 등록을 차단한다.

홈의 모든 등록 버튼에 제한을 적용한다. 상품 등록 목적지에서도 진입·복원·복귀 때 상점을 조회하고, 최종 저장 직전 다시 확인한다. 상품 등록 화면의 영업시간도 상점 응답을 사용한다. 상품 생성 API 자체는 기존 상품 기능의 후속 범위이며 현재 저장 콜백은 로컬 UI 호스트에 연결돼 있다.

## 아직 연결할 수 없는 항목

- `unreadNotificationCount`는 전체 미확인 알림 수다. 신규 찜 배너로 변환하지 않는다.
- `expiredTodayCount`, `productsShortOfStock`, `shortfallQty`, `reconfirmPendingCount`만으로 미해결 취소 찜 건수나 수령 확인 건수를 확정하지 않는다. 해당 배너와 “확인해야 할 문제는 없어요” 문구는 운영 홈에서 숨긴다.
- 상품 목록·설정 탭은 기존 목적지로 이동한다. 상품 목록은 아직 로컬 데이터다. 서버 상품·찜 상세, 찜 필터 목록, 알림 목적지는 준비 중 안내를 표시하며 샘플 ID로 연결하지 않는다.
- `OwnerHomeSamples`는 Preview·Compose 테스트 전용이며 운영 ViewModel은 사용하지 않는다.

## 디자인 원본

Figma 파일 `hqglQXERCwjFx1W4amjPHI`: 운영 현황 `1318:26022`, 빈 홈 `1318:25961`, 최초 안내 `1318:25950`.
일러스트·벡터는 기존 Figma 에셋을 유지하며 운영 상품 사진은 서버 `photoUrl`을 사용한다.

## 검증

- 데이터: `:data:user:testDebugUnitTest`, `:data:owner:store:testDebugUnitTest`, `:data:owner:home:testDebugUnitTest`
- 상태·등록 제한: `:feature:owner:home:testDebugUnitTest`, `:feature:owner:product:testDebugUnitTest`
- UI: `:feature:owner:home:connectedDebugAndroidTest` (로딩·오류·승인 제한·부족 수량·시간 만료·글꼴·카드 정렬)
- 앱: Owner/Consumer별 `test…DebugUnitTest`, `lint…Debug`, `assemble…Debug`

MockWebServer·가짜 Repository 검증과 실제 계정 서버 검증은 별도로 보고한다. 계측 스크린샷은 수동 시각 검토용이며 기준 이미지 자동 비교가 아니다.
