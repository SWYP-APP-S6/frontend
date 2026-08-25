# Application Architecture Convention

## 적용 상태

문서와 구현이 충돌하면 현재 코드와 Gradle 설정을 우선하고 같은 변경에서 문서를 갱신한다.

이 문서의 레이어 책임과 의존 제한은 새 기능을 설계할 때 적용한다. `consumer`와 `owner`를 Product Flavor로 분리하는 구성을 목표 아키텍처로 삼는다.

현재 `app/build.gradle.kts`에는 Product Flavor가 아직 구현되지 않았다. 현재 구현 상태와 목표 아키텍처를 혼동하지 않으며, Flavor를 도입하는 변경에서 Gradle 설정과 프로젝트 지도를 함께 갱신한다.

## 기본 구조

애플리케이션 로직은 `Presentation`, 선택적인 `Domain`, `Data`로 구분한다.

```text
Presentation ──> Domain ──> Data
       └─────────────────> Data  # Domain이 필요하지 않은 단순 기능
```

위 화살표는 호출 흐름을 나타낸다. 실제 Gradle 모듈의 의존 방향과 계약 위치는 모듈을 도입하는 ADR에서 결정한다.

## 레이어 책임

| 레이어 | 책임 | 포함하지 않는 항목 |
|---|---|---|
| Presentation | 화면 렌더링, 사용자 입력 전달, 화면 상태 관리 | 네트워크·DB 직접 호출, DTO·Entity 노출 |
| Domain | 복잡하거나 재사용되는 비즈니스 규칙과 UseCase | Android·Compose 타입, Retrofit·Room 구현 |
| Data | Repository 구현, 원격·로컬 데이터 소스, DTO·Entity 변환 | `UiState`, `UiAction`, `UiEvent`, 화면 이동 |

Presentation의 MVVM 계약은 다음 문서를 원본으로 삼는다.

- [ViewModel Convention](../presentation/ViewModel.md)
- [UiState Convention](../presentation/State.md)
- [UiAction Convention](../presentation/Action.md)
- [UiEvent Convention](../presentation/Event.md)

## Domain 적용 기준

Domain 레이어는 모든 기능에 의무적으로 추가하지 않는다. 다음 중 하나 이상에 해당할 때 도입한다.

1. 같은 비즈니스 규칙을 둘 이상의 화면이나 진입점에서 재사용한다.
2. 여러 Repository의 결과를 조합하거나 실행 순서를 제어한다.
3. 계산, 검증, 상태 전이처럼 UI와 분리해 단위 테스트할 규칙이 있다.

Repository 호출을 그대로 전달하기만 하는 UseCase는 만들지 않는다. 위 조건이 없다면 ViewModel이 Repository 계약을 직접 사용하고, 기능이 복잡해질 때 Domain을 분리한다.

## 의존성 규칙

- 비즈니스 규칙은 `Activity`, `ViewModel`, Composable과 같은 UI 구현에 의존하지 않는다.
- Presentation은 Retrofit Service, Room DAO, DTO, Entity에 직접 의존하지 않는다.
- Data는 Presentation 타입을 반환하거나 화면 이동을 결정하지 않는다.
- DTO와 Entity는 Data 내부에서 앱이 사용할 모델로 변환한다.
- 서로 다른 기능은 상대 기능의 내부 구현을 직접 참조하지 않는다.
- 공통 코드는 실제로 둘 이상의 소비자가 생기고 변경 이유가 같을 때만 추출한다.

## 모듈화 기준

현재 모듈 구조의 원본은 `settings.gradle.kts`이며 현재 등록된 Android 모듈은 `:app`뿐이다.

- 폴더 구조만 맞추기 위해 `core`, `data`, `feature` 모듈을 미리 만들지 않는다.
- Feature 경계는 소비자·점주 같은 역할 이름보다 화면 흐름이나 비즈니스 기능을 기준으로 검토한다.
- 소비자·점주 구분은 Feature 경계가 아니라 Product Flavor 기반 앱 구성 경계로 다룬다.
- Flavor별 전용 Feature가 필요하면 모듈 도입 후 `consumerImplementation` 또는 `ownerImplementation`으로 조립한다.
- 공유 모델과 Repository를 역할별 Feature에 중복 정의하지 않는다.

