# 에이전트 문서 안내

이 디렉터리는 사람, Codex, Claude Code가 같은 기준으로 Mangro 저장소를 변경하고 검증하기 위한 운영 문서를 제공한다.

## 사실의 원본

| 확인 대상 | 원본 |
|---|---|
| 등록 모듈과 저장소 정책 | `settings.gradle.kts` |
| SDK, Java, 앱 버전 | `build-logic/.../Constants.kt` |
| convention plugin | `build-logic/src/main/kotlin/**` |
| 라이브러리 및 플러그인 버전 | `gradle/libs.versions.toml` |
| 코드 스타일 | `.editorconfig`, ktlint 설정 |
| 검증 명령 | `scripts/agent/verify.sh` |
| 커밋 메시지 정책 | `.githooks/commit-msg` |

문서와 현재 체크아웃이 충돌하면 코드와 빌드 설정을 우선하고, 같은 변경에서 문서도 갱신한다.

## 작업별 문서 경로

| 변경 대상 | 먼저 읽을 문서 | 최소 검증 |
|---|---|---|
| `settings.gradle.kts`, `build-logic/**`, `gradle/**` | `project-map.md`, `architecture.md`, `validation.md` | 전체 |
| `app/src/main/**` | `project-map.md`, `architecture.md`, `validation.md` | 전체 |
| `app/src/test/**`, `app/src/androidTest/**` | `validation.md` | 빠른 또는 기기 |
| `AGENTS.md`, `CLAUDE.md`, `docs/agent/**`, `scripts/agent/**` | 이 문서, `validation.md` | 전체 |
| `.githooks/**`, `.github/**`, `.gitmessage` | `delivery.md`, `validation.md` | 관련 스크립트 검사 |
| 모듈 경계 또는 장기 기술 결정 | `architecture.md`, `../adr/README.md` | 전체 |

## 문서 구성

- [프로젝트 지도](project-map.md): 현재 구현된 모듈, 빌드 구조, 알려진 공백
- [아키텍처 규칙](architecture.md): 변경 시 유지해야 하는 저장소 수준 불변조건
- [검증 정책](validation.md): 변경 유형별 검증 수준과 결과 보고 방식
- [전달 규칙](delivery.md): 커밋, PR, 작업 완료 보고 기준
- [ADR 안내](../adr/README.md): 중요한 기술 결정을 기록하는 기준

## 검토 중인 컨벤션

- [Android 컨벤션 초안](../conventions/README.md)

위 문서는 애플리케이션 아키텍처와 Compose, MVVM의 State·Action·Event, ViewModel 구현 방향을 검토하기 위한 초안이다. 현재 체크아웃에 관련 구현이 아직 없으므로 확정된 프로젝트 규칙으로 간주하지 않으며, 실제 코드와 팀 합의를 반영하면서 보완한다.

## 확정 규칙에서 제외한 범위

- Compose 작성 규칙
- MVVM State·Action·Event 규칙
- ViewModel 공통 기반 클래스
- Overlay 및 에러 표시 정책
- 아직 구현되지 않은 Feature, Core, Data 모듈 규칙

위 항목은 초안 문서가 존재하더라도 실제 구현과 팀 합의가 생기기 전까지 하네스의 확정 규칙으로 적용하지 않는다.

## 작업 완료 보고

작업 완료 시 다음을 구분한다.

1. 변경한 파일과 이유
2. 실행한 명령과 성공 또는 실패 결과
3. 실행하지 못한 검증과 이유
4. 남은 위험 또는 후속 작업
