#!/bin/sh

set -eu

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

mode="${1:---quick}"

if [ "$#" -gt 1 ]; then
    echo "사용법: $0 [--quick|--full|--device]"
    exit 2
fi

check_diff() {
    echo "[검증] staged/unstaged diff 공백 검사"
    git diff --check
    git diff --cached --check
}

run_quick() {
    check_diff
    echo "[검증] ktlint와 Debug 단위 테스트"
    ./gradlew \
        ktlintCheck \
        :app:testDebugUnitTest \
        --console=plain
}

run_full() {
    check_diff
    echo "[검증] ktlint, 단위 테스트, Android Lint, Debug APK 조립"
    ./gradlew \
        ktlintCheck \
        :app:testDebugUnitTest \
        :app:lintDebug \
        :app:assembleDebug \
        --console=plain
}

case "$mode" in
    --quick)
        run_quick
        ;;
    --full)
        run_full
        ;;
    --device)
        run_full
        echo "[검증] 연결된 기기 또는 에뮬레이터에서 계측 테스트"
        ./gradlew :app:connectedDebugAndroidTest --console=plain
        ;;
    *)
        echo "알 수 없는 검증 수준: $mode"
        echo "사용법: $0 [--quick|--full|--device]"
        exit 2
        ;;
esac

