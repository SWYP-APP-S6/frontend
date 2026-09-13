# 점주 상품 UI (#65)

`ownerProductNavGraph`는 등록 3단계, 사진 선택·삭제, 상품 미리보기·수정, 목록·상세, 재고 재확인과 직접 입력을 제공한다. `app`의 Owner `OwnerNavHost`에서 홈과 연결하며 Consumer에는 의존하지 않는다.

## 패키지와 Navigation

- `screen/list`, `screen/detail`, `screen/editor`, `screen/stock`: 화면별 UI와 입력 상태.
- `component`: 상품 화면에서 함께 사용하는 UI.
- `model`: 복원 가능한 `OwnerProductModel`.
- `util`: 가격·품목명·사진·태그·수량 검증.
- `navigation`: 타입 기반 목적지와 `NavGraphBuilder.ownerProductNavGraph`. 상품 식별자만 경로 인자로 전달하며 상품 데이터는 호출부에서 제공한다.
- 홈의 점포 관리 메뉴·상품 목록 액션은 목록으로, 상품 카드는 상세로, 등록 액션은 등록 화면으로 이동한다. 홈도 같은 상품 목록을 표시한다.
- 수정 저장/취소는 이전 상세 화면으로, 목록에서 등록 후에는 목록으로 복귀한다. 시스템 뒤로가기와 상단 뒤로가기는 Navigation 백스택을 사용하며 등록 단계 뒤로가기·작성 취소 확인은 유지한다.
- 찜 취소 안내도 별도 목적지로 열어 돌아오면 재고 입력 상태를 유지한다. 실제 찜 취소 API는 아직 연결하지 않았다.

- 호출부가 `products`, `storeClosingTime(HH:mm)`을 제공한다. `onSaveProducts`에는 변경된 상품들만 전달한다. 호출부는 ID로 병합해야 한다.
- 재고 부족 시 `onCancelReservations`에 해당 상품 ID를 전달한다. 찜 취소 실행·선착순 주문 배정·안내 발송은 #66의 `:feature:owner:pickup` 책임이다. 상품 UI는 부족 수량을 주문 건수로 추정하지 않는다.
- 매장 잔여 수량은 찜에 배정된 수량을 포함한다. 소비자에게 추가 판매 가능한 수량은 `max(잔여 - 찜, 0)`이며 0이면 `isVisibleToCustomers`가 false다. 소비자 조회 API의 노출 필터 연결은 별도다.
- 사진 URI 읽기 권한을 유지한다. 입력 및 목록은 화면 재생성 시 복원된다. 현재 Owner 앱은 메모리/저장 상태 기반 UI 호스트이며 서버 저장·영구 보관·업로드는 구현하지 않는다. 운영시간 20:00도 호스트 예시로, 실제 매장 설정을 주입해야 한다.

## 디자인 근거와 미확정 사항

[Figma 점주 플로우](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-17186)

- 등록: `1172:18171`, `1172:18202`, `1172:21439`
- 미리보기: `1172:18127`
- 목록: `1172:17653`, 상세: `1172:17195`, 수량 확인: `1172:18073`
- 사진/태그: 입력 완료 화면의 최대 5개 기준으로 통일. 빈 화면의 n 표기 및 이슈의 10장 표기는 미확정으로 기록한다.
- 품목명: 목록 예시의 25자 내외 문구를 근거로 최대 25 Unicode 코드포인트 적용. 공백만 입력할 수 없으며 앞뒤 공백은 제거한다. 정확한 정책 확정 필요.
- 태그는 자유 입력이며 첫 태그를 대표로 표시한다. 카테고리 자동 분류 규칙이 없어 미리보기 분류는 기타로 표시한다.
- 할인율은 정수 내림. 정가/할인가 모두 양수, 할인가 ≤ 정가. 정수 범위를 넘는 입력은 허용하지 않는다.
- O-050/O-051은 제공된 섹션에서 찾지 못해 이슈의 재고 재확인·직접 입력·저장·나중에 하기 요구와 기존 디자인 시스템으로 구성했다. 별도 노드 확인 후 시각 비교 필요.

## 검증

단위 테스트는 가격·수량 경계값, 할인율 오버플로, Unicode 품목명, 사진/태그 제한, 찜 수량 대비 가용 재고를 확인한다. 앱의 `ownerAndroidTest`는 호이스팅된 데이터로 등록/수정/재고 UI를 검사한다. 실행 결과는 작업 완료 보고를 참조한다.

### 실행 기록 (2026-09-10)

- `ktlintCheck`, `:feature:owner:product:testDebugUnitTest` (6개): 통과.
- `:app:testOwnerDebugUnitTest`, `:app:testConsumerDebugUnitTest`, `:app:assembleOwnerDebug`, `:app:assembleConsumerDebug`, `:app:lintOwnerDebug`, `:app:lintConsumerDebug`: 통과. Lint 기존 의존성/리소스 경고는 남아 있다.
- `:app:connectedOwnerDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.swyp.mangro.OwnerProductFlowTest`: Pixel 10 API 37에서 6개 통과. 필수 입력, 수정/미리보기, 재고 0 확인, 직접 입력 보류, 부족 재고 콜백, 저장 상태 복원을 확인했다.
- 수동 확인: 시스템 사진 선택기에서 이미지 선택 → 썸네일 → 품목명/가격 입력 → 60% 할인율 → 입력 중 태그를 포함한 미리보기.
- 미실행: Release 빌드, 실기기, Consumer 계측, 전체 앱 계측, 서버 업로드/저장 및 실제 찜 취소. O-050/O-051의 원본 디자인과 시각 비교도 미실행이다.

### Navigation 리팩터링 검증 (2026-09-13)

- 전체 `ktlintCheck`, 상품 단위 테스트 6개·홈 4개·앱 Flavor별 1개, Consumer/Owner Debug 조립·lint: 통과.
- `:app:connectedOwnerDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.swyp.mangro.OwnerProductFlowTest,com.swyp.mangro.OwnerNavigationTest`: Samsung SM-F711N / Android 15에서 8개 통과, 건너뜀 없음.
- 홈 → 점포 관리와 상단/시스템 뒤로가기, 작성 취소 확인, 수정 저장 후 상세 복귀, 재고 확인·보류, 입력 상태 복원을 검증했다.
- 미실행: Release 빌드, Consumer 계측, 실제 서버 저장·업로드·찜 취소.
