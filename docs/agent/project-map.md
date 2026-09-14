# 프로젝트 지도

이 문서는 현재 체크아웃에서 확인할 수 있는 Mangro 프로젝트 구조만 설명한다. 예정된 구조는 현재 구현으로 기록하지 않는다.

## 현재 구성

| 경로 | 유형 | 책임 |
|---|---|---|
| `:app` | Android application 모듈 | 애플리케이션 패키징과 앱 진입점 |
| `:core:network` | Android library 모듈 | Retrofit/Json 구성, Bearer 헤더와 HTTP 오류 정보 처리 |
| `:remote:auth` | Android library 모듈 | 공통 인증 8 API와 공개 약관 2 API 생성 |
| `:remote:consumer` | Android library 모듈 | Consumer 21 API 생성 |
| `:remote:owner` | Android library 모듈 | Owner 12 API 생성 |
| `:core:designsystem` | Android library 모듈 | Compose 테마와 공통 UI 컴포넌트 |
| `:core:utils` | Android library 모듈 | 네트워크 상태 관측 등 공통 Android 유틸리티용 모듈 골격 |
| `:feature:owner:setting` | Android library 모듈 | 점주 상점 정보와 약관 목록·WebView |
| `:feature:owner:product` | Android library 모듈 | 점포 관리의 상품 등록·상세 및 찜 목록·상세·취소 |
| `:feature:owner:home` | Android library 모듈 | 점주 홈 UI, 운영 현황과 외부 화면 진입 액션 |
| `:feature:owner:onboarding` | Android library 모듈 | 점주 최초 매장 등록 2단계 UI와 외부 검색·신청 연결 계약 |
| `:feature:splash` | Android library 모듈 | owner/consumer 스플래시 화면과 시작 시 로그인 분기 |
| `:data:owner:auth` | Android library | Owner 카카오 인증, 가입, 암호화 세션 저장 및 갱신 |
| `:data:owner:terms` | Android library 모듈 | Owner 약관 목록·본문 Repository와 검증된 앱 모델 |
| `:feature:auth` | Android library 모듈 | owner/consumer 로그인·약관 동의 화면과 내비게이션 |
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
  - Owner는 `OwnerNavHost`에서 스플래시·로그인·약관 동의·홈·상점 등록·점포 관리·찜·설정 Navigation을 조립한다. 온보딩 모듈의 실제 주소 검색과 상위 그래프 복귀는 연결되어 있으며 등록 API는 미연결이다.
  - 동일한 패키지와 함수 시그니처를 사용하며, 빌드 대상 Flavor의 구현만 포함한다.
- Consumer `MainScreen`: `app/src/consumer`. `AppNavGraph`는 Consumer 소스셋에 있고 스플래시 시작 테마는 공통으로 사용한다.
  - Consumer는 스플래시에서 로그인 화면으로 진입한다. Owner 신규 회원은 로그인 → 약관 동의·가입 → 상점 등록, 기존 회원과 복원된 세션은 홈으로 이동한다. 상점 조회를 통한 최종 분기는 다음 단계에서 연결한다.
  - Kotlin 함수는 소스셋 사이에서 덮어쓰지 않으므로 `MainScreen`은 각 빌드에서 하나만 포함한다.
- Flavor별 로그인 UI: `feature/auth/src/owner`, `feature/auth/src/consumer`의 `LoginScreen`
- Flavor별 스플래시 UI: `feature/splash/src/owner`, `feature/splash/src/consumer`의 `SplashScreen`
  - 두 Feature 모두 `role` 차원의 owner/consumer Flavor를 선언하며, 화면은 각 Flavor에서 독립적으로 수정한다.
  - 로그인 Route·ViewModel·상태·이벤트와 스플래시 Route·ViewModel은 Flavor별로 분리한다. Consumer의 기존 동작은 유지한다.
- Flavor별 테마와 앱 리소스: `app/src/consumer`, `app/src/owner`
- 로컬 단위 테스트: `app/src/test`
- Android 계측 테스트: `app/src/androidTest`
- application convention plugin: `MangroApplicationPlugin.kt`
- library convention plugin: `MangroLibraryPlugin.kt`
- 공통 SDK 및 버전 값: `Constants.kt`

## 현재 확인된 제약

