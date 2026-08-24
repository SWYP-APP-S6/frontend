# Git hooks

저장소의 커밋 메시지 템플릿과 Git hooks를 사용하려면 프로젝트 루트에서
다음 명령을 실행합니다.

```bash
./scripts/agent/install-git-hooks.sh
```

현재 정책은 다음과 같습니다.

- `commit-msg`: 커밋 메시지 형식을 검증하고 잘못된 메시지를 차단합니다.
- `pre-commit`: 전체 ktlint 검사를 실행하고 위반 사항이 있으면 커밋을 차단합니다.

커밋 시에는 `pre-commit`이 전체 ktlint 검사를 자동 실행합니다.
필요한 범위를 직접 검사하거나 포맷하려면 다음 명령을 사용합니다.

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew :app:ktlintCheck
./gradlew :app:ktlintFormat
```

루트 태스크는 루트 Gradle 스크립트와 앱 모듈을 검사하거나 포맷하며,
포함 빌드인 `build-logic`은 대상에서 제외합니다.
특정 모듈만 대상으로 삼으려면 모듈 경로가 포함된 태스크를 실행합니다.

저장소 공용 검증 진입점은 다음과 같습니다.

```bash
./scripts/agent/verify.sh --quick
./scripts/agent/verify.sh --full
./scripts/agent/verify.sh --device
```
