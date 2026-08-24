# Compose UI Convention

> 이 문서는 Compose UI의 컴포넌트 API와 화면 구성 규칙을 정의하는 초안이다.
> 현재 체크아웃에 Compose와 Hilt 구현이 없으므로 실제 도입 과정에서 코드와 함께 검증한다.

## 적용 범위

이 문서는 다음 내용을 다룬다.

- Composable 함수와 Callback 작성 규칙
- 상태 호이스팅과 로컬 UI 상태의 경계
- Route와 Screen의 책임
- `UiState`, `UiAction`, `UiEvent`의 Compose 연결 방식
- Navigation 의존성 경계

각 계약의 정의와 선택 기준은 다음 문서를 원본으로 삼는다.

- [UiState Convention](./State.md)
- [UiAction Convention](./Action.md)
- [UiEvent Convention](./Event.md)

## Composable 파라미터 순서

Composable 함수의 파라미터는 다음 순서를 권장한다.

1. 필수 데이터와 Callback
2. `Modifier`
3. 기본값이 있는 선택 파라미터
4. 선택적 Content Lambda
5. 필수 Content Lambda

```kotlin
@Composable
fun LoginButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = "로그인")
    }
}
```

`Modifier`는 기본값으로 `Modifier`를 사용하고 Composable의 최상위 UI 요소에 적용한다.

## 컴포넌트 책임과 상태 호이스팅

Composable은 하나의 명확한 UI 책임만 갖도록 작성한다. 재사용 가능한 하위 컴포넌트는 ViewModel, `UiAction`, `NavController`에 의존하지 않고 필요한 값과 Callback만 전달받는다.

```kotlin
LoginButton(
    enabled = uiState.isLoginEnabled,
    onClick = { onAction(LoginUiAction.LoginClicked) },
)
```

`LoginButton`은 클릭이 어떤 비즈니스 동작을 시작하는지 알지 못한다. Feature Screen이 Callback을 `UiAction`으로 변환한다.

## Route와 Screen

화면은 ViewModel과 UI 계약을 연결하는 `Route`와 상태를 렌더링하는 `Screen`으로 구분한다.

```text
NavGraph
    ↓ Navigation 함수
Route
    ├─ UiState 수집
    ├─ UiEvent 처리
    └─ handleAction 연결
    ↓
Screen
    ├─ UiState 렌더링
    └─ 사용자 입력 전달
```

| 구분 | 책임 | 의존 가능 대상 |
|---|---|---|
| Route | ViewModel 연결, 상태 수집, Event 처리, Navigation 연결 | ViewModel, 화면 이동 함수 |
| Screen | 상태 렌더링, 사용자 입력 전달 | `UiState`, Callback, Compose UI 타입 |

Route는 Hilt가 생성한 ViewModel을 `hiltViewModel()`로 주입받는다. Screen과 재사용 가능한 하위 Composable은 ViewModel 생성 방식을 알지 못한다.

## Route 작성 규칙

Route는 다음 작업만 담당한다.

- `collectAsStateWithLifecycle`로 `UiState` 수집
- `UiEvent`를 한 곳에서 수집하고 UI 동작으로 변환
- `viewModel::handleAction`을 Screen의 `onAction`에 연결
- 시스템 뒤로 가기와 뒤로 가기 제스처를 `NavigationBackClicked` Action으로 변환
- Navigation Layer에서 받은 화면 이동 함수를 `UiEvent` 처리에 연결

```kotlin
@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        viewModel.handleAction(LoginUiAction.NavigationBackClicked)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                LoginUiEvent.NavigateBack -> navigateBack()
                LoginUiEvent.NavigateToHome -> navigateToHome()
                is LoginUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction,
    )
}
```

Route는 화면별 상태를 다시 가공하거나 비즈니스 로직을 수행하지 않는다. 이러한 처리는 ViewModel이 담당한다.

## Screen 작성 규칙

Screen은 ViewModel과 Navigation 구현을 알지 못하며 전달받은 상태를 렌더링하고 사용자 입력을 상위 계층으로 전달한다.

```kotlin
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (LoginUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LoginContent(
            email = uiState.email,
            password = uiState.password,
            isLoginEnabled = uiState.isLoginEnabled,
            isLoading = uiState.isLoading,
            onEmailChange = {
                onAction(LoginUiAction.EmailChanged(it))
            },
            onPasswordChange = {
                onAction(LoginUiAction.PasswordChanged(it))
            },
            onLoginClick = {
                onAction(LoginUiAction.LoginClicked)
            },
            onBackClick = {
                onAction(LoginUiAction.NavigationBackClicked)
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
```

Screen에는 다음 객체나 동작을 포함하지 않는다.

- ViewModel 직접 참조
- `NavController` 또는 `NavHostController`
- Repository 또는 UseCase 호출
- `UiEvent` 수집
- Composition 중 상태 변경이나 화면 이동 실행

이 구조를 사용하면 Screen을 독립적으로 Preview하고 상태와 Callback만으로 테스트할 수 있다.

## Callback과 Action 경계

계층별 함수 이름은 다음과 같이 구분한다.

| 위치 | 형식 | 예시 |
|---|---|---|
| 재사용 컴포넌트 | 역할이 드러나는 Callback | `onClick`, `onValueChange` |
| Feature Screen | Action 전달 | `onAction` |
| ViewModel | Action 처리 진입점 | `handleAction` |
| Route와 NavGraph | 실행할 화면 이동 | `navigateBack`, `navigateToHome` |

Feature Screen에서 발생한 사용자 입력은 Navigation 요청을 포함해 `UiAction`으로 변환한다. 상단 버튼, 시스템 뒤로 가기, 뒤로 가기 제스처는 ViewModel의 판단 필요 여부와 관계없이 모두 `NavigationBackClicked`로 전달한다.

```kotlin
onAction(LanguageUiAction.LanguageSelected(language))
onAction(LoginUiAction.NavigationBackClicked)
```

ViewModel은 Navigation Action을 처리한 뒤 `NavigateBack`과 같은 `UiEvent`를 Route에 전달한다. Route는 Event를 실제 화면 이동 함수로 변환한다.

## Navigation 경계

`NavController`는 NavGraph가 소유한다. NavGraph는 실제 화면 이동 함수를 Route에 전달하고 Route는 ViewModel의 `UiEvent` 처리와 연결한다.

```kotlin
composable<LoginDestination> {
    LoginRoute(
        navigateToHome = { navController.navigate(HomeDestination) },
        navigateBack = navController::navigateUp,
    )
}
```

Screen과 재사용 컴포넌트는 Navigation 목적지와 백 스택 구조를 알지 못한다.

## 로컬 UI 상태

화면을 다시 구성하거나 상태를 다시 수집할 때 복원되어야 하는 값은 `UiState`로 관리한다.

Compose 내부에는 다음과 같은 일시적인 표현 상태만 둘 수 있다.

- 포커스 여부
- 애니메이션 진행 상태
- ViewModel 판단이 필요 없는 펼침 또는 접힘 상태
- 사용자가 입력 중인 값과 무관한 컴포넌트 내부 상태

단순한 표현 상태는 `remember`, 구성 변경 후에도 유지해야 하는 순수 UI 상태는 필요한 경우 `rememberSaveable`을 사용한다.

입력값, 로딩, 다이얼로그, 선택 결과처럼 화면 동작에 영향을 주는 값은 로컬 상태와 `UiState`에 중복 저장하지 않는다.
