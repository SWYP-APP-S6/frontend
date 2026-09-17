# 점주 상품 UI (#65)

`ownerProductNavGraph`는 등록 3단계, 사진 선택·삭제, 등록 미리보기·입력 재수정, 점포 관리·상세을 제공한다. `app`의 Owner `OwnerNavHost`에서 홈과 연결하며 Consumer에는 의존하지 않는다.

## 패키지와 Navigation

- 상품 작성은 `screen/editor/basic`, `screen/editor/price`, `screen/editor/pickup`으로 분리한다. 각 Screen 파일 상단에 Destination을 선언하고 Route와 Screen을 함께 둔다. State, Action, Event, ViewModel은 같은 화면 패키지의 별도 파일에 둔다.
- 각 Route에서 화면 범위의 `hiltViewModel()`을 생성한다. 초기 상태와 입력 검증, 사진/태그 변경, 미리보기·작성 취소는 해당 ViewModel이 처리하며 `SavedStateHandle`에 복원 가능한 상태를 저장한다. `TextFieldState`는 UI에만 둔다.
- `ProductEditorNavHost`에서 사진·품목명 → 수량·가격 → 픽업시간 선택·태그로 이동한다. 각 ViewModel이 자신의 기본값이 있는 State를 SavedStateHandle에 보존한다. 다음 단계에는 ProductDraftModel만 전달하고, 상단/시스템 뒤로가기는 이전 목적지로 돌아간다. 미리보기의 수정 액션은 입력을 유지한 채 첫 목적지로 돌아간다.

- `screen/list`, `screen/detail`, `screen/editor`: 화면별 UI. 점포 관리와 상세는 `ProductDetailState`, `Action`, `Event`, `ViewModel`, `Route`, `Screen`으로 분리한다.
- `component`: 상품 화면에서 함께 사용하는 UI.
- `model`: 복원 가능한 `OwnerProductModel`.
- `util`: 가격·품목명·사진·수량 검증.
- `navigation`: 타입 기반 목적지와 `NavGraphBuilder.ownerProductNavGraph`. 상품 식별자만 경로 인자로 전달하며 상품 데이터는 호출부에서 제공한다.
- 상세 Route가 화면 범위의 `hiltViewModel()`을 생성하고 상태를 구독한다. ViewModel이 `SavedStateHandle.toRoute<OwnerProductDetailDestination>()`으로 상품 ID를 읽고 전달받은 목록에서 상품을 선택한다. 상세 Navigation에서는 인자 해석이나 상품 조회를 하지 않는다. 수량·확인/저장 시트는 ViewModel과 SavedStateHandle이 관리하며 저장·이동 이벤트는 Route가 기존 호출부 콜백으로 전달한다. 수정 후 상품 변경을 반영하되 잔여 수량이 같으면 작성 중인 수량을 유지한다.
- 홈의 점포 관리 메뉴·상품 목록 액션은 목록으로, 상품 카드는 상세로, 등록 액션은 등록 화면으로 이동한다. 홈도 같은 상품 목록을 표시한다.
- editor는 신규 등록만 지원하며 기존 상품 ID를 받지 않는다. 등록 저장/취소는 진입 전 화면으로 복귀한다. 시스템 뒤로가기와 상단 뒤로가기는 Navigation 백스택을 사용하며 등록 단계 뒤로가기·작성 취소 확인은 유지한다.
- 찜 취소 안내도 별도 목적지로 열어 돌아오면 상세의 수량 입력 상태를 유지한다. 실제 찜 취소 API는 아직 연결하지 않았다.

