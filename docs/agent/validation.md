# 검증 정책

검증 수준을 고르는 기준은 이 문서가 담당하고, 실제 Gradle 태스크 조합은 `scripts/agent/verify.sh`가 담당한다.

## 빠른 검증

명령:

```bash
./scripts/agent/verify.sh --quick
```

대상:

- 작은 Kotlin 또는 Gradle 스크립트 변경
- 로컬 단위 테스트 변경
- 빌드 구조를 바꾸지 않는 리팩터링

현재 범위:

- staged/unstaged diff 공백 검사
- ktlint
- `:app:testDebugUnitTest`

## 전체 검증

명령:

```bash
./scripts/agent/verify.sh --full
```

대상:

- 앱 소스, 리소스, Manifest 변경
- `settings.gradle.kts`, `build-logic`, Version Catalog 변경
- 하네스 문서와 검증 스크립트 변경
- 의존성, SDK, Build Type 변경

현재 범위:

- 빠른 검증 범위
- `:app:lintDebug`
- `:app:assembleDebug`

`assembleDebug`은 included build인 `build-logic`을 컴파일하지만, 현재 루트 ktlint가 해당 소스를 직접 검사하지 않는 제약은 별도로 보고한다.

## 기기 검증

명령:

```bash
./scripts/agent/verify.sh --device
```

대상:

- UI 상호작용
- Activity, Intent, Manifest, 권한
- Android 저장소와 플랫폼 API
- 계측 테스트 변경

현재 범위:

- 전체 검증 범위
- `:app:connectedDebugAndroidTest`

연결된 기기나 에뮬레이터가 없으면 성공으로 대체하지 않고 `미실행`으로 보고한다.

## 변경 유형별 최소 수준

| 변경 유형 | 최소 검증 | 추가 증거 |
|---|---|---|
| 문서만 변경 | diff 공백 검사 | 링크와 명령 수동 확인 |
| Kotlin 로직 | 빠른 | 관련 단위 테스트 |
| 앱 리소스와 Manifest | 전체 | 필요한 경우 기기 확인 |
| Gradle, 의존성, build-logic | 전체 | 필요한 경우 dependency insight |
| UI 또는 계측 테스트 | 기기 | 화면 캡처 또는 로그 |
| Git hook과 셸 스크립트 | 전체 | `sh -n` 문법 검사 |

## 성공 기준

- 명령 종료 코드가 0이어야 한다.
- 테스트가 `NO-SOURCE` 또는 `SKIPPED`라면 검증 범위가 충분한지 확인한다.
- APK가 필요한 변경은 `assembleDebug` 완료까지 확인한다.
- 기존 실패와 이번 변경으로 발생한 실패를 구분한다.

## 보고 예시

```text
검증
- 성공: ./scripts/agent/verify.sh --full
- 미실행: ./scripts/agent/verify.sh --device
  - 이유: 연결된 Android 기기 또는 에뮬레이터 없음
- 남은 제약: build-logic 소스는 현재 루트 ktlint 대상이 아님
```

