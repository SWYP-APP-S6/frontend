# 공통 카카오 로그인·가입

Consumer/Owner 로그인 버튼은 Kakao Android SDK `v2-user`를 사용한다.
카카오톡이 있으면 앱 인증을 먼저 시도하고, 없거나 취소 외의 오류가 발생하면 웹 계정 로그인으로 전환한다.
SDK 인증 후 `:data:auth`의 `AuthRepository`가 SDK accessToken으로 Flavor별 검증 API를 호출한다. 응답의 accessToken이 있으면 함께 내려온 **맹그로 서버 accessToken·refreshToken**을 AuthStore에 저장하고 홈으로 이동한다. accessToken이 null이면 가입이 필요하므로 signupToken을 메모리에 보관하고 약관으로 이동한다. SDK 토큰은 AuthStore에 저장하지 않는다. SDK 또는 서버 검증 실패·취소 시 로그인 화면에 머물며 재시도할 수 있다.
Owner·Consumer 모두 시작 시 AuthStore를 읽어 저장된 세션이 있으면 홈으로, 없으면 로그인으로 이동한다. 저장소가 손상되면 세션을 제거하고 로그인을 다시 진행한다.

## 키 설정

루트 `local.properties`에 실제 네이티브 앱 키를 설정한다. 같은 이름의 환경 변수가 우선한다.

```properties
MANGRO_CONSUMER_KAKAO_NATIVE_APP_KEY=YOUR_CONSUMER_NATIVE_APP_KEY
MANGRO_OWNER_KAKAO_NATIVE_APP_KEY=YOUR_OWNER_NATIVE_APP_KEY
```

앱 모듈에서 Flavor별 `BuildConfig.KAKAO_NATIVE_APP_KEY`와 Manifest 스킴을 생성한다. 키를 문자열 리소스에 넣지 않는다. `Application`이 SDK를 초기화하고 Launcher가 `KakaoSdk.isInitialized`로 초기화 여부를 확인한다. 키가 없으면 SDK를 초기화하지 않고 버튼 클릭 시 설정 안내를 표시한다. 설정 후 앱을 다시 빌드한다.
키나 토큰은 로그로 출력하지 않는다. `local.properties`는 Git에 포함하지 않는다.
Native App Key만 사용하며 REST API 키, Admin 키, Client Secret을 앱에 넣지 않는다.

## 카카오디벨로퍼스 설정

- 해당 카카오 앱의 카카오 로그인을 활성화한다.
- 네이티브 앱 키의 Android 플랫폼에 사용하는 패키지를 등록한다: `com.swyp.mangro.consumer`, `com.swyp.mangro.owner`.
- 설치한 APK를 서명한 인증서의 키 해시를 등록한다. 개발자별 Debug 인증서 및 배포 시 Play 앱 서명 인증서를 구분한다.
- 두 앱을 동시에 설치할 경우 별도 네이티브 앱 키를 사용해야 콜백 스킴 충돌을 피할 수 있다. 같은 키를 사용하는 테스트에서는 한 앱씩 설치한다.
- Redirect URI는 Flavor별 `kakao{NATIVE_APP_KEY}://oauth`이며 Manifest에 자동 반영한다.

