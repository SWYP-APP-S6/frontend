# Local storage

AuthKey와 사용자 데이터 저장을 위한 Android library 모듈이다. 현재는 모듈과 의존성만 선언하며 저장소 구현과 앱 연결은 아직 없다.

- `src/main`: 두 앱에서 공통으로 사용하는 AuthKey 저장소와 저장 관련 코드.
- `src/consumer`: Consumer UserInfoStore와 사용자 모델.
- `src/owner`: Owner UserInfoStore와 사용자 모델.

`role` Flavor는 앱과 같은 `consumer`·`owner`를 사용한다. Preferences DataStore로 항목별 값을 저장하고, 개별 value의 암복호화에는 `:core:crypto`의 `CryptoManager`를 사용한다. DataStore 의존성 선언만으로 저장 값이 자동 암호화되는 것은 아니다.

직접 사용하는 의존성은 Preferences DataStore, Coroutines, Hilt·KSP이며 버전은 Version Catalog에서 관리한다. Remote DTO와 JSON 직렬화에는 의존하지 않는다.

## 빌드 확인

```sh
./gradlew :core:local:ktlintCheck \
  :core:local:lintConsumerDebug :core:local:lintOwnerDebug \
  :core:local:assembleConsumerDebug :core:local:assembleOwnerDebug \
  :core:local:assembleConsumerRelease :core:local:assembleOwnerRelease
```
