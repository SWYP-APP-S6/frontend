# ADR-0003: Owner 인증 Repository와 세션 경계

상태: 구현, 최신 가입 계약 확인 및 실제 계정 검증 대기

## 범위

`feat/owner-terms-api`의 로컬 커밋을 기반으로 `feat/owner-auth-api`에서 구현한다. Consumer 로그인·스플래시·Application의 기존 구현은 Consumer 소스셋으로 이동하여 유지한다.

- `data:owner:auth`의 `OwnerAuthRepository`가 Owner 로그인, 가입, 세션 복원과 로그아웃을 소유한다.
- 카카오 SDK 2.25.0은 Owner 의존성으로만 포함한다. SDK 로그인 결과의 access token을 `/auth/owner/kakao`로 전달한다. auth code exchange는 중복 호출하지 않는다.
- 신규 회원은 메모리의 signupToken으로 약관 동의 화면에 진입한다. 가입 성공 후에만 앱 토큰을 저장하고 온보딩으로 이동하며 인증 화면은 back stack에서 제거한다.
- 기존 회원과 복원된 세션은 홈으로 이동한다. 상점 보유 여부는 `registered`로 추정하지 않는다. 상점 GET me 기반 홈/온보딩 분기는 3단계에서 추가해야 한다.
- signupToken과 카카오 토큰은 SavedStateHandle, navigation arguments, UI state, 디스크에 저장하지 않는다. 가입 중 프로세스 종료로 signupToken이 사라지면 로그인부터 다시 진행한다.

## 토큰 저장과 갱신

앱 access/refresh token 쌍을 Android Keystore의 AES-GCM 키로 암호화하고 `noBackupFilesDir`에 AtomicFile로 저장한다. 저장 성공 후에만 인증 상태를 게시한다. 시작 시 저장된 refresh token을 서버에 제출해 세션을 복원한다. 네트워크/서버 장애에서는 기존 토큰을 삭제하지 않고 시작 화면에서 재시도한다. refresh 401에서는 세션을 폐기한다. 키 무효화·암호문 손상으로 복호화할 수 없는 세션도 제거하고 재로그인으로 복구한다.

`@OwnerAuthenticated OkHttpClient`는 다음 Owner API 단계에서 주입할 인증 전용 클라이언트다. API origin이 일치할 때만 헤더를 붙이고 redirect를 따르지 않는다. 401 이후 거절된 access token과 현재 token을 비교하며 Mutex로 갱신을 직렬화한다. 재전송은 한 번만 하고 one-shot/duplex body는 재전송하지 않는다. Terms 및 인증 API는 별도 공개 클라이언트를 사용하므로 갱신 재귀가 발생하지 않는다.

로그아웃은 서버 refresh token 폐기를 시도한 뒤 로컬 세션을 제거한다. 설정 화면의 로그아웃 UI 추가는 이번 단계에 포함하지 않는다.

## 약관 매핑과 서버 계약 제약

제출 직전에 OWNER 약관 목록을 다시 조회하여 화면에서 확인한 문서 및 버전이 유지되는지 검사한다. 변경 시 재동의를 요청한다. 실제 선택한 REQUIRED/OPTIONAL만 매핑하고 NOTICE는 항상 제외한다.

| 약관 type | 기존 가입 필드 |
|---|---|
| SERVICE | serviceTermsAgreed |
| PRIVACY_COLLECTION | privacyTermsAgreed |
| MARKETING | marketingOptIn |
| LOCATION | locationTermsAgreed |
| THIRD_PARTY | thirdPartyTermsAgreed |

화면에 없는 항목은 false로 유지한다. 알 수 없는 선택 약관은 임의 매핑하지 않고 제출을 차단한다. 현재 공개 Owner 목록에는 LOCATION/THIRD_PARTY가 없다. 최신 OpenAPI를 받지 못했으므로 기존 boolean 계약에 대한 호환 구현이며 실제 가입 성공은 미검증이다. 문서 ID/version을 받는 새 가입 요청 형식이 있다면 이 매핑과 코드 생성을 함께 갱신해야 한다. 1단계 Terms 보완 스펙 제약도 유지된다.

## 로컬 설정

`local.properties` 또는 환경변수에 `MANGRO_OWNER_KAKAO_NATIVE_APP_KEY`를 설정한다. 환경변수가 우선한다. 값은 출력하거나 커밋하지 않는다. 값이 없으면 빌드는 가능하며 로그인 버튼에서 오류를 표시한다. Owner Application에서 SDK를 초기화하고 Owner Manifest에 `kakao{nativeKey}://oauth` callback을 등록한다.

Kakao Developers에서 Owner 패키지 `com.swyp.mangro.owner`와 사용하는 서명의 키 해시 등록, 카카오 로그인 활성화가 필요하다. 콘솔 설정과 테스트 계정은 로컬 코드가 대신 생성하지 않는다.

참고: [Kakao Android 설정](https://developers.kakao.com/docs/en/android/getting-started), [로그인 API](https://developers.kakao.com/docs/en/kakaologin/android), [SDK 릴리스](https://developers.kakao.com/docs/en/android/download).

## 검증 경계

Repository 테스트는 실제 Retrofit 직렬화와 MockWebServer를 사용한다. Owner ViewModel은 명시적인 fake repository로 신규/기존 회원, 취소·실패·중복 제출을 검사한다. 화면 navigation 테스트는 OAuth를 우회하는 제품 코드 없이 테스트에서 온보딩 목적지로 진입한다. 실제 카카오 로그인·가입 성공은 앱 키, 콘솔 등록, 테스트 계정과 최신 가입 계약 확인 후 별도로 검증한다.
