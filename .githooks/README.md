# Git hooks

저장소의 커밋 메시지 템플릿과 Git hooks를 사용하려면 프로젝트 루트에서
다음 명령을 실행합니다.

```bash
git config --local commit.template .gitmessage
git config --local core.hooksPath .githooks
```

현재 정책은 다음과 같습니다.

- `commit-msg`: 커밋 메시지 형식을 검증하고 잘못된 메시지를 차단합니다.
- `pre-commit`: ktlint 검사를 실행하지 않고 항상 성공합니다.

ktlint는 개발자가 필요한 범위에 대해 수동으로 실행합니다.

```bash
./gradlew :feature:shop:ktlintCheck
./gradlew :feature:shop:ktlintFormat
```

전체 `ktlintFormat`은 광범위한 파일을 수정할 수 있으므로 초기 도입 단계에서는
모듈 단위로 실행합니다.
