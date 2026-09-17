# Remote API

Auth·User·Consumer·Owner Android library에서 OpenAPI Generator 7.24.0으로 Kotlin Retrofit Service와 DTO를 생성한다. 생성 코드는 각 모듈의 `build/generated/openapi`에 두며 커밋하지 않는다.

## 개발 환경 준비

명세·매핑·체크섬·서버 전달사항은 비공개 자료로 Git에서 제외한다. 새로 클론한 개발자와 CI는 빌드 전에 팀의 비공개 전달 경로로 다음 파일을 받아 저장소 루트에 배치해야 한다.

- `openapi/mangro-app-openapi-2026-09-17-v3.json`
- `openapi/endpoint-map.json`
- `openapi/model-map.json`
- `openapi/spec.sha256`

자동 다운로드는 아직 구현되어 있지 않다. 위 파일 없이 코드 생성과 앱 빌드는 실패한다. 파일은 같은 버전의 묶음으로 전달하고, 명세와 체크섬이 일치해야 한다. 전달사항과 내부 보고서도 비공개로 공유한다.

Python 3과 Android/Java 개발 환경이 필요하다. 템플릿과 생성 스크립트는 저장소에서 관리한다. 원본 JSON → `prepareOpenApiSpecs` → 모듈별 `openApiValidate` → `openApiGenerate` 순서로 실행되며 Debug/Release 컴파일에 연결된다.

## 공통 사용자 API

`GET /users/me`는 `:remote:user`에서 생성하고 `:data:user`가 공통 사용자 모델로 변환한다. 위치 조회·수정은 Consumer UserService에 유지한다. 생성 스크립트의 `client_mapping`이 이전 비공개 매핑 묶음의 `/users/me`도 공통 모듈로 옮기므로 매핑 파일의 별도 배포를 기다릴 필요가 없다. 생성 결과 검증에도 같은 규칙을 적용한다.

## DI

Hilt `SingletonComponent`에서 공통 OkHttpClient·Retrofit과 각 Service 인터페이스를 싱글턴으로 제공한다. app은 Auth를 공통 의존성으로, Consumer/Owner를 각각 Flavor 의존성으로 포함한다. Service 인터페이스를 생성자 주입으로 사용한다.

기본 클라이언트는 저장된 서버 토큰으로 인증 헤더·401 갱신 및 BaseResponse 해제를 처리한다. `AuthServices`, `ConsumerServices`, `OwnerServices` 팩토리는 테스트나 별도 Retrofit을 직접 구성할 때 사용할 수 있다.

## 검증

```sh
python3 -m unittest discover -s scripts/openapi -p 'test_*.py'
./gradlew :remote:auth:openApiGenerate :remote:consumer:openApiGenerate :remote:owner:openApiGenerate :remote:user:openApiGenerate
python3 scripts/openapi/check_generated.py
./gradlew :core:network:testDebugUnitTest :remote:auth:testDebugUnitTest :remote:consumer:testDebugUnitTest :remote:owner:testDebugUnitTest
```

기본 테스트는 MockWebServer를 사용한다. `-PmangroLiveApi=true`로 명시적으로 활성화하는 테스트는 운영 서버를 호출한다.

## 템플릿

`openapi/templates/kotlin/libraries/jvm-retrofit2/api.mustache`는 OpenAPI Generator 7.24.0 JAR의 동명 템플릿을 기반으로 DELETE-with-body 어노테이션 분기를 추가했다. 업그레이드 시 차이를 재검토한다. 생성 Kotlin은 직접 수정하지 않고 템플릿과 비공개 매핑을 수정한다.

## 공통 응답 해제

생성 서비스는 `@UnwrapBaseResponse`로 표시하며 `BaseResponseInterceptor`가 data를 추출한다. 호출부는 `body()`로 내부 DTO를 읽는다. 가입은 `Response<TokenResponse>`로 생성하며 응답의 서버 토큰 쌍을 저장한다. 직접 만든 Retrofit에도 Interceptor를 등록해야 한다.

