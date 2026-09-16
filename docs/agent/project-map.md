# 프로젝트 지도

이 문서는 현재 체크아웃에서 확인할 수 있는 Mangro 프로젝트 구조만 설명한다. 예정된 구조는 현재 구현으로 기록하지 않는다.

## 현재 구성

| 경로 | 유형 | 책임 |
|---|---|---|
| `:app` | Android application 모듈 | 애플리케이션 패키징과 앱 진입점 |
| `:core:network` | Android library 모듈 | Retrofit/Json 구성, Bearer 헤더와 HTTP 오류 정보 처리 |
| `:core:crypto` | Android library 모듈 | Android Keystore 키 관리와 AES-GCM 바이트 암복호화. 저장소와 직렬화는 포함하지 않음 |
| `:core:local` | Android library 모듈 | 공통 AuthStore의 개별 토큰 암호화·DataStore 저장. Consumer/Owner Flavor 선언, UserInfoStore는 미구현 |
| `:remote:auth` | Android library 모듈 | 공통 인증 8 API 생성 |
| `:data:auth` | Android library 모듈 | 역할별 서버 로그인·가입 및 회원 토큰 저장 |
| `:remote:consumer` | Android library 모듈 | Consumer 20 API 생성 |
| `:remote:user` | Android library 모듈 | Owner·Consumer 공통 `/users/me` API 생성 |
| `:data:user` | Android library 모듈 | 사용자 프로필 조회 Flow Repository |
| `:data:owner:home` | Android library 모듈 | Owner 홈 조회·픽업 완료 Flow Repository |
| `:remote:owner` | Android library 모듈 | Owner 12 API 생성 |
| `:core:designsystem` | Android library 모듈 | Compose 테마와 공통 UI 컴포넌트 |
| `:core:utils` | Android library 모듈 | 네트워크 상태 관측 등 공통 Android 유틸리티용 모듈 골격 |
| `:feature:owner:setting` | Android library 모듈 | 점주 상점 정보와 약관 목록·WebView |
| `:feature:owner:product` | Android library 모듈 | 점포 관리의 상품 등록·상세 및 찜 목록·상세·취소 |
| `:feature:owner:home` | Android library 모듈 | 점주 홈 UI, 운영 현황과 외부 화면 진입 액션 |
| `:feature:owner:onboarding` | Android library 모듈 | 점주 최초 매장 등록 2단계 UI와 외부 검색·신청 연결 계약 |
| `:feature:splash` | Android library 모듈 | owner/consumer 스플래시 화면과 시작 시 로그인 분기 |
| `:feature:auth` | Android library 모듈 | owner/consumer 로그인 화면과 내비게이션 |
| `build-logic` | Gradle included build | Android application/library 공통 설정 |
| `gradle/libs.versions.toml` | Version Catalog | 플러그인과 외부 라이브러리 버전 |
| `.githooks` | Git hooks | 커밋 메시지와 커밋 전 ktlint 검사 |
| `.github` | GitHub 설정 | PR 템플릿과 향후 CI 구성 |
| `docs/reports` | 작업 보고서 | 실행 결과와 분석 보고서 보관 |

등록된 Gradle 모듈의 최종 원본은 `settings.gradle.kts`이다.

## 현재 빌드 기준

| 항목 |  현재 값 | 원본 |
|---|------:|---|
| Gradle | 9.4.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.2.1 | `gradle/libs.versions.toml` |
| Java |    17 | `build-logic/.../Constants.kt` |
| minSdk |    28 | `build-logic/.../Constants.kt` |
| targetSdk |    37 | `build-logic/.../Constants.kt` |
| compileSdk |    37 | `build-logic/.../Constants.kt` |

값을 변경할 때는 이 문서만 수정하지 말고 원본 설정을 먼저 변경한다.

## 주요 소스 위치

- 공통 Activity, Manifest와 리소스: `app/src/main`
- Flavor별 `MainScreen`: `app/src/consumer`, `app/src/owner`
  - Owner는 홈·점포 관리·찜·설정 Navigation을 사용한다. 온보딩 모듈의 실제 주소 검색은 연결되어 있으며 등록 API와 최상위 이동은 미연결이다.
  - 동일한 패키지와 함수 시그니처를 사용하며, 빌드 대상 Flavor의 구현만 포함한다.
