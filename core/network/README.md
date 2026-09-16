# 네트워크 인증

## 실행 흐름

- 공통 Accept를 강제하지 않는다. Content-Type은 OpenAPI의 JSON·multipart 요청에 맞춰 실제 body가 결정한다.
- `AuthorizationInterceptor`: TokenProvider에서 현재 accessToken을 조회해 Authorization에 부착한다. 외부 origin과 명시적 Authorization은 처리하지 않는다.
- `TokenAuthenticator`: 요청의 Bearer 헤더에서 실패한 토큰을 읽고 TokenProvider가 저장된 세션과 비교하여 갱신한다. priorResponse의 401 이력으로 재시도를 한 번으로 제한한다. 직접 지정한 토큰도 저장된 세션에 해당하면 갱신할 수 있다. 403과 다른 origin에는 갱신을 시도하지 않는다. body 재전송 가능 여부는 OkHttp에 맡긴다.
- `app/auth/StoreTokenProvider`: `runBlocking { authStore.authKey.first() }`로 DataStore Flow를 동기 OkHttp 경계에 연결한다. 호출 스레드는 읽기 완료까지 대기하며 메모리 토큰 캐시는 아니다.
- 갱신에는 별도 클라이언트로 생성한 `TokenRefreshService`를 사용한다. 생성된 요청·응답 DTO를 재사용하고 같은 토큰 쌍의 accessToken을 명시적인 Authorization 헤더로 전달한다. Authenticator와 dispatcher를 일반 API 클라이언트와 분리한다.

## 갱신과 세션 변경

같은 실패 토큰의 진행 중 갱신은 CompletableFuture로 성공과 실패를 공유한다. 완료 후 진입한 새 호출은 다시 갱신할 수 있다. 갱신·세션 확인은 Mutex로 직렬화한다. 성공한 갱신의 이전·새 accessToken을 기억하여 같은 실패 토큰으로 대기하던 요청은 저장된 새 토큰을 사용한다. 다른 로그인에서 저장한 토큰은 이전 요청에 재사용하지 않는다.

새 토큰은 `AuthStore.replaceIfMatches(expected, updated)`로 저장한다. DataStore edit 안에서 기존 토큰 쌍을 비교하므로 갱신 요청 중 로그아웃이나 계정 변경이 발생하면 결과를 버린다.

Provider의 갱신 실패 IOException은 Authenticator에서 null로 처리하여 원래 401을 반환한다. 토큰이 없거나 비어 있거나 실패한 토큰과 같아도 재전송하지 않는다. 자동 로그아웃이나 UserInfoStore 삭제는 수행하지 않으며 세션 종료 UI 정책은 호출 계층에서 결정한다.

## 적용 범위

9월 15일 OpenAPI는 로그인·게스트·갱신을 포함한 모든 operation에 bearerAuth를 상속하며 예외 선언이 없다. 경로에 따른 인증 제외 규칙을 두지 않는다. 가입 토큰은 SignupRequest.signupToken body 필드이며 Authorization을 대체하지 않는다. AuthStore에는 회원 토큰 쌍만 저장한다.

401을 갱신 대상으로 삼는 것은 클라이언트 복구 정책이며, 명세가 모든 401을 토큰 만료라고 정의하지는 않는다. Authenticator에 경로별 예외는 두지 않는다. 갱신 전용 클라이언트에는 Authenticator가 없어 갱신 재귀가 발생하지 않는다. 로그아웃의 로컬 저장소 clear는 Repository에서 처리한다.

생성된 API 파일은 수정하지 않는다. remote:auth의 TokenRefreshService는 갱신 요청의 Authorization을 명시적으로 전달하기 위한 작은 Retrofit 계약이며 DTO는 생성본을 사용한다. `core:network`는 TokenProvider 계약만 알고 `app`이 `core:local`과 `remote:auth`를 조립한다. 로그인 성공 시 최초 토큰을 AuthStore에 저장하는 UI 연결은 별도 작업이다.

## 검증

- `:core:network:testDebugUnitTest`: 헤더, 401 재시도 제한, 외부 origin·리다이렉트, 명시적 인증, 403 처리.
- `:app:testConsumerDebugUnitTest`, `:app:testOwnerDebugUnitTest`: StoreTokenProvider의 갱신 API body, 동시 갱신, 저장 실패, 로그아웃 중 갱신, 계정 변경 및 재요청 검증.
- `:core:local:connectedConsumerDebugAndroidTest`, `:core:local:connectedOwnerDebugAndroidTest`: 실제 Keystore·DataStore의 조건부 토큰 교체 검증.

## BaseResponse 처리

`NetworkModule.provideRetrofit()`은 `BaseResponseInterceptor`를 추가한다. 표시된 Retrofit 메서드의 HTTP 2xx 응답에서 본문 status가 2xx이고 code가 OK인지 확인한 뒤 data만 반환한다. HTTP 오류의 본문과 헤더는 유지하며, 공통 형식/업무 오류는 BaseResponseException에 상태와 코드를 담아 전달한다. data 누락은 실패이며 null은 명시적으로 허용된 메서드만 수용한다. HTTP 204/205는 그대로 통과한다. 별도 로그인·약관·갱신 클라이언트에도 Interceptor를 등록한다.
