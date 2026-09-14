# Owner Setting

점주 설정의 상점 정보와 약관 WebView를 소유하는 `:feature:owner:setting` 모듈이다.

- 상점 이름·전화번호는 읽기 전용이다. Debug는 예시 값, Release는 미등록 상태이며 상점 조회 API는 아직 연결하지 않았다.
- 서비스 이용약관·개인정보 처리방침을 선택하면 앱 내 WebView로 이동한다. URL이 없어 현재 `about:blank`와 준비 중 안내를 표시한다.
- 확정 URL은 `OwnerPolicyViewModel`에서 `OwnerPolicyUiState.url`로 전달한다. 실제 웹 문서 로딩은 URL 확정 후 검증한다.
- 두 화면은 각각 State·Action·Event·ViewModel을 사용하며, `OwnerNavHost`가 홈·점포 관리·설정을 연결한다.
- 공통 상단바·Owner 하단바, 테마 색상·타이포그래피와 기존 VectorDrawable을 재사용한다.

## 검증 진입점

- `:feature:owner:setting:ktlintCheck`, `:feature:owner:setting:compileReleaseKotlin`
- `app/src/androidTestOwner/.../OwnerSettingFlowTest.kt`: 실제 Owner 앱에서 탭 이동, 약관 WebView 생성, 화면 재생성과 뒤로 가기.
- `app/src/androidTestOwner/.../OwnerSettingLayoutTest.kt`: 미등록 정보, 긴 상점 이름과 1.5배 글씨에서 약관 목록 접근.
- 설정 UI 테스트만 실행: `./gradlew :app:connectedOwnerDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.swyp.mangro.OwnerSettingFlowTest,com.swyp.mangro.OwnerSettingLayoutTest --console=plain`
