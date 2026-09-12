# 프로젝트 지도

이 문서는 현재 체크아웃에서 확인할 수 있는 Mangro 프로젝트 구조만 설명한다. 예정된 구조는 현재 구현으로 기록하지 않는다.

## 현재 구성

| 경로 | 유형 | 책임 |
|---|---|---|
| `:app` | Android application 모듈 | 애플리케이션 패키징과 앱 진입점 |
| `:core:network` | Android library 모듈 | 네트워크 계층용 모듈 골격 |
| `:core:designsystem` | Android library 모듈 | Compose 테마와 공통 UI 컴포넌트 |
| `:core:utils` | Android library 모듈 | 네트워크 상태 관측 등 공통 Android 유틸리티용 모듈 골격 |
| `:feature:owner:home` | Android library 모듈 | 점주 홈 UI, 운영 현황과 외부 화면 진입 액션 |
| `:feature:owner:pickup` | Android library 모듈 | 점주 찜 현황·상세·재고 부족 취소 UI와 상태/배정 로직 |
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
  - 동일한 패키지와 함수 시그니처를 사용하며, 빌드 대상 Flavor의 구현만 포함한다.
- 로컬 단위 테스트: `app/src/test`
- Android 계측 테스트: `app/src/androidTest`
- application convention plugin: `MangroApplicationPlugin.kt`
- library convention plugin: `MangroLibraryPlugin.kt`
- 공통 SDK 및 버전 값: `Constants.kt`

## 현재 확인된 제약

- `:app`은 `:core:designsystem`, `:core:utils`에 의존하며, Owner Flavor는 `:feature:owner:pickup`에 의존한다. Data/API 구현은 아직 없다.
- `:app`은 `:core:designsystem`, `:core:utils`에 의존하며, Owner Flavor만 `:feature:owner:home`에 의존한다. Data 모듈은 아직 등록되지 않았다.
- 점주 홈은 최초 안내·빈 상태·운영 현황을 표시한다. Owner Debug 진입점은 Figma 샘플 데이터를 사용하며 Release는 최초 안내를 표시한다. 실제 데이터 조회와 목적지 연결은 아직 없다.
- `:core:utils`의 `NetworkConnectivityManager`는 기본 네트워크 콜백으로 연결 상태를 관측한다. `MangroApplication`에서 필드 주입받아 앱 시작 시 인스턴스를 생성한다.
- 앱의 실제 기능 소스는 아직 초기 상태이며 예제 테스트가 남아 있다.
- Compose convention plugin은 `:app`, `:core:designsystem`, `:feature:owner:home`에 적용되어 있다. Hilt 및 KSP 플러그인은 `:app`, `:core:network`, `:core:utils`, `:feature:owner:home`에 적용되어 있으며, 앱의 Hilt 진입점은 `MangroApplication`이다.
- 루트 `ktlintCheck`는 subproject를 집계하지만 included build인 `build-logic` 소스는 직접 검사하지 않는다.
- `.github/workflows` 기반 CI는 아직 없다.

## 갱신 조건

다음 변경에서는 이 문서를 함께 갱신한다.

- 모듈 추가 또는 제거
- convention plugin 책임 변경
- SDK, Java, Build Type, Product Flavor 변경
- 주요 테스트 진입점 변경
- 프로젝트 최상위 디렉터리의 책임 변경
