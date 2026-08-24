# ViewModel Convention

## 적용 상태
문서와 구현이 충돌하면 현재 코드와 Gradle 설정을 우선하고 같은 변경에서 문서를 갱신한다.

## 책임

ViewModel은 화면 단위 상태를 관리하고 UI와 비즈니스 로직의 경계를 연결한다.

- 현재 화면을 나타내는 `UiState`를 소유하고 읽기 전용으로 노출한다.
- UI에서 전달된 `UiAction`을 `handleAction`으로 처리한다.
- UI가 한 번 수행할 동작이 필요하면 `UiEvent`를 읽기 전용으로 노출한다.
- 화면 데이터를 조회하고 UI가 사용할 형태로 변환한다.

세부 계약은 다음 문서를 원본으로 삼는다.

- [UiState Convention](./State.md)
- [UiAction Convention](./Action.md)
- [UiEvent Convention](./Event.md)

## 외부 공개 API

ViewModel 외부에는 다음 API만 노출하는 것을 권장한다.

- 읽기 전용 `UiState`
- 읽기 전용 `UiEvent`
- `handleAction(action: UiAction)`

변경 가능한 상태 객체, 상태 갱신 함수, Repository 또는 UseCase를 외부에 노출하지 않는다.

## Android UI 의존성 제한

ViewModel은 다음 객체에 의존하지 않는다.

- `Activity`, `Fragment`, `View`
- `Context`, `Resources`
- `NavController`, `NavHostController`
- Composable 함수와 Compose UI 타입

AndroidX Lifecycle의 `ViewModel`과 `SavedStateHandle`은 사용할 수 있다. Android Resource 변환이나 화면 이동처럼 UI 구현에 해당하는 작업은 Route에서 처리한다.

## 생성자 규칙

`SavedStateHandle`이 필요하면 생성자의 첫 번째 파라미터로 선언하고, 나머지 의존성은 그 뒤에 배치한다.

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val loginUseCase: LoginUseCase,
) : ViewModel()
```

ViewModel은 `@HiltViewModel`과 `@Inject constructor`를 사용해 의존성을 생성자 주입받는다. Route는 `hiltViewModel()`로 ViewModel을 주입받는다.

## 함수 네이밍

| 목적 | 형식 | 예시 |
|---|---|---|
| Action 처리 | `handleAction` | `handleAction(action)` |
| 화면 데이터 조회 | `fetch + 대상` | `fetchProfile()` |
| 상태 갱신 | `update + 대상` | `updateEmail(value)` |

조회와 상태 갱신을 위한 보조 함수는 외부에서 직접 호출할 이유가 없다면 `private`으로 선언한다.

이 네이밍은 ViewModel 내부 함수에만 적용한다. UseCase와 Repository는 각 계층의 역할과 도메인 용어에 맞는 이름을 사용한다.

## 변경 시 확인

ViewModel을 추가하거나 수정할 때 다음을 함께 확인한다.

1. `UiState`, `UiAction`, `UiEvent` 경계가 각 컨벤션과 일치하는가?
2. Android UI 객체나 변경 가능한 상태를 외부에 노출하지 않는가?
3. 동작 변경을 검증하는 ViewModel 단위 테스트가 있는가?
4. 새 의존성을 도입했다면 Version Catalog와 프로젝트 지도를 갱신했는가?
5. Hilt ViewModel Annotation과 생성자 주입 규칙을 지켰는가?
