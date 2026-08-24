#!/bin/sh

set -eu

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

git config --local core.hooksPath .githooks

if [ -f ".gitmessage" ]; then
    git config --local commit.template .gitmessage
    echo "[성공] 커밋 템플릿을 .gitmessage로 설정했습니다."
else
    echo "[주의] .gitmessage가 없어 커밋 템플릿은 설정하지 않았습니다."
fi

echo "[성공] Git hook 경로를 .githooks로 설정했습니다."

