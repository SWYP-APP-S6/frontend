# 저장소 아키텍처 규칙

이 문서는 현재 구현된 빌드 구조와 저장소 경계만 다룬다. Compose, MVVM Presentation, DI와 같이 아직 구현되지 않은 설계는 강제하지 않는다.

## ARCH-001: 의존성 저장소 소유권

- 의존성 저장소는 `settings.gradle.kts`에서 선언한다.
- 개별 모듈에 `repositories {}`를 추가하지 않는다.
- 예외가 필요하면 이유와 영향 범위를 ADR로 남긴다.

현재 `RepositoriesMode.FAIL_ON_PROJECT_REPOS`가 이 규칙을 지원한다.

## ARCH-002: 버전 관리

- 외부 라이브러리와 Gradle 플러그인 버전은 `gradle/libs.versions.toml`에서 관리한다.
- 모듈 빌드 파일에 외부 의존성 버전을 직접 작성하지 않는다.
- SDK, Java, 앱 버전의 원본은 `build-logic`의 `Constants.kt`이다.

## ARCH-003: Android convention plugin

- 애플리케이션 모듈은 `mangro.android.application`을 사용한다.
- Android library 모듈은 `mangro.android.library`를 사용한다.
- convention plugin이 제공하는 SDK와 Java 설정을 모듈에서 중복 선언하지 않는다.
- 공통 설정을 추가할 때 application/library 양쪽 적용 범위를 검토한다.

## ARCH-004: 앱 모듈 경계

- `:app`은 애플리케이션 패키징, Manifest, 최종 의존성 조립을 소유한다.
- 새로운 모듈은 실제 책임 경계와 재사용 필요가 생겼을 때 추가한다.
- 예정된 모듈 이름이나 의존 방향을 코드보다 먼저 확정된 사실로 문서화하지 않는다.

## 새 Android library 모듈 체크리스트

1. `settings.gradle.kts`에 모듈을 등록한다.
2. `mangro.android.library`를 적용한다.
3. 고유한 `namespace`를 선언한다.
4. 외부 의존성은 Version Catalog alias를 사용한다.
5. 모듈 책임에 맞는 단위 테스트 진입점을 준비한다.
6. `project-map.md`를 갱신한다.
7. `./scripts/agent/verify.sh --full`을 실행한다.

## 예외와 결정 기록

- 현재 규칙을 벗어나는 변경은 PR에 이유와 검증 결과를 적는다.
- 여러 모듈과 장기간의 유지보수에 영향을 주는 결정은 [ADR 안내](../adr/README.md)를 따른다.
- 승인된 ADR과 이 문서가 충돌하면 같은 변경에서 문서 관계를 명시적으로 정리한다.