- 호출부가 `products`, `storeOpeningTime(HH:mm)`, `storeClosingTime(HH:mm)`을 제공한다. `onSaveProducts`에는 변경된 상품들만 전달한다. 호출부는 ID로 병합해야 한다.
- 재고 부족 시 `onCancelReservations`에 해당 상품 ID를 전달한다. 찜 취소 화면과 선착순 배정은 이 모듈의 `screen/pickup` 및 `OwnerPickupStore`에서 담당한다. 실제 안내 발송은 API 미연결 범위다. 상품 UI는 부족 수량을 주문 건수로 추정하지 않는다.
- 매장 잔여 수량은 찜에 배정된 수량을 포함한다. 소비자에게 추가 판매 가능한 수량은 `max(잔여 - 찜, 0)`이며 0이면 `isVisibleToCustomers`가 false다. 소비자 조회 API의 노출 필터 연결은 별도다.
- 사진 URI 읽기 권한을 유지한다. 입력 및 목록은 화면 재생성 시 복원된다. 등록은 서버 업로드·저장에 연결되어 있고 운영시간은 매장 조회 응답을 사용한다. 점포 관리 목록·상세의 로컬 상태와 재고 수정 API 연결은 별도 범위다.

## 디자인 근거와 미확정 사항

[Figma 점주 플로우](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-17186)

- 등록: `1172:18171`, `1172:18202`, `1172:21439`
- 미리보기: `1172:18127`
- 목록: `1172:17653`, 상세: `1172:17195`, 수량 확인: `1172:18073`
- 사진: 단일 사진 선택기로 최대 1장만 등록한다. 기존 사진을 삭제한 뒤 다시 선택할 수 있다.
- 품목명: 목록 예시의 25자 내외 문구를 근거로 최대 25 Unicode 코드포인트 적용. 공백만 입력할 수 없으며 앞뒤 공백은 제거한다. 정확한 정책 확정 필요.
- 상품 등록에서 커스텀 태그 입력은 제공하지 않는다. 미리보기 분류는 매장 조회 응답의 카테고리를 표시한다.
- 할인율은 정수 내림. 정가/할인가 모두 양수, 할인가 ≤ 정가. 정수 범위를 넘는 입력은 허용하지 않는다.
- 점포 관리 기준은 `1731:4845`(등록 상품), `1731:5136`(찜 현황), `1731:5523`(빈 필터 결과)이다. 이전에 디자인 확인 없이 만든 `ProductStockScreen`과 재고 재확인 목적지·버튼은 제거했다.
- 점포 관리는 하단 탭을 유지하는 최상위 화면이다. 상품 등록은 홈에서 진입하며, 점포 관리에는 별도 등록/뒤로가기 버튼을 두지 않는다. 홈 탭 왕복 시 상단 탭과 필터를 복원한다.
- 등록 상품/찜 현황 탭과 전체·픽업완료·픽업불가·찜 취소·만료된 찜 필터를 제공한다. 상품 탭의 상태 필터는 해당 상태의 찜이 연결된 상품을 표시한다.
- `OwnerPickupModel`로 찜 레코드를 별도로 받는다. 취소 필요 건수는 픽업불가 레코드 수이며 부족 재고 수량으로 추정하지 않는다. 신규 찜 표시, 타이머, 완료/취소/만료 상태는 공통 `OwnerPickupRequestCard`를 사용한다.
- 실제 앱의 찜 공급원과 조회/완료 API는 미연결이며 기본값은 빈 목록이다. 연결 없이 완료 상태를 임의 변경하지 않는다. 설정 탭 연결 역시 기존처럼 미구현이다. Figma 예시 데이터와 이미지는 계측 테스트에만 둔다.

## 검증

아래 과거 실행 기록은 해당 시점의 코드 기준이며, 삭제된 재고 화면의 테스트 결과는 현재 디자인 검증 근거가 아니다.

단위 테스트는 가격·수량 경계값, 할인율 오버플로, Unicode 품목명, 사진 1장 제한, 찜 수량 대비 가용 재고를 확인한다. 앱의 `ownerAndroidTest`는 호이스팅된 데이터로 등록/재고 UI를 검사한다. 실행 결과는 작업 완료 보고를 참조한다.

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

### 상세 ViewModel 분리 검증 (2026-09-13)

- 상품/앱 ktlint, Owner Debug 빌드·lint: 통과.
- 상품 단위 테스트 12개: 통과. 상세 ViewModel 테스트 6개는 수량 검증, 확인 후 저장, 중복 저장 방지, 완료/취소 이동, 저장 상태로 ViewModel 재생성, 수정 결과 동기화를 확인한다.
- Samsung SM-F711N / Android 15의 기존 화면 테스트 8개: 통과. 상세 Route는 실제 Hilt ViewModel을 사용하며 테스트 전용 빈 Hilt Activity는 Owner Debug에만 포함한다.
- Consumer 및 Release 검증은 이번 변경에서 미실행. 실제 서버 저장은 기존처럼 미연결이다.