- Consumer `MainScreen`과 `AppNavGraph`: `app/src/consumer`. Consumer 전용 Feature를 참조하는 그래프는 Consumer 빌드에만 포함하고, 스플래시 시작 테마는 `app/src/main`에서 공유한다.
  - 두 Flavor는 스플래시에서 저장 세션을 확인한다. 기존 회원은 홈으로 이동하며, Owner 신규 회원은 검증 → 약관 → 상점 입력 → signup → 상점 등록 순서로 진행한다. Consumer 신규 회원은 약관 → signup → 홈으로 이동한다.
  - Kotlin 함수는 소스셋 사이에서 덮어쓰지 않으므로 `MainScreen`은 각 빌드에서 하나만 포함한다.
- Flavor별 로그인 UI: `feature/auth/src/owner`, `feature/auth/src/consumer`의 `LoginScreen`
- Flavor별 스플래시 UI: `feature/splash/src/owner`, `feature/splash/src/consumer`의 `SplashScreen`
  - 두 Feature 모두 `role` 차원의 owner/consumer Flavor를 선언하며, 화면은 각 Flavor에서 독립적으로 수정한다.
  - Route, ViewModel, 상태와 이벤트는 각 Feature의 `src/main`에서 공유한다.
- Flavor별 테마와 앱 리소스: `app/src/consumer`, `app/src/owner`
- 로컬 단위 테스트: `app/src/test`
- Android 계측 테스트: `app/src/androidTest`
- application convention plugin: `MangroApplicationPlugin.kt`
- library convention plugin: `MangroLibraryPlugin.kt`
- 공통 SDK 및 버전 값: `Constants.kt`

## 현재 확인된 제약

- `:app`은 `:core:designsystem`, `:core:utils`에 의존하며, Owner에 한해 `:feature:owner:onboarding`, `:feature:owner:home`, `:feature:owner:product`, `:feature:owner:setting`에도 의존한다. Data 모듈은 아직 등록되지 않았다.
- `:app`은 Auth remote를 공통으로, Consumer/Owner remote를 Flavor별로 의존한다. Consumer는 `:feature:splash`, `:feature:auth`에도 의존한다. API 화면 연동은 후속이다.
- Remote 생성·검증 방법은 [Remote README](../../remote/README.md)를 따른다. OpenAPI Generator 7.24.0 및 Python 3을 사용하며, 생성 코드는 Git에 포함하지 않는다.
- `:app`은 두 Flavor 모두 `:core:designsystem`, `:core:utils`, `:feature:splash`, `:feature:auth`에 의존한다. 네이버 지도 의존성과 API 키 Manifest 설정은 consumer에만 적용한다.
- 카카오 SDK 인증은 두 Flavor의 로그인 버튼에 연결되어 있다. 설정과 테스트 절차는 [인증 README](../../feature/auth/README.md)를 따른다. 망그로 서버 인증·회원가입·토큰 저장은 연결되었으며 자동 로그인 및 개인정보처리방침 이동은 미연결이다.
- `:core:utils`의 `NetworkConnectivityManager`는 기본 네트워크 콜백으로 연결 상태를 관측한다. `MangroApplication`에서 필드 주입받아 앱 시작 시 인스턴스를 생성한다.
- 앱의 실제 기능 소스는 아직 초기 상태이며 예제 테스트가 남아 있다.
- Compose convention plugin은 `:app`, `:core:designsystem`, `:feature:owner:home` 등 Compose UI 모듈에 적용되어 있다. Hilt 및 KSP 플러그인은 `:app`, `:core:utils`, `:core:network`, `:remote:auth`, `:remote:consumer`, `:remote:owner`, `:feature:owner:home` 등에 적용되어 있으며, 앱의 Hilt 진입점은 `MangroApplication`이다.
- 점주 홈은 최초 안내·빈 상태·운영 현황을 표시한다. Owner Debug와 Release 모두 사용자·상점·홈 API를 조회한다. Preview와 UI 테스트만 샘플 데이터를 사용한다. 승인 완료 시 상품 등록을 허용하며 상품 등록 목적지에서도 서버 상태를 재검증한다. 상품·찜 상세 및 알림 목적지는 해당 기능의 API 연결 전까지 준비 중 안내를 표시한다.
- 루트 `ktlintCheck`는 subproject를 집계하지만 included build인 `build-logic` 소스는 직접 검사하지 않는다.
- `.github/workflows` 기반 CI는 아직 없다.

