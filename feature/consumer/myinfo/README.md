# Consumer 내 정보

Owner 설정 화면의 카드, 약관 목록, 로그아웃 확인 UI 구성을 Consumer 내 정보에 적용한다. `:app`의 Consumer 소스셋에서만 의존하고 하단 `MY` 탭에 연결한다.

- 저장된 회원 세션이 없으면 비회원 안내와 `카카오로 계속 연동하기` 버튼을 표시한다. 버튼은 기존 로그인 화면으로 이동하며, 이후 카카오 인증·가입은 `:feature:auth`가 처리한다.
- 회원이면 `UserRepository.fetchMe()`로 닉네임·전화번호를 조회한다. 조회 실패는 재시도를 제공하며 비회원으로 취급하지 않는다.
- 서비스 이용약관·개인정보 처리방침은 공통 인증 모듈의 약관 상세 화면으로 연결한다.
- 회원 로그아웃은 확인 후 `AuthRepository.logout()`으로 처리한다. 실패하면 화면을 유지하고, 성공하면 저장된 탭 스택을 정리하고 로그인으로 이동한다.
- 비회원 카카오 진입에서도 저장된 탭 스택을 정리해 인증 이후 이전 회원 상태가 복원되지 않게 한다.

검증 명령:

```bash
./gradlew :feature:consumer:myinfo:ktlintCheck :feature:consumer:myinfo:testDebugUnitTest :app:assembleConsumerDebug
./gradlew :feature:consumer:myinfo:connectedDebugAndroidTest
```

단위 테스트는 비회원 분기·회원 조회 실패 및 재시도·로그아웃 확인/중복 방지/재시도를 검증한다. 기기 테스트는 회원·비회원 화면의 표시와 액션 전달을 검증하며, 실제 카카오 계정 인증이나 서버 세션 발급까지 검증하지 않는다.