### 상세 Navigation 인자 이전 검증 (2026-09-13)

- `productId` 해석과 상품 선택을 상세 ViewModel로 이동했다. 수정·찜 취소 이벤트가 상품 ID를 전달한다.
- 상품 단위 테스트 6개와 Android 상세 ViewModel 테스트 8개 통과. `toRoute()`가 Android Bundle을 사용하므로 상세 테스트는 `src/androidTest`에서 실행한다.
- Owner Debug 빌드·lint, 상품/앱 ktlint 통과.
- 앱 화면 테스트 재실행 중 실기기 화면이 자동 잠금되어 Compose UI 계층을 인식하지 못했다. 해당 재실행은 통과로 간주하지 않는다.

### 점포 관리 Figma 재점검 (2026-09-13)

- 기준: `1731:4845`, `1731:5136`, `1731:5523`. 재고 전용 화면과 임의의 등록/뒤로가기 버튼, 판매 가능/재고 없음 필터를 제거했다.
- 최종 Owner·Consumer Debug 빌드, Owner lint, 상품·디자인 시스템·앱 ktlint, 상품 단위 테스트 10개와 Owner 앱 단위 테스트 1개 통과.
- Samsung SM-F711N / Android 15에서 최종 계측 테스트 10개 통과. 홈/점포 관리 왕복과 탭·필터 복원, 등록·상세 수정 회귀, 상품/찜 카드와 취소 안내, 빈 필터 상태를 확인했다.
- 키보드가 열린 상태에서 단계 이동이 누락된 기존 수정 테스트는 키보드를 닫은 후 진행하도록 보완했다. 최종 재실행은 건너뜀/실패 없이 통과했다.
- Figma 원본 이미지로 세 상태를 캡처해 비교했다. 이미지와 예시 찜은 `androidTestOwner`에만 포함한다. 테스트의 건수는 실제 fixture 크기이며 Figma의 99/20을 고정하지 않는다.
- 미실행: Release, Consumer 계측, 전체 앱 계측. 실제 찜 공급원·조회·완료·취소 API와 설정 탭은 미연결이다.

### 상품 작성 단계 분리 검증 (2026-09-14)

- 단계별 패키지와 Route/Screen/State/Action/Event/ViewModel을 분리하고, 각 목적지의 Hilt ViewModel과 SavedStateHandle을 사용한다.
- Owner Debug 빌드·lint, 상품/앱 ktlint, 상품 단위 테스트 10개 통과.
- 실기기 단독 실행에서 화면 테스트 9개 통과. 입력 직후 복원, 3단계 복원, 시스템/상단 뒤로가기, 미리보기 수정과 입력 유지, 기존 등록/수정/상세 회귀를 확인했다.
- 신규 상품 작성 ViewModel 계측 테스트 3개 통과. 초기화가 복원 상태를 덮지 않는지, 가격·수량 검증, 미입력 태그의 미리보기 반영과 중복 저장 방지를 확인했다.
- 중간 계측 실행은 다른 실행과 겹쳐 프로세스가 종료되어 성공으로 간주하지 않았다. 최종 단독 실행은 실패·건너뜀 없이 통과했다.
- 이번 변경에서 Release, Consumer 빌드·계측과 전체 앱 계측은 미실행이다.

### 화면 파일·상태 컨벤션 수정 검증 (2026-09-14)

- 목록과 작성 단계의 Destination·Route·Screen을 각 Screen 파일에 함께 배치했다.
- 부모 Route의 UI State 중복 저장을 제거했다. 각 ViewModel은 기본값이 있는 non-null State를 제공하고, 단계 간에는 `ProductDraftModel`을 전달한다. 뒤로 이동한 목적지는 Navigation의 상태 저장·복원으로 입력을 유지한다.
- Owner Debug 빌드, 앱·상품 모듈 ktlint, Owner Debug lint, 상품 단위 테스트 10개 통과.
- 상품 작성 ViewModel 계측 테스트 5개 통과. 이전 단계 변경 시 가격의 잘못된 입력과 픽업시간·태그 입력이 유지되는지 확인했다.
- 이번 변경의 화면 이동·복원 계측 테스트는 기기 잠금으로 미실행. 위에 기록된 이전 구현의 화면 테스트 통과 결과를 이번 변경의 검증으로 대체하지 않는다.

