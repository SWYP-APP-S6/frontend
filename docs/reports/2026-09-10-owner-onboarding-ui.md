# #63 점주 최초 매장 등록 UI 구현 결과

`feat/owner-onboarding`, develop `a4d5664` 기반의 별도 worktree에서 구현했다. 기존 `feat/owner-home` worktree는 수정하지 않았다. 커밋·푸시·GitHub 이슈 변경은 하지 않았다.

## 구현 범위

- `:feature:owner:onboarding`: 상호·가게 종류·주소 검색/선택·상세 주소와 연락처·시간·요일의 2단계 등록 UI.
- 필수값에 따른 CTA 활성화, 단계 왕복·구성 변경 시 입력 유지, 전화번호 정규화, 전송 중 중복 클릭 방지, 실패 시 입력 유지·재시도, 성공 안내·완료 이벤트.
- 기존 디자인 시스템 입력/드롭다운/버튼/상단 바·Owner 폰트 사용. 전화·주의 아이콘은 Figma 원본 변환.
- Android 기본 시간 선택기, 주소 검색의 로딩·오류·빈 결과·취소 상태. 작은 화면/큰 글꼴에서는 시간 필드를 세로로 배치해 값이 잘리지 않게 했다.
- app Owner Debug 진입은 명시적인 샘플 안내와 검색/신청 fixture를 사용한다. `망원`으로 검색하면 샘플 주소가 나온다. Owner Release 진입은 기존 화면을 유지한다.
- `settings.gradle.kts`, Owner 전용 의존성, 프로젝트 지도, 모듈 README를 갱신했다. 공통 디자인 시스템·Consumer 소스는 변경하지 않았다.

[구현/호스트 계약](../../feature/owner/onboarding/README.md) · [사전 계획](../plans/2026-09-10-owner-onboarding-63.md)

## 검증 결과

- 성공: 기존/신규 파일 공백 검사, `ktlintCheck`.
- 성공: `:feature:owner:onboarding:testDebugUnitTest` 4개. 기본 정보·전화번호·시간 경계·요일 검증.
- 성공: `:app:testOwnerDebugUnitTest`, `:app:testConsumerDebugUnitTest` 각각 기존 예제 1개. 앱 전체 기능 검증을 의미하지 않는다.
- 성공: `:feature:owner:onboarding:lintDebug` — 오류/경고 없음.
- 성공: `:app:lintOwnerDebug`, `:app:lintConsumerDebug` — 오류 없음, 각각 18개 기존 버전/리소스 관련 경고.
- 성공: `:app:assembleOwnerDebug`, `:app:assembleConsumerDebug`, `:app:compileOwnerReleaseKotlin`.
- 성공: emulator-5554 계측 테스트 9개. 마지막 수정 후 재실행 결과 `OK (9 tests)`.
  - 필수값 미입력, 종류·주소 선택, 뒤로가기·저장 상태 복원, 요일/시간 검증, 신청 실패 재시도, 중복 신청 방지, Android 시간 선택기의 확인·취소, 320dp/글꼴 1.3배와 실제 텍스트 잘림 검사, 주소 검색 실패·빈 결과·취소.
- 성공: 최종 Owner Debug APK 설치·실행 및 화면 확인.
- 미실행: 실물 기기, Release APK 설치, Consumer UI 계측, 실제 주소 서비스·등록 API 종단 간 검증.

UI 계측은 테스트 APK 설치 후 다음 runner를 직접 실행했다. Gradle UTP의 테스트 앱 정리로 캡처가 지워지지 않도록 직접 실행했다.

```sh
adb -s emulator-5554 shell am instrument -w \
  -e class com.swyp.mangro.feature.owner.onboarding.OwnerOnboardingScreenTest \
  com.swyp.mangro.feature.owner.onboarding.test/androidx.test.runner.AndroidJUnitRunner
```

최종 로그: [빌드/Lint](/tmp/onboarding-layout-verify.log), [UI 테스트](/tmp/onboarding-layout-ui.log). Consumer와 앱 단위 테스트를 포함한 앞선 전체 검증: [로그](/tmp/onboarding-verify.log).

## 화면 캡처

로컬 임시 파일이므로 필요하면 별도 보관해야 한다.

- [Step1](/tmp/onboarding-reviewed-captures/step1.png)
- [Step2](/tmp/onboarding-reviewed-captures/step2.png)
- [작은 화면·큰 글꼴](/tmp/onboarding-reviewed-captures/small-large-font.png)
- [실제 Owner 앱 진입 화면](/tmp/onboarding-app-final.png)

## 남은 연결·결정

실제 주소 검색 서비스와 등록 API는 저장소 및 제공 자료에 없어 연결되지 않았다. 현재 Debug의 성공은 샘플 동작이며 실서비스 접수가 아니다. 프로덕션 호스트는 `onSearchAddress`/`onSubmit`에 실제 구현을 전달해야 한다.

가게 종류의 실제 코드, 연락처 검증 규칙, 상세 주소 필수 여부, 심야·24시간 영업 지원을 확정해야 한다. 현재는 상세 주소 선택 입력, 0으로 시작하는 9~11자리 연락처, 같은 날 시작보다 늦은 종료 시간, 최소 하루의 영업 요일을 적용했다.

신청 이후 심사 상태·재진입 분기는 #56에서 연결한다. 주소 검색·시간 선택·신청 완료/대기 화면의 별도 확정 디자인이 오면 현재 공통/기본 UI를 맞춰야 한다. 프로세스 종료 중 접수 여부는 서버 조회/멱등성 계약이 필요하다.

#63의 UI와 연결 계약은 구현했지만, 이슈 전체의 실서비스 등록 흐름이 완료된 상태는 아니다.
