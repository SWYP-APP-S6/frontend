# Mangro 프로젝트 작업 지침

이 문서는 Mangro 저장소에서 작업하는 코딩 에이전트가 공통으로 따라야 하는 루트 지침이다.

## 작업 시작

- 변경 전에 `git status --short --branch`로 브랜치와 기존 변경을 확인한다.
- [에이전트 문서 안내](docs/agent/README.md)를 읽고 변경 범위에 필요한 문서만 추가로 확인한다.
- 현재 체크아웃의 코드, Gradle 설정, 테스트를 사실의 원본으로 사용한다.
- 저장소 밖의 문서는 명시적으로 채택되지 않았다면 프로젝트 규칙으로 적용하지 않는다.
- 기존 사용자 변경과 관련 없는 파일은 수정하거나 되돌리지 않는다.

## 현재 프로젝트 기준

- 현재 등록된 Android 모듈은 `:app`이다.
- 공통 Android 설정은 `build-logic`의 convention plugin이 소유한다.
- 의존성 및 플러그인 버전은 `gradle/libs.versions.toml`에서 관리한다.
- 아직 저장소에 구현되지 않은 Compose, MVI, 모듈 구조를 현재 규칙으로 가정하지 않는다.

자세한 현재 구조는 [프로젝트 지도](docs/agent/project-map.md)를 따른다.

## 변경 절차

1. 관련 구현, 빌드 설정, 테스트를 함께 확인한다.
2. 요청 범위 안에서 가장 작은 변경을 만든다.
3. 동작이 변경되면 관련 테스트를 추가하거나 갱신한다.
4. [검증 정책](docs/agent/validation.md)에 따라 검증한다.
5. 변경 파일, 실행한 검증, 실행하지 못한 검증, 남은 위험을 한국어로 보고한다.

## 표준 명령

- 환경 진단: `./scripts/agent/doctor.sh`
- 빠른 검증: `./scripts/agent/verify.sh --quick`
- 전체 검증: `./scripts/agent/verify.sh --full`
- 기기 검증: `./scripts/agent/verify.sh --device`
- Git hook 설치: `./scripts/agent/install-git-hooks.sh`

검증 명령의 실제 구성은 `scripts/agent/verify.sh`를 원본으로 삼는다.

## 안전과 전달

- `local.properties`, 서명 파일, 토큰, 비밀 값의 내용을 출력하거나 커밋하지 않는다.
- 사용자의 명시적 요청 없이 커밋, 푸시, 브랜치 변경을 수행하지 않는다.
- 실행하지 않은 빌드나 테스트를 성공했다고 보고하지 않는다.
- 기기가 없어 계측 테스트를 실행하지 못했다면 `미실행`으로 명시한다.
- 커밋과 PR 규칙은 [전달 규칙](docs/agent/delivery.md)을 따른다.

