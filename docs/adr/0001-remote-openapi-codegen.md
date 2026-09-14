# ADR-0001: 역할별 Remote와 공통 Auth의 OpenAPI 생성

- 상태: 승인됨 (사용자 계획 승인 및 구현 요청)
- 날짜: 2026-09-13

## 배경

전달된 OpenAPI 3.1 명세의 36 API를 Consumer·Owner 앱에 연결해야 하며, 클라이언트에는 Service/Request/Response 명명과 프로퍼티 기본값 규칙이 있다.

## 결정

`remote:consumer`, `remote:owner`, `remote:auth`를 일반 Android library로 만들고 app Flavor에서 조립한다. OpenAPI Generator 7.24.0의 Kotlin Retrofit coroutine 생성기를 사용하며 버전은 Catalog에서 관리한다. 원본은 보존하고 Python 표준 라이브러리로 분리·명명·확인된 Pageable 변환을 수행한다. 필드 기본값은 vendor extension과 두 모델 부분 템플릿으로 생성한다.

서비스는 주입된 Retrofit으로 만들고 공통 네트워크 코드는 core:network에 둔다. core가 remote에 의존하지 않는다. 인증 저장·갱신과 UI 연동은 후속이다. 생성 결과는 build 아래에만 두고 Variant API로 Debug/Release 컴파일과 연결한다.

## 결과

API 중복 없이 역할 경계를 검증하고 재생성할 수 있다. DTO는 모듈별 패키지에 중복될 수 있다. 기본값이 응답 누락을 감출 수 있어 사용 경계 검증이 필요하며, enum 기본값과 템플릿은 명세 변경 때 검토해야 한다. Python 3이 빌드 전제 조건으로 추가된다.

## 검토한 대안

수동 Service/DTO 작성은 재생성과 서버 계약 비교가 어려워 제외했다. 전체 Auth를 Consumer/Owner에 중복 생성하는 방식은 공통 인증 모듈 결정으로 대체했다. 생성된 별도 Gradle 프로젝트는 기존 Catalog·convention 소유권을 유지하기 위해 사용하지 않는다.

## 검증

분류·wire contract 보존 테스트, 실제 생성 코드 검사, MockWebServer 요청/응답 테스트, 양쪽 앱 빌드 및 remote Release 컴파일로 확인한다. 실행 결과는 작업 보고서에 기록한다.

## 재검토 조건

새로운 조합 schema, non-null 순환 모델, 인증 플로 변경, enum 확장 또는 generator 업그레이드 시 변환기·템플릿·기본값 정책을 재검토한다.