[SDK 시작하기](https://developers.kakao.com/docs/ko/android/getting-started), [로그인 가이드](https://developers.kakao.com/docs/ko/kakaologin/android)

## 실행 및 확인

```sh
./gradlew :app:installConsumerDebug
./gradlew :app:installOwnerDebug
```

1. 앱을 열고 카카오 로그인 버튼을 누른다.
2. 카카오톡 인증 또는 브라우저 로그인을 완료한다.
3. 서버 검증 결과에 따라 기존 회원은 홈, 신규 회원은 약관 진입을 확인한다. Owner 신규 회원은 약관 동의 후 상점 정보를 입력하고, 등록 신청 시 signup → 상점 등록 순서로 요청한다. Consumer는 약관 동의 후 signup 성공 시 홈으로 이동한다.
4. 인증 화면에서 뒤로 가기로 취소하면 로그인 화면에 머물고 재시도되는지 확인한다.
5. 카카오톡이 없는 기기에서도 웹 로그인 경로를 확인한다.

서버 검증, 약관 조회·상세, 가입, 서버 토큰 저장, 시작 시 세션 복원을 연결했다.
SDK accessToken은 검증 요청에 원문으로 전달하며 UI 상태·로그에 저장하지 않는다. SDK refreshToken은 앱 인증 흐름에 전달하지 않는다. 기존 회원은 검증 응답의 토큰 저장 후 홈으로 이동한다. Owner 신규 회원은 상점 정보 입력을 마친 뒤 가입 응답의 토큰을 저장하고, 이 토큰으로 상점을 등록한다. Consumer 신규 회원은 가입 응답의 토큰 저장 후 홈으로 이동한다. 서버 토큰 쌍이 누락되거나 비어 있으면 로그인 성공으로 처리하지 않는다.
Owner 상품·설정 계측 테스트는 Debug 전용 비공개 Activity에서 로그인 이후 UI를 독립 검증한다.
실제 계정 인증 성공은 자동 테스트와 별도로 확인해야 한다.

## 책임 분리

Route는 실행 시 Context를 ViewModel에 전달한다. ViewModel은 Context를 필드에 저장하지 않고 `KakaoLoginLauncher`로 SDK 인증을 시작한다. Launcher는 SDK 호출과 웹 로그인 재시도만 담당하고 Repository 역할을 갖지 않는다.

`:data:auth`의 `AuthRepository`는 카카오 토큰 검증·신규 가입·기존 `AuthStore`를 통한 회원 토큰 저장을 담당한다. 신규 회원의 signupToken은 Singleton Repository 메모리에만 보관하며 프로세스 종료 후에는 다시 로그인해야 한다. 로그인·가입은 `:remote:auth`의 DI에서 제공하는 `@Named("login") AuthService`를 사용하며 회원 Authorization 헤더와 401 자동 갱신이 없는 클라이언트로 실행된다. `AuthRepository`의 login/signup과 `TermsRepository`의 목록/상세는 `Flow<AuthResult<…>>`, 세션 확인은 `Flow<Boolean>`을 반환하고 ViewModel에서 수집한다. Flow는 수집할 때마다 요청을 실행하고 결과를 한 번 내보내므로 자동 재수집으로 가입 요청을 반복하지 않는다.

약관은 현재 Flavor의 role로 조회하며 문서 제목·필수 여부를 화면에 반영한다. 상세는 서버의 Markdown을 표시한다. 약관 확인 버튼을 누를 때 문서 ID·버전·필수 여부가 바뀌었으면 동의를 초기화한다. 동의는 현재 OpenAPI의 boolean 필드로 전달한다. Owner 가입 요청의 `locationTermsAgreed`와 `thirdPartyTermsAgreed`는 서버 계약에 따라 항상 `true`로 보내며, Consumer는 사용자가 선택한 동의 값을 사용한다. 서버 API가 문서 ID/버전을 받지 않으므로 조회와 가입 사이 변경을 서버에서 원자적으로 검증하는 것은 현재 계약으로 보장할 수 없다.

가입 성공 응답의 서버 토큰 쌍을 기존 암호화 AuthStore에 저장하고 signupToken을 지운다. 가입 성공 후 저장 실패만 발생했다면 응답 토큰을 메모리에 유지하여 재시도 때 가입 API를 다시 호출하지 않고 저장만 재시도한다. 프로세스 종료 시 이 임시 상태는 사라지므로 카카오 로그인을 다시 진행한다.

`BaseResponseInterceptor`는 공통 응답의 성공 코드 확인과 data 추출을 담당한다. 검증 모델의 선택적 서버 토큰은 null을 허용하고, 가입 API는 `Response<TokenResponse>`로 생성한다. 401/업무 오류와 파싱 실패는 Repository에서 별도 실패 결과로 매핑한다. 실계정과 서버 토큰 갱신은 로컬 MockWebServer 테스트와 별도로 검증해야 한다.

Owner 약관 확인은 `OwnerOnboardingRequired(consents)`로 앱의 상점 온보딩에 동의 값을 전달하며, 이 시점에는 signup을 호출하지 않는다. Consumer는 약관 확인에서 signup을 호출하고 `SignupCompleted`로 홈 이동을 요청한다.
