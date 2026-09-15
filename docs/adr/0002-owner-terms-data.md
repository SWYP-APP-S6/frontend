# ADR-0002: Owner 약관 Repository와 Flavor별 화면 연결

- 상태: 채택됨
- 범위: Owner 약관 조회와 동의 화면. 실제 가입 요청은 다음 Auth 작업에서 연결한다.

## 결정

`data:owner:terms`는 Repository 계약, 앱 모델, DTO 검증과 Hilt 구현을 소유한다. Owner Auth Feature만 이 모듈에 의존한다. Consumer의 기존 약관 구현은 Consumer 소스셋으로 이동하여 동작과 의존성을 유지한다.

공개 약관은 `remote:auth`의 생성된 `TermsService`를 사용한다. 인증 인터셉터가 없는 별도 Retrofit으로 제공하여 세션 만료나 토큰 갱신 실패가 약관 조회를 막지 않도록 한다. ViewModel과 Navigation에는 Retrofit DTO를 전달하지 않는다.

문서 표시 순서는 서버 응답을 유지한다. REQUIRED/OPTIONAL만 전체 동의에 포함하며 NOTICE는 본문 보기만 제공한다. 선택은 문서 ID와 버전으로 복원하고, 알 수 없는 requirement 및 잘못된 응답은 진행 가능한 기본값으로 변환하지 않는다.

본문은 CommonMark를 HTML로 변환해 WebView에 표시한다. 원시 HTML은 이스케이프하고 JavaScript 및 파일 접근을 사용하지 않는다. 외부 링크는 시스템 URI 처리기로 전달한다.

## 명세 입력

사용자가 전달한 `mangro-app-openapi-2026-09-15.json`을 원본으로 사용한다. 원본·매핑·체크섬을 함께 갱신했으며 약관 보완 입력의 병합은 제거했다. 비공개 입력 묶음의 전달 방법은 [Remote README](../../remote/README.md)를 따른다.

약관의 role·type·requirement는 공식 enum으로 생성한다. 선택 필드인 effectiveDate는 앱 모델에서도 null을 허용하며, 값이 있을 때 날짜 형식을 검증한다.

## 검증 범위

생성 입력 분류, MockWebServer DTO/HTTP 계약, ViewModel 동의 조건 및 문서 버전 복원, Owner UI 목록·404·본문 렌더링과 기존 화면 이동을 검증한다. Consumer는 기존 화면 동작을 변경하지 않고 빌드 및 단위 테스트로 영향 여부를 확인한다.