새 모듈을 추가할 때는 [저장소 아키텍처 규칙](../../agent/architecture.md)의 체크리스트를 따르고 다음 내용을 ADR에 기록한다.

1. 모듈이 소유하는 책임과 공개 계약
2. 허용하는 의존 방향
3. 기존 패키지 경계로 해결할 수 없는 이유
4. 빌드 시간과 테스트에 미치는 영향

## Product Flavor 앱 구성

`:app`은 `role` Flavor dimension 아래 두 개의 제품을 구성한다.

| Flavor | 배포 대상 | 예상 빌드 Variant |
|---|---|---|
| `consumer` | 소비자용 앱 | `consumerDebug`, `consumerRelease` |
| `owner` | 점주용 앱 | `ownerDebug`, `ownerRelease` |

- 두 Flavor는 서로 다른 `applicationId`를 사용하여 별도 APK/AAB로 설치하고 배포할 수 있어야 한다.
- 공통 Application, Activity, 비즈니스 로직은 `app/src/main`에서 공유한다.
- 앱별 시작 화면, Navigation 조립, Manifest, 리소스, 설정은 `app/src/consumer`와 `app/src/owner`에 둔다.
- Flavor 전용 의존성은 공통 `implementation`에 추가하지 않고 해당 Flavor configuration을 사용한다.
- 로그인 이후 역할을 선택하거나 실행 중 역할을 전환하는 기능을 만들지 않는다.
- 한 Flavor의 소스가 다른 Flavor의 source set을 직접 참조하지 않도록 한다.

정확한 `applicationId`, 앱 이름, 아이콘, 서명 및 배포 설정은 Flavor 구현 변경에서 결정한다.

## 의존성 주입

애플리케이션의 의존성 주입 프레임워크는 Hilt를 사용한다.

- ViewModel은 `@HiltViewModel`과 `@Inject constructor`로 의존성을 주입받는다.
- Route는 `hiltViewModel()`로 ViewModel을 주입받는다.
- Screen과 재사용 가능한 하위 Composable은 Hilt와 ViewModel에 의존하지 않는다.
- Service Locator를 만들거나 UI에서 Repository 또는 UseCase를 직접 생성하지 않는다.

현재 체크아웃에는 Hilt 플러그인과 의존성이 아직 없다. 실제 도입 변경에서 Version Catalog, convention plugin, 테스트 및 프로젝트 지도를 함께 갱신한다. Hilt Component와 Scope의 세부 구성은 실제 의존성 생명주기가 확인될 때 결정한다.

## 변경 시 확인

기능을 추가하거나 구조를 변경할 때 다음을 확인한다.

1. 각 클래스가 Presentation, Domain, Data 중 어느 책임을 가지는지 설명할 수 있는가?
2. Domain을 추가하거나 생략한 이유가 적용 기준과 일치하는가?
3. UI가 Data 구현 세부사항에 직접 의존하지 않는가?
4. 비즈니스 규칙이 Android UI 또는 외부 데이터 프레임워크와 분리되어 있는가?
5. 기능 간 직접 참조나 역할별 공통 코드 복제가 생기지 않았는가?
6. 소비자용과 점주용 앱이 런타임 역할 분기가 아닌 별도 Variant로 구성되어 있는가?
7. Flavor 전용 코드와 의존성이 공통 영역으로 유출되지 않았는가?
8. 모듈을 변경했다면 ADR, 프로젝트 지도, 전체 검증을 함께 갱신했는가?
9. 새 비즈니스 규칙과 데이터 변환을 관련 단위 테스트로 검증했는가?

## 이 문서에서 확정하지 않는 항목

- 최종 `core`, `data`, `feature` 모듈 목록
- Hilt Component와 Scope의 세부 구성
- 네트워크와 로컬 저장소 기술

위 항목은 실제 요구와 구현이 생긴 뒤 별도 컨벤션 또는 ADR로 결정한다.

## 출처

- [Notion: 프론트엔드 아키텍처 구성](https://app.notion.com/p/3bf995bccfff805f9272d04fc49e7828)
