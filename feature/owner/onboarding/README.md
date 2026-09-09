# 점주 최초 매장 등록 (#63)

Figma [Step1](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-17042), [Step2](https://www.figma.com/design/hqglQXERCwjFx1W4amjPHI?node-id=1172-20742)의 Compose UI.

## 호출 계약

`OwnerOnboardingScreen`은 `categories`, `onSearchAddress`, `onSubmit`, `onComplete`, `onBack`을 받는다. 기본 상태는 `rememberOwnerOnboardingState()`로 생성하며 호스트에서 같은 상태를 주입할 수도 있다.

- `categories`: 서버/기획에서 정한 ID와 표시명. Debug의 `debug-*` ID는 실제 서버 코드가 아니다.
- `onSearchAddress(query)`: 검색 결과 `List<StoreAddress>` 반환. 결과 없음은 빈 목록, 실패는 예외. 검색 취소 시 작업을 취소하고 기존 주소를 유지한다. 호스트는 네트워크 작업을 main thread에서 차단하지 않아야 한다.
- `onSubmit(registration)`: 실제 접수에 성공했을 때만 정상 반환한다. 예외는 재시도 안내, 코루틴 취소는 상위로 전파한다. 이름/상세 주소 앞뒤 공백을 제거하고 연락처는 숫자만 전달한다.
- `onComplete`: 성공 안내의 확인 버튼에서 앱 경로 이동 요청. 승인 완료를 의미하지 않는다. 실제 심사 상태별 분기는 #56에서 연결한다.
- `onBack`: Step1에서만 외부로 전달. Step2에서는 입력을 보존하며 Step1로 돌아간다.

## 현재 UI 정책과 제한

- 상호, 종류, 검색된 5자리 우편번호·주소는 필수. 상세 주소는 선택 입력이다.
- 연락처는 0으로 시작하는 숫자 9~11자리. 최종 서비스 검증 규칙 확정 시 수정해야 한다.
- 시간은 자정부터의 분으로 저장하고 오전/오후 12시간제로 표시한다. 기본값 없이 선택을 요구하며, 종료가 시작보다 늦은 같은 날 영업만 지원한다. 심야·24시간·요일별 시간은 미지원이다.
- 요일은 일요일 0~토요일 6의 집합이며 초기 미선택, 최소 하루가 필수다.
- 기본 Android 시간 선택기와 공통 AlertDialog를 사용한다. 주소 검색/오류/접수 안내는 제공되지 않은 추가 시안의 대체 UI다.
- 입력과 현재 단계는 saveable 상태로 복원한다. 앱 재실행용 영구 임시저장 기능은 없다.
- 화면이 사라지면 진행 중 검색/제출 코루틴은 취소된다. 실제 API 연결 시 서버의 접수 여부 조회/멱등성 계약을 연결해야 하며, UI의 중복 클릭 방지만으로 서버 중복 접수 방지가 보장되지는 않는다.

## 앱 조립

`:app`은 `ownerImplementation`으로 이 모듈에 의존한다. `ownerDebug/MainScreen`만 샘플 주소 검색/신청으로 UI를 실행한다. 진입 시 샘플 안내를 표시하며 실제 서비스로 정보가 전송되지 않는다. 검색어 `망원`으로 결과 두 개를 확인할 수 있다. 완료 확인은 Debug Activity를 종료한다.

`ownerRelease/MainScreen`은 기존 Owner 진입 화면을 유지한다. Consumer 진입 코드는 변경하지 않는다. 기존 `feat/owner-home`과 합칠 때 두 Feature 의존성/등록을 유지하고 MainScreen 선택은 #56에서 조립해야 한다.

## 리소스

전화/주의 아이콘은 Figma 원본 SVG를 Android Svg2Vector로 변환했다. 만료되는 asset URL을 런타임에서 사용하지 않는다. 나머지 입력 필드·드롭다운·버튼·상단 바·색상/폰트는 기존 디자인 시스템을 재사용한다.
