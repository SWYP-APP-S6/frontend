# 점주 최초 매장 등록 (#63)

Figma [Step1](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-17042), [Step2](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-20742)의 Compose UI.

## 호출 계약

`OwnerOnboardingNavigation`은 `navController`, `onComplete`, `onBack`을 받는 Navigation 진입점이다. 각 입력 화면은 자신의 Hilt ViewModel만 사용한다. 각 Route에서 화면 백스택 엔트리 범위의 ViewModel을 생성한다.

- `StoreCategoryModel.options`의 `debug-*` 가게 종류 ID는 샘플이며 실제 서버 코드가 아니다.
- 주소 검색: 등록 폼을 대신하는 별도 화면으로 카카오 우편번호 WebView를 표시한다. Dialog를 사용하지 않으며, 취소 또는 시스템 뒤로가기로 등록 폼에 복귀한다. 선택한 주소 종류에 따라 도로명/지번 주소와 5자리 우편번호를 반영하며, 상세 주소는 기존 네이티브 필드에서 입력한다. 취소하면 기존 입력을 유지한다. Navigation에서 `AddressSearchWebView`를 직접 호출한다. Route의 화면 이동 콜백과 WebView의 주소 결과 전달은 각각 계측 테스트로 검증한다.
- 등록 Repository/API는 미연결이다. 등록 버튼을 누르면 입력값을 유지한 채 오류 안내를 표시하며, 실제 요청이나 샘플 성공 처리는 하지 않는다.
- `onComplete`: 성공 안내의 확인 버튼에서 앱 경로 이동 요청. 승인 완료를 의미하지 않는다. 실제 심사 상태별 분기는 #56에서 연결한다.
- `onBack`: Step1에서만 외부로 전달. Step2에서는 Step1로 돌아가며 운영 정보 ViewModel은 해제된다.

## 현재 UI 정책과 제한

- 상호, 종류, 검색된 5자리 우편번호·주소는 필수. 상세 주소는 선택 입력이다.
- 연락처는 0으로 시작하는 숫자 9~11자리. 최종 서비스 검증 규칙 확정 시 수정해야 한다.
- 시간은 자정부터의 분으로 저장하고 24시간제(`00:00`~`23:00`)로 표시한다. 기본값 없이 1시간 단위 목록에서 선택하며 종료는 시작 이상인 같은 날 시간만 지원한다. 시작 변경으로 기존 종료가 더 빨라지면 종료 선택을 초기화한다. 심야·24시간·요일별 시간은 미지원이다.
- 요일은 일요일 0~토요일 6의 집합이며 초기 미선택, 최소 하루가 필수다.
- 시간 선택은 공통 드롭다운의 목록을 사용한다. 주소 검색/오류/접수 안내는 제공되지 않은 추가 시안의 대체 UI다.
- 입력과 다이얼로그는 각 ViewModel의 `SavedStateHandle`로 복원하고 현재 화면은 Navigation 백스택으로 복원한다. 앱 재실행용 영구 임시저장 기능은 없다.
- 주소 검색을 닫으면 WebView를 해제하고, ViewModel은 해당 화면의 백스택 엔트리가 제거될 때 해제된다. 실제 API 연결 시 서버의 접수 여부 조회/멱등성 계약을 연결해야 하며, UI의 중복 클릭 방지만으로 서버 중복 접수 방지가 보장되지는 않는다.

## 앱 조립

`:app`은 `ownerImplementation`으로 이 모듈에 의존한다. `ownerDebug/MainScreen`은 실제 카카오 주소 검색과 등록 폼 UI를 실행한다. 검색어는 카카오 서비스로 전송되지만 등록 신청은 전송되지 않는다. 완료 확인은 Debug Activity를 종료한다.

`ownerRelease/MainScreen`은 기존 Owner 진입 화면을 유지한다. Consumer 진입 코드는 변경하지 않는다. 기존 `feat/owner-home`과 합칠 때 두 Feature 의존성/등록을 유지하고 MainScreen 선택은 #56에서 조립해야 한다.

## 리소스

전화/주의 아이콘은 Figma 원본 SVG를 Android Svg2Vector로 변환했다. 만료되는 asset URL을 런타임에서 사용하지 않는다. 나머지 입력 필드·드롭다운·버튼·상단 바·색상/폰트는 기존 디자인 시스템을 재사용한다.

## 카카오 우편번호 연동

