# Owner Setting

- `GET /owner/stores/me`로 상점 이름·전화번호를 표시한다. 진입·복귀 시 갱신하고 조회 오류는 재시도할 수 있다. 예시 정보는 Preview에만 둔다.
- 로그아웃은 인증된 `POST /auth/logout`에 refreshToken을 전달하고 토큰·사용자 정보·가입 대기 정보를 삭제한다. 성공 후 Owner 화면 스택을 제거하고 로그인으로 이동한다. 서버/저장 실패 시 재시도하며, 이미 만료된 세션(401)은 로컬 정보를 정리한다.
- 약관 목록은 Owner role로 조회하고 SERVICE 및 PRIVACY_POLICY 문서 ID로 상세 Markdown을 조회한다. 문서 누락·오류는 재시도 화면을 표시한다.
- Repository는 Flow, 화면은 StateFlow와 Action/Event를 사용한다.

검증: 설정 ViewModel 단위 테스트, 인증 양쪽 Flavor 및 상점 Repository 테스트, 설정·약관 Compose 기기 테스트. 실제 계정 로그아웃은 자동 테스트에서 실행하지 않는다.
