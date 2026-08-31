# 프로젝트 지도

이 문서는 현재 체크아웃에서 확인할 수 있는 Mangro 프로젝트 구조만 설명한다. 예정된 구조는 현재 구현으로 기록하지 않는다.

## 현재 구성

| 경로 | 유형 | 책임 |
|---|---|---|
| `:app` | Android application 모듈 | 애플리케이션 패키징과 앱 진입점 |
| `build-logic` | Gradle included build | Android application/library 공통 설정 |
| `gradle/libs.versions.toml` | Version Catalog | 플러그인과 외부 라이브러리 버전 |
| `.githooks` | Git hooks | 커밋 메시지와 커밋 전 ktlint 검사 |
| `.github` | GitHub 설정 | PR 템플릿과 향후 CI 구성 |
| `docs/reports` | 작업 보고서 | 실행 결과와 분석 보고서 보관 |

등록된 Gradle 모듈의 최종 원본은 `settings.gradle.kts`이다.

## 현재 빌드 기준

| 항목 | 현재 값 | 원본 |
|---|---:|---|
| Gradle | 9.4.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.2.1 | `gradle/libs.versions.toml` |
| Java | 17 | `build-logic/.../Constants.kt` |
| minSdk | 28 | `build-logic/.../Constants.kt` |
| targetSdk | 36 | `build-logic/.../Constants.kt` |
| compileSdk | 37 | `build-logic/.../Constants.kt` |

값을 변경할 때는 이 문서만 수정하지 말고 원본 설정을 먼저 변경한다.

## 주요 소스 위치

- 앱 Manifest와 리소스: `app/src/main`
- 로컬 단위 테스트: `app/src/test`
- Android 계측 테스트: `app/src/androidTest`
- application convention plugin: `MangroApplicationPlugin.kt`
- library convention plugin: `MangroLibraryPlugin.kt`
- 공통 SDK 및 버전 값: `Constants.kt`

## 현재 확인된 제약

- `:app` 외 Feature, Core, Data 모듈은 아직 등록되지 않았다.
- 앱의 실제 기능 소스는 아직 초기 상태이며 예제 테스트가 남아 있다.
- Compose와 DI 관련 플러그인 및 MVVM Presentation 구현은 아직 적용되지 않았다.
- 루트 `ktlintCheck`는 subproject를 집계하지만 included build인 `build-logic` 소스는 직접 검사하지 않는다.
- `.github/workflows` 기반 CI는 아직 없다.

## 갱신 조건

다음 변경에서는 이 문서를 함께 갱신한다.

- 모듈 추가 또는 제거
- convention plugin 책임 변경
- SDK, Java, Build Type, Product Flavor 변경
- 주요 테스트 진입점 변경
- 프로젝트 최상위 디렉터리의 책임 변경