- [공식 가이드](https://postcode.map.kakao.com/guide)의 `kakao.Postcode().embed()`를 사용한다. 별도 API 키나 호스팅 서버는 필요 없다.
- `assets/postcode/index.html`을 `WebViewAssetLoader`의 HTTPS 앱 자산 URL로 로드한다. 공식 CDN 스크립트와 검색 iframe은 네트워크로 불러오며 카카오 로고를 유지한다.
- `WebMessageListener`는 앱 자산 origin의 메인 프레임에만 허용한다. 외부 iframe에는 네이티브 결과 전달 객체를 노출하지 않는다. 파일/콘텐츠 접근과 외부 메인 프레임 이동은 허용하지 않는다.
- 기존 앱의 `INTERNET` 권한을 사용한다. 위치 권한은 필요하지 않다. 주소의 좌표 변환 및 실제 매장 등록 API는 별도 연결 대상이다.
- 초기 로딩 오류에는 공통 네트워크 오류 화면에서 재시도를 제공하며 상단 뒤로가기로 취소할 수 있다. 별도 로딩 타임아웃은 없다. 지원되지 않는 오래된 WebView에서도 오류 안내를 표시한다. 재시도하면 WebView를 새로 생성한다.

## 화면과 Navigation

- `OwnerOnboardingNavigation.kt`: 타입 안전 route를 사용하는 NavHost 진입점. 기본 정보 → 운영 정보, 기본 정보 → 주소 검색을 연결한다.
- `OwnerBasicInfoScreen.kt`: 상호·가게 종류·주소 입력, 다음 단계 및 주소 검색 요청.
- `OwnerOperatingInfoScreen.kt`: 연락처·영업 시간·요일 입력, 등록 신청과 실패 재시도·성공 안내.
- `components/OwnerOnboardingScaffold.kt`: 두 입력 화면의 공통 상단 바·버튼·스크롤 영역과 필드 레이아웃.
- 기본 정보 ViewModel은 다음 선택 시 검증된 `StoreBasicInfoModel`를 이벤트로 전달한다. Navigation은 기본 정보를 `Bundle`에 `java.io.Serializable` 객체로 담아 운영 화면 인자로 전달한다. 운영 ViewModel은 주입받은 자신의 `SavedStateHandle`에서 기본 정보를 읽어 초기 상태를 만든다. Navigation과 Route에서는 기본 정보를 읽거나 초기화 Action을 전달하지 않는다. 운영 입력 복원은 ViewModel 자체 `SavedStateHandle`을 사용한다. 다른 화면의 ViewModel이나 uiState를 구독하지 않는다.
- 주소 검색 결과는 이전 Navigation 엔트리의 `SavedStateHandle`에 `java.io.Serializable` 객체로 저장한 뒤 복귀한다. Navigation은 기본 화면이 RESUMED 상태일 때 결과를 Route에 전달한다. Route가 자신의 ViewModel에 반영한 뒤 결과 소비 콜백으로 값을 비운다. ViewModel은 각 Route의 기본 `hiltViewModel()`에서만 생성한다. Navigation 엔트리의 handle과 ViewModel의 handle은 별개다.
- 각 ViewModel은 해당 화면의 백스택 엔트리에 속한다. 운영 화면을 뒤로가기로 제거하면 운영 ViewModel도 해제되며, 재진입 시 최신 기본 정보로 새 운영 화면을 만든다.
- 운영 정보에서 뒤로가면 기본 정보로, 기본 정보에서 뒤로가면 호스트 `onBack`으로 전달한다. 제출 중에는 뒤로가기를 막는다.

## 화면별 State·Action·Event 바인딩

- `screen/basic`: `OwnerBasicInfoState`, `OwnerBasicInfoAction`, `OwnerBasicInfoEvent`, ViewModel, Route, Screen.
- `screen/operating`: `OwnerOperatingInfoState`, `OwnerOperatingInfoAction`, `OwnerOperatingInfoEvent`, ViewModel, Route, Screen.
- State는 기본값이 있는 `data class`와 `val`로 선언하고 `copy`로 교체한다. 문자열 입력을 사용하며 요일 컬렉션은 persistent collection으로 노출한다. 카테고리는 `StoreCategoryModel.options`의 고정 메뉴(곡류·과채류·육류·어류·견과류·기타)를 사용하고, State에는 선택값만 보관한다. `TextFieldState`, Compose mutable state, 콜백을 포함하지 않는다.
- 화면의 입력·선택한 시간·등록 로딩·오류·접수 안내는 ViewModel이 관리한다. 운영 ViewModel이 자신의 `SavedStateHandle`에 전달된 불변 `StoreBasicInfoModel` 값을 최종 등록 데이터와 결합한다.
- Navigation 입력은 Action → ViewModel의 Event → Route → NavGraph 순서로 처리한다. 주소 검색 결과만 기본 정보 Action으로 전달하며, 취소는 결과 변경 없이 백스택을 되돌린다.
- 등록 연동 시 운영 ViewModel에서 Repository를 직접 호출한다.
- `MangroTextField`에 `value`/`onValueChange` 오버로드를 추가했다. 기존 TextFieldState API와 장식 레이아웃을 공유하며, 화면은 입력을 별도 Compose 상태로 복제하지 않는다.

## UI 모델

- `model` 패키지의 `StoreBasicInfoModel`, `StoreRegistrationModel`, `StoreAddressModel`, `StoreCategoryModel`을 사용한다.
- 모델은 `java.io.Serializable`을 구현하며 Navigation과 ViewModel의 `SavedStateHandle`에 객체로 저장한다. JSON 변환은 사용하지 않는다.
- 저장 상태를 Parcel로 직렬화한 뒤 복원하는 계측 테스트로 중첩 객체, 빈 값, 최신 입력, 영업 요일 컬렉션을 검증한다.
