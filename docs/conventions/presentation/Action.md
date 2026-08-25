# UiAction Convention

## 역할

`UiAction`은 UI에서 ViewModel로 전달되는 사용자 입력을 표현한다.
상태 변경, 유효성 검사, 데이터 조회, 비즈니스 로직 수행 또는 Navigation 요청이 필요한 입력을 Action으로 정의한다.

```text
UI -- UiAction --> ViewModel -- UiState --> UI
```

## 작성 규칙

화면별 `sealed interface`로 정의하고 사용자에게 이미 발생한 행동을 과거형으로 표현한다.

```kotlin
sealed interface LoginUiAction {
    data class EmailChanged(
        val value: String,
    ) : LoginUiAction

    data class PasswordChanged(
        val value: String,
    ) : LoginUiAction

    data object LoginClicked : LoginUiAction
    data object NavigationBackClicked : LoginUiAction
}
```

권장 네이밍:

- `LoginClicked`
- `RefreshClicked`
- `EmailChanged`
- `ItemSelected`
- `SearchSubmitted`
- `NavigationBackClicked`

다음과 같이 처리 방법이나 결과를 이름에 넣지 않는다.

```kotlin
// 지양
data object HandleLogin : LoginUiAction
data object RequestLoginApi : LoginUiAction
data object NavigateToHome : LoginUiAction
```

## ViewModel 진입점

ViewModel의 Action 진입점은 `handleAction`으로 통일한다.

```kotlin
fun handleAction(action: LoginUiAction) {
    when (action) {
        is LoginUiAction.EmailChanged -> updateEmail(action.value)
        is LoginUiAction.PasswordChanged -> updatePassword(action.value)
        LoginUiAction.LoginClicked -> login()
        LoginUiAction.NavigationBackClicked -> navigateBack()
    }
}
```

Screen은 구체적인 ViewModel에 의존하지 않고 `onAction`을 전달받는다.

```kotlin
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onAction: (LoginUiAction) -> Unit,
) {
    // UI rendering
}
```

재사용 가능한 하위 Composable은 `UiAction` 타입에 결합하지 않고 역할이 드러나는 Callback을 사용한다.

```kotlin
LoginButton(
    onClick = { onAction(LoginUiAction.LoginClicked) },
)
```

## Action을 사용하지 않는 입력

ViewModel의 판단이 필요 없는 순수 UI 동작은 Action을 거치지 않아도 된다.

예시:
- 로컬 UI의 펼침 또는 접힘처럼 화면 상태로 보존할 필요가 없는 동작

Navigation 요청은 이 예외에 포함하지 않는다. 상단 버튼, 시스템 뒤로 가기, 뒤로 가기 제스처와 화면 닫기를 포함한 모든 Navigation 입력은 UI 경계에서 `UiAction`으로 변환한다.

로그인 성공 후 이동처럼 ViewModel의 처리 결과에 따라 실행되는 동작은 [Event 규칙](./Event.md)을 따른다.

## 선택 기준

다음 질문 중 하나라도 `예`라면 `UiAction`으로 정의한다.

1. `UiState`를 변경하는가?
2. ViewModel의 검증이나 판단이 필요한가?
3. UseCase 또는 Repository 호출의 시작점인가?
4. 처리 성공이나 실패에 따라 후속 결과가 달라지는가?
5. Navigation 또는 화면 종료를 요청하는가?

모두 `아니요`라면 명시적인 Callback이 더 단순한지 먼저 검토한다.