### 사용자 플로우 실기기 검증 (2026-09-14)

Samsung SM-F711N / Android 15에서 Owner Debug로 실행했다.

- 첫 실행: 화면 테스트 12개 중 2개 실패. 이전 화면을 기준으로 `inclusive = false, saveState = true`로 pop하면 저장 상태가 가격 목적지에 잘못 연결되어 픽업 화면만 복원됐다. 현재 화면을 `inclusive = true`로 저장하도록 수정했다.
- 수정 후 화면 테스트 12개 모두 통과: 홈↔점포 관리/탭·필터 유지, 홈에서 작성 시작·취소 확인, 필수 입력 차단, 수정→미리보기→첫 단계 재수정→저장, 작성 내용·현재 단계 복원, 상단/시스템 뒤로 가기, 수량 0·부족 확인과 취소, 목록·찜 상태별 액션/빈 결과.
- 작성 ViewModel 6개와 상세 ViewModel 7개, 총 13개 계측 테스트 통과. 신규 상품은 단계별 입력→ID 복원 유지→최종 상품 생성까지 검증했다.
- Owner Debug 빌드·lint, 앱/상품 모듈 ktlint, 상품 단위 테스트 10개 통과. 점포 관리 3종과 수량·찜 취소 시트 3종의 캡처를 확인했다.
- 범위 한계: 신규 등록의 시스템 사진 선택기 조작부터 저장까지 전체 UI 경로, 실제 서버 저장·찜 취소·픽업 완료 응답, OS 프로세스 강제 종료 후 복원은 이번에 실행하지 않았다. 화면 테스트의 복원은 `StateRestorationTester`, 상태 테스트의 복원은 `SavedStateHandle` 재사용 방식이다. 찜 액션 검증은 콜백 전달까지이며 서버 성공을 의미하지 않는다.


### 작성 화면 Figma·Action 컨벤션 점검 (2026-09-14)

- `1155:11416`, `1155:13956`, `1155:14029`의 디자인 컨텍스트와 실제 캡처를 대조했다. 제목 여백, 섹션 간격, 필수 표시, 사진 버튼/한도, 가격 원화 아이콘과 초기 할인 안내, 픽업시간 문구, 태그 칩을 반영했다. 별도 태그 추가 버튼을 제거하고 키보드 완료 및 등록 시 입력 중인 태그를 반영한다.
- 기존 Mangro 컴포넌트를 사용한다. 공통 입력에 선택적 선행 아이콘, 버튼에 선택적 shape를 추가했다. 기본 파라미터는 유지하며 작성 화면에서만 지정한다.
- `ProductEditorDesignTest`를 `feature/owner/product/src/androidTest/.../screen/editor`에 추가했다. 지정한 세 상태의 UI와 캡처, 태그 삭제를 검증한다. OS 시스템바와 기기 크기 차이는 Figma 화면 이미지의 픽셀 단위 동일성으로 간주하지 않는다.
- `Action.md`에 맞춰 basic/price/pickup/list의 Action을 `NextClicked`, `NavigationBackClicked`, `PhotoRemoveClicked`, `RegisterClicked`, `TagSubmitted`, `TabSelected` 등 사용자 행동의 과거형으로 통일했다. Screen·ViewModel·테스트 사용처도 함께 변경했다.
- State와 model의 컬렉션은 최종 요청에 따라 `List`를 유지한다. PersistentList·Parcelize는 도입하지 않았다.
- 최종 검증: 디자인 3개 + 작성/상세 ViewModel 13개 계측 테스트 통과, 앱 사용자 플로우 12개 통과, 단위 테스트 10개 통과. Owner/Consumer Debug 빌드·lint와 앱/상품/디자인시스템 ktlint 통과. 마지막 시간 문자열 분리 후 Owner 빌드·lint와 계측 테스트를 다시 통과했다.