## 2026-09-17 입력

9월 15일 전체 명세에 9월 17일 점주 명세를 병합한 비공개 입력을 사용한다. 체크섬 및 전달된 점주 경로 일치를 확인했다. 서버 명세 URL은 401로 실시간 비교하지 못했다. 재고 변경 요청에서 `cancelOverflow`가 제거되고 찜 취소 후보/취소 API와 서버 시각이 추가되었다. 공통 알림 생성 타입은 `:remote:user`로 이동했다. 비공개 입력·매핑·체크섬은 같은 묶음으로 준비한다.

## 2026-09-17 점주 명세 반영

현재 입력은 09-17 v3 전체 명세다. 이전에는 09-15 전체 명세에 09-17 점주 부분 명세를 병합했으나 전체 전달본으로 대체했다. 점주 부분 명세만으로 전체 입력을 대체하지 않는다. 소비자 전용 경로는 유지한다. 비공개 입력·매핑·체크섬은 같은 묶음으로 전달한다.

알림 조회·읽음 처리·FCM 등록/삭제는 양쪽 역할의 공통 API이므로 `:remote:user`에서 생성하고 Hilt로 제공한다. 기존 `ConsumerServices.notification` 접근은 공통 생성 타입을 참조하도록 유지한다. `DELETE /users/me`도 공통 UserService에 포함한다.

Owner HoldService에 재고 부족 취소 후보 조회·취소를 추가하고, 재고 수정 요청에서 `cancelOverflow`를 제거했다. 홈·찜 목록의 `serverTime`을 반영했으며, 점주 찜·공통 알림 목록은 새 명세의 `page`·`size`만 사용한다. 상품 업로드·미리보기·등록 계약은 동일하다.

## 2026-09-17 v2 변경

- 소비자 `GET /holds`, `GET /recipes`는 `page`(기본 0, 최소 0)와 `size`(기본 20, 1~100)를 명시한다. `sort`/`pageable`은 보내지 않으며 레시피의 `category`는 유지한다.
- 상품 등록 `pickupEndAt`은 한국 시간의 벽시계 값이다. 오프셋을 보내면 서버가 한국 시간으로 변환하고, 생략하면 가게 영업 종료 시각을 사용한다. 기존 앱의 `+09:00` 전송은 유효하므로 유지한다.
- 이전 09-17 병합본 대비 경로·operation 추가/삭제는 없고, 전체 46개 operation이다.
- v2 SHA-256: `4ccb4c42aea08f1d13f1ca666edfa853a7633c1b658120fd356feb37beb0f9b7`.
- `endpoint-map.json`, `model-map.json`, `spec.sha256`도 같은 비공개 묶음으로 제공해야 한다. `GET /v3/api-docs`는 확인 시 401이므로 배포 서버와의 실시간 동일성은 검증하지 못했다.

## 2026-09-17 v3 변경

- 전체 48개 operation: Auth 10, Consumer 15, Owner 16, User 7.
- Owner `IngredientService`에 식자재 검색(`query`, `size`)과 추천(`name`) API를 추가하고 Hilt로 제공한다.
- BaseResponse의 객체 배열을 `List<IngredientTagResponse>`로 해제한다.
- 상품 상세·미리보기의 `ingredientTags`는 ID 배열에서 `id`, `name`, nullable `category` 객체 목록으로 변경되었다. 등록·미리보기 요청의 태그 ID 목록 계약은 유지된다.
- 현재 상품 관리 도메인은 응답 객체에서 태그 ID 집합만 보존한다. 태그 이름 표시와 검색·추천 UI 연결은 후속 범위다.
- v3 SHA-256: `881bcb323add6059d6e75e50ad706193eddd6c55c5e80a6097d16e6bdc73febf`. 명세·endpoint-map·model-map·spec.sha256을 같은 비공개 묶음으로 전달한다. 실서버 동일성은 이번 갱신에서 검증하지 않았다.