## 갱신 조건

다음 변경에서는 이 문서를 함께 갱신한다.

- 모듈 추가 또는 제거
- convention plugin 책임 변경
- SDK, Java, Build Type, Product Flavor 변경
- 주요 테스트 진입점 변경
- 프로젝트 최상위 디렉터리의 책임 변경

## 점주 상품 화면

- `:feature:owner:product`: 상품 등록 3단계, 미리보기, 목록·상세, 재고 재확인 및 찜 목록·상세·취소 UI.
- `:app` Owner 소스셋에서만 의존하며 `OwnerNavHost`가 홈과 상품 Navigation 그래프를 조립한다. 상품 저장은 호출부 콜백으로 연결하고, 찜 상세·취소는 같은 product 모듈의 Navigation 그래프로 연결한다.
- 현재 API 연결 이전의 UI 호스트 범위이며 상세 계약과 미확정 디자인 기준은 `feature/owner/product/README.md`를 참고한다.

## 점주 설정 화면

- `:feature:owner:setting`: 상점 이름·전화번호 읽기 전용 카드, 서비스 이용약관·개인정보 처리방침 목록과 앱 내 WebView.
- `OwnerNavHost`가 홈·점포 관리·설정 탭을 연결한다. 각 화면이 State·Action·Event·ViewModel을 소유한다.
- 상점 조회 API는 미연결이며 Debug에서만 예시 정보를 사용하고 Release는 미등록 상태를 표시한다.
- 약관 URL은 아직 없으므로 WebView는 `about:blank`를 열고 준비 중 안내를 표시한다. 확정 URL은 `OwnerPolicyViewModel`의 상태에 연결한다.

## 인증 Repository

- `:data:auth`: Owner·Consumer Flavor별 카카오 검증 API, 가입 API 및 `:core:local`의 AuthStore 저장을 담당한다. `:remote:auth`의 생성 API를 사용한다.
- SDK 실행은 `:feature:auth`의 KakaoLoginLauncher에 남기고 LoginViewModel은 서버 검증 결과로 홈·약관 이동을 결정한다. TermsViewModel은 Consumer의 가입을 처리하며, Owner는 동의 값을 상점 온보딩으로 전달한다. Owner 가입 API는 상점 입력 완료 후 호출한다.
- Native App Key는 앱 Flavor별 BuildConfig로 주입하며 문자열 리소스로 선언하지 않는다. 자동 로그인과 약관 문서·버전 검증은 미연결이다.

### 공통 인증 흐름

Owner·Consumer의 `:data:auth`는 Flow 기반 SDK 토큰 검증·가입·저장 세션 확인과 약관 조회를 제공한다. 검증 응답의 accessToken이 있으면 서버 토큰 쌍을 AuthStore에 저장하고, null이면 signupToken으로 가입한 뒤 가입 응답의 서버 토큰 쌍을 저장한다. `:feature:splash`에서 두 Flavor의 세션을 복원한다.

`:data:owner:store`는 Owner 상점 등록·내 상점 조회 Flow Repository, 승인 상태와 서버 요청 매핑을 소유한다. `:feature:owner:onboarding`은 이를 수집하며, Owner 신규 회원은 기본 정보 → 운영 정보 입력을 마친 뒤 signup → 상점 등록 → 접수 안내로 연결된다. Consumer에는 이 모듈을 연결하지 않는다.

- 2026-09-17 API 갱신: 공통 알림·FCM 및 회원 탈퇴는 `:remote:user`, 재고 부족 찜 취소는 `:remote:owner`에서 생성한다. 입력은 소비자 경로를 보존한 병합 명세이며 상세는 `remote/README.md`를 참고한다.

- `:data:owner:product`: 상품 등록과 첫 사진 multipart 업로드를 담당하며 생성된 `:remote:owner` 서비스를 사용한다.