- `:app`은 `:core:designsystem`, `:core:utils`에 의존하며, Owner에 한해 `:feature:owner:onboarding`, `:feature:owner:home`, `:feature:owner:product`, `:feature:owner:setting`에도 의존한다. `data:owner:terms`가 Owner 인증 Feature에서 약관 조회를 담당한다.
- `:app`은 Auth remote를 공통으로, Consumer/Owner remote를 Flavor별로 의존한다. Consumer는 `:feature:splash`, `:feature:auth`에도 의존한다. API 화면 연동은 후속이다.
- Remote 생성·검증 방법은 [Remote README](../../remote/README.md)를 따른다. OpenAPI Generator 7.24.0 및 Python 3을 사용하며, 생성 코드는 Git에 포함하지 않는다.
- `:app`은 두 Flavor 모두 `:core:designsystem`, `:core:utils`, `:feature:splash`, `:feature:auth`에 의존한다. 네이버 지도 의존성과 API 키 Manifest 설정은 consumer에만 적용한다.
- Owner는 `data:owner:auth`로 카카오 로그인·가입·세션 복원과 갱신을 연결한다. 신규 가입은 약관 동의 성공 후 상점 등록으로, 기존 회원 및 복원된 세션은 홈으로 이동한다. 상점 보유 여부 조회에 따른 분기는 3단계에서 추가한다. Consumer 인증은 기존 미연결 상태다.
- Owner SDK 설정은 [Owner 인증 ADR](../adr/0003-owner-auth-data.md)을 따른다. 현재 가입 동의 매핑은 기존 OpenAPI의 boolean 필드를 사용하며 최신 서버 계약 확인이 필요하다.
- `:core:utils`의 `NetworkConnectivityManager`는 기본 네트워크 콜백으로 연결 상태를 관측한다. `MangroApplication`에서 필드 주입받아 앱 시작 시 인스턴스를 생성한다.
- 앱의 실제 기능 소스는 아직 초기 상태이며 예제 테스트가 남아 있다.
- Compose convention plugin은 `:app`, `:core:designsystem`, `:feature:owner:home` 등 Compose UI 모듈에 적용되어 있다. Hilt 및 KSP 플러그인은 `:app`, `:core:utils`, `:core:network`, `:remote:auth`, `:remote:consumer`, `:remote:owner`, `:feature:owner:home` 등에 적용되어 있으며, 앱의 Hilt 진입점은 `MangroApplication`이다.
- 점주 홈은 최초 안내·빈 상태·운영 현황을 표시한다. 앱의 홈 주문 정보는 찜과 같은 `OwnerPickupStore`의 ID·상태·재고로 구성한다. Debug는 예시 주문, Release는 빈 상태를 사용한다. Preview는 Debug 전용이며 실제 데이터 조회는 미연결이며 상품 목록·상세·등록 및 찜 목적지는 Owner Navigation으로 연결한다.
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

## Owner 전체 화면 이동

- `OwnerNavHost`: 스플래시 → 로그인 → 약관 동의 → 상점 정보 등록 → 완료 시 홈. 로그인에서 개인정보처리방침으로 이동한 뒤 로그인으로 복귀한다. 등록 완료 후 홈 진입 시 스플래시·로그인·약관·온보딩 기록을 제거한다. 등록 화면에서 뒤로가면 약관 동의로, 약관에서 뒤로가면 로그인으로 복귀한다.
- 홈 상단 상점 등록 → 기본 정보 → 주소 검색/운영 정보. 주소 검색 결과는 기본 정보의 SavedStateHandle로 돌려주며, 등록 완료 이벤트는 홈으로 복귀한다.
- 홈·점포 관리·설정 하단 탭은 이전 선택을 복원한다. 홈의 상품/찜 바로가기는 요청한 탭·필터를 우선한다.
- 홈 방문 예정/새 찜 → 전체 찜, 픽업 완료 → 완료 필터, 수령 확인 → 만료 필터, 취소 안내 → 전체 재고 부족 취소.
- 홈 방문자와 찜 목록은 같은 주문 ID로 상세에 진입한다. 상품 상세의 재고 부족 취소는 해당 상품 ID만 전달한다.
- 상품 등록 3단계와 미리보기는 product 내부 Navigation이 소유한다. 설정의 이용약관·개인정보처리방침은 같은 WebView 목적지를 사용한다.
- 로그인은 기존 임시 성공 이벤트이며 실제 인증·매장 등록 여부 조회는 미연결이다. 등록 신청은 기존 오류 처리를 유지한다. 약관 동의는 필수 항목을 모두 선택해야 온보딩으로 진행하며 마케팅 동의는 선택 사항이다. Owner 약관 목록·본문은 공개 Terms API에 연결되어 있다. 동의 내역의 서버 저장은 미연결이고, 설정의 약관 WebView는 확정 URL 없이 준비 중 화면을 표시한다. 알림 목록 화면은 없어 준비 중 안내를 표시한다.
