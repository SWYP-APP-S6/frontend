# Remote API

Auth·Consumer·Owner Android library에서 OpenAPI Generator 7.24.0으로 Kotlin Retrofit Service와 DTO를 생성한다. 생성 코드는 각 모듈의 `build/generated/openapi`에 두며 커밋하지 않는다.

## 개발 환경 준비

명세·매핑·체크섬·서버 전달사항은 비공개 자료로 Git에서 제외한다. 새로 클론한 개발자와 CI는 빌드 전에 팀의 비공개 전달 경로로 다음 파일을 받아 저장소 루트에 배치해야 한다.

- `openapi/mangro-app-openapi-2026-09-15.json`
- `openapi/endpoint-map.json`
- `openapi/model-map.json`
- `openapi/spec.sha256`

자동 다운로드는 아직 구현되어 있지 않다. 위 파일 없이 코드 생성과 앱 빌드는 실패한다. 파일은 같은 버전의 묶음으로 전달하고, 명세와 체크섬이 일치해야 한다. 전달사항과 내부 보고서도 비공개로 공유한다.

Python 3과 Android/Java 개발 환경이 필요하다. 템플릿과 생성 스크립트는 저장소에서 관리한다. 원본 JSON → `prepareOpenApiSpecs` → 모듈별 `openApiValidate` → `openApiGenerate` 순서로 실행되며 Debug/Release 컴파일에 연결된다.

## DI

Hilt `SingletonComponent`에서 공통 OkHttpClient·Retrofit과 각 Service 인터페이스를 싱글턴으로 제공한다. app은 Auth를 공통 의존성으로, Consumer/Owner를 각각 Flavor 의존성으로 포함한다. Service 인터페이스를 생성자 주입으로 사용한다.

`TermsService`는 인증 상태와 무관하게 조회하도록 별도의 인증 없는 Retrofit을 사용한다. 기본 클라이언트에는 인증 인터셉터가 없다. 토큰 저장·인증 헤더·갱신과 화면 연동은 후속 작업이다. `AuthServices`, `ConsumerServices`, `OwnerServices` 팩토리는 테스트나 별도 Retrofit을 직접 구성할 때 사용할 수 있다.

## 검증

```sh
python3 -m unittest discover -s scripts/openapi -p 'test_*.py'
./gradlew :remote:auth:openApiGenerate :remote:consumer:openApiGenerate :remote:owner:openApiGenerate
python3 scripts/openapi/check_generated.py
./gradlew :core:network:testDebugUnitTest :remote:auth:testDebugUnitTest :remote:consumer:testDebugUnitTest :remote:owner:testDebugUnitTest
```

기본 테스트는 MockWebServer를 사용한다. `-PmangroLiveApi=true`로 명시적으로 활성화하는 테스트는 운영 서버를 호출한다.

## 템플릿

`openapi/templates/kotlin/libraries/jvm-retrofit2/api.mustache`는 OpenAPI Generator 7.24.0 JAR의 동명 템플릿을 기반으로 DELETE-with-body 어노테이션 분기를 추가했다. 업그레이드 시 차이를 재검토한다. 생성 Kotlin은 직접 수정하지 않고 템플릿과 비공개 매핑을 수정한다.
