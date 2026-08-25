# UiEvent Convention

## 역할

`UiEvent`는 ViewModel의 처리 결과로 UI가 한 번 수행해야 하는 동작을 표현한다.
현재 화면을 그리는 값이 아니며 처리 후 같은 화면에서 복원할 필요가 없는 경우에만 사용한다.

```text
UI -- UiAction --> ViewModel -- UiEvent --> UI 동작
```

대표적인 사용 대상은 다음과 같다.

- 처리 성공 후 화면 이동
- Snackbar 또는 Toast 표시
- 브라우저나 시스템 설정 등 외부 화면 열기
- 화면 종료

## 작성 규칙

화면별 `sealed interface`로 정의하고 UI가 수행할 동작을 `동사 + 대상` 형태로 표현한다.

```kotlin
sealed interface LoginUiEvent {
    data object NavigateToHome : LoginUiEvent
    data object NavigateBack : LoginUiEvent

    data class ShowSnackbar(
        val message: String,
    ) : LoginUiEvent
}
```

권장 네이밍:

- `NavigateToHome`
- `NavigateBack`
- `ShowSnackbar`
- `OpenBrowser`
- `FinishScreen`

Event는 이미 완료된 사실이 아니라 UI에 요청할 동작이다.

뒤로 가기 입력은 `NavigationBackClicked` Action으로 ViewModel에 전달하고, ViewModel은 `NavigateBack` Event를 발행한다. Route는 이 Event를 실제 Navigation 동작으로 변환한다.

```kotlin
// 지양
data object HomeNavigated : LoginUiEvent
data object SnackbarShown : LoginUiEvent
```

## Event로 표현하지 않는 값

다음 값은 화면을 다시 구성하거나 상태를 다시 수집할 때 복원되어야 하므로 [State 규칙](./State.md)을 따른다.

- 로딩 여부
- 화면 데이터와 입력값
- 선택된 탭이나 항목
- 현재 표시 중인 다이얼로그
- 사용자가 확인하기 전까지 유지해야 하는 오류

특히 다이얼로그 표시는 일회성 `ShowDialog` Event가 아니라 `UiState`의 표시 여부나 Dialog 모델로 관리한다.

## 발행과 수집

Event는 단일 소비 흐름으로 노출한다. 구체적인 Flow 구현은 실제 화면 구현 시 결정하되 다음 조건을 지킨다.

- UI에는 읽기 전용 `Flow<UiEvent>`만 노출한다.
- Event 처리기는 Route에 둔다.
- 동일 Event를 State와 Event에 중복 저장하지 않는다.
- Event가 유실되면 화면이 잘못 복원되는 요구사항에는 Event를 사용하지 않는다.

```kotlin
private val _event = Channel<LoginUiEvent>(Channel.BUFFERED)
val event: Flow<LoginUiEvent> = _event.receiveAsFlow()

private fun navigateToHome() {
    viewModelScope.launch {
        _event.send(LoginUiEvent.NavigateToHome)
    }
}
```

Compose에서 Event를 수집하고 처리하는 방식은 [Compose UI Convention](./Compose.md)의 Route 작성 규칙을 따른다.

## 제한

`UiEvent`는 프로세스 종료 후 복원을 보장하지 않는다.
결제 완료 여부, 제출 상태, 사용자가 반드시 확인해야 하는 메시지처럼 유실되면 안 되는 결과는 저장 가능한 도메인 상태나 `UiState`로 모델링한다.
