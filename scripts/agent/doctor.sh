#!/bin/sh

set -eu

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || true)"

if [ -z "$repo_root" ]; then
    echo "[실패] Git 저장소 안에서 실행해야 합니다."
    exit 1
fi

cd "$repo_root"

failure_count=0
warning_count=0

pass() {
    echo "[성공] $1"
}

fail() {
    echo "[실패] $1"
    failure_count=$((failure_count + 1))
}

warn() {
    echo "[주의] $1"
    warning_count=$((warning_count + 1))
}

if command -v java >/dev/null 2>&1; then
    java_version_line="$(java -version 2>&1 | sed -n '1p')"
    java_major="$(printf '%s\n' "$java_version_line" | sed -E 's/.*version "([0-9]+).*/\1/')"

    if [ "$java_major" = "17" ]; then
        pass "프로젝트 기준 Java 17을 확인했습니다: $java_version_line"
    else
        warn "현재 Java가 프로젝트 기준 17과 다릅니다: $java_version_line"
    fi
else
    fail "Java 명령을 찾을 수 없습니다. JDK 17을 확인하세요."
fi

if [ -x "./gradlew" ]; then
    pass "Gradle Wrapper 실행 파일을 확인했습니다."
else
    fail "실행 가능한 ./gradlew 파일이 없습니다."
fi

for required_file in \
    AGENTS.md \
    CLAUDE.md \
    docs/agent/README.md \
    docs/agent/project-map.md \
    docs/agent/architecture.md \
    docs/agent/validation.md \
    docs/agent/delivery.md \
    scripts/agent/verify.sh
do
    if [ -f "$required_file" ]; then
        pass "$required_file 파일을 확인했습니다."
    else
        fail "$required_file 파일이 없습니다."
    fi
done

if [ -n "${ANDROID_HOME:-}" ] || [ -n "${ANDROID_SDK_ROOT:-}" ] || [ -f "local.properties" ]; then
    pass "Android SDK 위치 설정을 확인했습니다. 값은 출력하지 않습니다."
else
    warn "ANDROID_HOME, ANDROID_SDK_ROOT, local.properties 중 확인 가능한 SDK 설정이 없습니다."
fi

hooks_path="$(git config --local --get core.hooksPath || true)"
if [ "$hooks_path" = ".githooks" ]; then
    pass "저장소 Git hook 경로가 .githooks로 설정되어 있습니다."
else
    warn "Git hook이 설치되지 않았습니다. ./scripts/agent/install-git-hooks.sh를 실행하세요."
fi

echo
echo "진단 결과: 실패 ${failure_count}개, 주의 ${warning_count}개"

if [ "$failure_count" -ne 0 ]; then
    exit 1
fi
