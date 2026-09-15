# Local storage

AuthKey와 사용자 데이터 저장을 위한 Android library 모듈이다. 공통 AuthStore를 구현했으며 UserInfoStore와 앱 연결은 아직 없다.

- `src/main`: 두 앱에서 공통으로 사용하는 AuthKey 저장소와 저장 관련 코드.
- `src/consumer`: Consumer UserInfoStore와 사용자 모델.
- `src/owner`: Owner UserInfoStore와 사용자 모델.

`role` Flavor는 앱과 같은 `consumer`·`owner`를 사용한다. Preferences DataStore로 항목별 값을 저장하고, 개별 value의 암복호화에는 `:core:crypto`의 `CryptoManager`를 사용한다.

직접 사용하는 의존성은 Preferences DataStore, Coroutines, Hilt·KSP이며 버전은 Version Catalog에서 관리한다. Remote DTO와 JSON 직렬화에는 의존하지 않는다.

## AuthStore

Hilt로 `AuthStore`를 주입받아 사용한다. `authKey: Flow<AuthKey?>`로 토큰 쌍을 조회하고, `save(AuthKey)`로 저장·교체하며 `clear()`로 삭제한다. `AuthKey`는 비어 있지 않은 accessToken·refreshToken을 요구하는 회원 세션 모델이다. refreshToken이 없는 게스트와 가입 중 임시 토큰은 포함하지 않는다.

- accessToken·refreshToken을 각각 UTF-8 바이트로 변환해 암호화한 뒤, `byteArrayPreferencesKey`로 저장한다. 두 값을 하나의 JSON으로 묶거나 평문으로 기록하지 않는다.
- 암호화 둘 다 성공한 후 하나의 `edit`에서 토큰 쌍을 교체한다. 암호화가 실패하면 이전 저장값을 유지한다.
- 읽기와 쓰기의 암복호화는 IO dispatcher에서 실행한다. `authKey`가 노출하는 Flow를 위해 Coroutines는 API 의존성이다.
- 두 값이 모두 없을 때만 `null`을 반환한다. 토큰 일부 누락, 복호화·UTF-8 해석 실패와 DataStore 읽기 오류는 호출자에게 전달하며 자동 삭제하지 않는다. 손상된 토큰도 `clear()`로 제거할 수 있다.
- `AuthKey.toString()`은 토큰을 마스킹한다.
- `LocalDiModule`이 AuthStore와 내부 DataStore를 앱별 싱글턴으로 생성한다. 파일은 백업 대상에서 제외되는 `context.noBackupFilesDir/auth.preferences_pb`에 둔다.
- 현재 CryptoManager의 암호문 형식과 AAD를 그대로 사용한다. 저장 항목 이름을 AAD로 추가하는 기능은 포함하지 않는다.

실제 로그인·토큰 갱신 API, 로그아웃 시 사용자 정보 제거, 계정 전환 중 진행 중인 요청 취소는 호출 계층에서 연결해야 한다.

## 빌드 확인

```sh
./gradlew :core:local:ktlintCheck \
  :core:local:lintConsumerDebug :core:local:lintOwnerDebug \
  :core:local:assembleConsumerDebug :core:local:assembleOwnerDebug \
  :core:local:assembleConsumerRelease :core:local:assembleOwnerRelease
```

## 기기 테스트

```sh
./gradlew :core:local:connectedConsumerDebugAndroidTest \
  :core:local:connectedOwnerDebugAndroidTest
```

실제 Keystore와 임시 DataStore 파일을 사용해 테스트용 JWT의 개별 암호화, 파일 재개방 후 복원, 갱신·삭제, 동시 저장, 암호화 실패 시 이전 값 보존, 손상·불완전 데이터 처리를 검증한다. 실제 서버 토큰은 사용하지 않는다.