- 픽업 마감: 드롭다운 기본 선택값은 매장 운영 종료 시간이다. 사용자는 현재 시각 이후이면서 운영시간 범위 안의 시간으로 변경할 수 있다. 선택값은 화면 복원과 미리보기에 유지되며, 제출 시 매장을 재조회해 운영시간 범위를 검증한 후 선택한 시간에 서울 기준 오늘 날짜를 합쳐 전송한다.


## 찜 관리 통합 (2026-09-14)

별도 `:feature:owner:pickup` 대신 이 모듈의 점포 관리 → 찜 현황 탭에서 목록, 상세, 완료, 재고 부족 취소, 확인 시트 및 빈 상태를 제공한다. 상세와 취소는 화면별 State/Action/Event/ViewModel/Route/Screen으로 나누며 목록과 로컬 찜 저장소를 공유한다.

- [컨벤션 안내](../../../docs/conventions/README.md): 다른 상품·홈 feature와 같은 형식, 화면 계약, ktlint 적용
- [통합 결정과 API 미연결 범위](../../../docs/adr/0001-owner-product-pickup.md)
- [찜 관리 아이콘 리소스](#찜-관리-아이콘-리소스)
- Debug만 예시 데이터와 로컬 완료·취소 동작을 제공한다. Release는 빈 목록이며 실제 재고 API와 메시지 전송은 미연결이다.


## 찜 관리 아이콘 리소스

Figma 파일 `hqglQXERCwjFx1W4amjPHI`의 SVG를 변환한 Android VectorDrawable을 `src/main/res/drawable`에서 관리한다. 별도 `design` 폴더에 SVG를 중복 보관하지 않으며, 화면은 `painterResource(R.drawable.pickup_*)`로 참조한다.

| Android 리소스 | 용도 | Figma 노드 |
|---|---|---|
| [pickup_empty.xml](src/main/res/drawable/pickup_empty.xml) | 취소 대상 없음 | 1318:26762 |
| [pickup_product.xml](src/main/res/drawable/pickup_product.xml) | 취소 상품 그룹 | 1318:26538 |
| [pickup_shortage.xml](src/main/res/drawable/pickup_shortage.xml) | 재고 부족 안내 | 1318:26538 |
| [pickup_notifications_off.xml](src/main/res/drawable/pickup_notifications_off.xml) | 알림 미동의 | 1318:26538 |

SVG의 path·색상·stroke를 유지하고 rect는 path, 회전은 group으로 변환했다. 빈 상태 아이콘은 48dp 프레임 안에 배치한다.

## 상품 등록 API 연결 (2026-09-17)

최종 저장 시 매장 승인 상태를 재확인한 뒤 `:data:owner:product`가 첫 사진만 multipart `file`로 업로드하고 반환 URL로 상품을 등록한다. 사진 배열이 비어 있으면 업로드를 생략하고 필수 문자열 `photoUrl`은 빈 문자열로 전송한다. 사진 선택 UI와 로컬 미리보기는 최대 1장으로 제한하며, 최종 저장된 모델에는 서버가 반환한 사진 한 장만 반영한다.

태그 ID 매핑은 보류하여 등록 요청에 빈 배열을 보내며 카테고리는 제출 직전 매장 조회 응답의 단일 카테고리를 사용한다. `PREPARED_FOOD`는 상품 API의 `SIDE_DISH`로 매핑하고 미리보기에는 조리식품으로 표시한다. 누락·복수·미지원 카테고리는 임의로 ETC를 전송하지 않고 등록을 실패 처리한다. 픽업 시각은 서울 기준 오늘 날짜와 합쳐 오프셋이 포함된 시각으로 보내고 지난 시각은 등록하지 않는다. 중복 저장을 차단하고 실패 시 입력을 유지해 다시 저장할 수 있다. 성공한 서버 ID·상품 정보만 호출부에 전달하며 홈 복귀 시 기존 ON_RESUME 조회로 서버 목록을 갱신한다. 미리보기 API·재고 수정·찜 취소 연결은 이번 범위에 포함하지 않는다.
