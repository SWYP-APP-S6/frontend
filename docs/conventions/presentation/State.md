# UiState Convention

> 이 문서는 6주 MVP에서 적용할 MVVM 화면 상태 규칙을 정의하는 초안이다.
> 공통 Overlay 계층은 도입하지 않고 각 화면이 필요한 상태를 직접 소유한다.

## 역할

`UiState`는 현재 화면을 그리는 데 필요한 값의 단일 진실 공급원이다.
화면이 다시 구성되거나 상태를 다시 수집해도 복원되어야 하는 값은 `UiState`로 표현한다.

다음 값이 여기에 해당한다.

- 서버나 로컬 저장소에서 조회한 화면 데이터
- 입력값과 선택값
- 로딩 여부
- 현재 표시 중인 다이얼로그와 오류 메시지
- 현재 상태만으로 계산할 수 있는 화면 속성

Navigation, Snackbar, 외부 앱 열기처럼 처리 후 복원할 필요가 없는 동작은 [Event 규칙](./Event.md)을 따른다.

## 작성 규칙

`UiState`는 기본값을 가진 `data class`로 정의하고 모든 프로퍼티를 `val`로 선언한다.
상태 변경은 기존 객체를 수정하지 않고 `copy`로 새 상태를 만든다.

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val dialog: LoginDialog? = null,
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginDialog {
    data class Error(
        val message: String,
    ) : LoginDialog
}
```

```kotlin
_uiState.update { state ->
    state.copy(
        email = value,
        dialog = null,
    )
}
```

## 파생 상태

현재 상태만으로 계산할 수 있고 계산 비용이 작은 값은 중복 저장하지 않고 계산 프로퍼티로 표현한다.

```kotlin
data class VersionUiState(
    val currentVersion: String = "",
    val latestVersion: String = "",
) {
    val isUpdateRequired: Boolean
        get() = compareVersion(currentVersion, latestVersion)
}
```

파생 값에 별도 생명주기나 비동기 처리가 필요하면 ViewModel에서 계산한 결과를 상태로 저장한다.

## Collection과 안정성

- 외부에서 변경 가능한 Collection을 상태에 노출하지 않는다.
- 실제 성능 문제를 확인하기 전에는 `@Stable`이나 `@Immutable`로 안정성을 강제하지 않는다.
- Immutable Collection 라이브러리는 프로젝트 의존성으로 채택된 뒤 사용한다.

## 포함하지 않는 값

`UiState`에는 다음 항목을 포함하지 않는다.

- Callback 또는 Lambda
- `@Composable` 함수
- `NavController`와 같은 UI 제어 객체
- Repository, UseCase와 같은 동작 객체
- 처리 후 복원하지 않을 일회성 `UiEvent`

```kotlin
// 지양
data class LoginUiState(
    val email: String = "",
    val onLoginClick: () -> Unit = {},
)
```

## 화면별 상태

화면에 로딩이나 다이얼로그가 필요하면 해당 `UiState`에 명시적으로 추가한다.

```kotlin
data class DeleteAccountUiState(
    val isLoading: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val errorMessage: String? = null,
)
```

여러 화면에서 같은 상태 구조가 실제로 반복되고 변경 이유도 같아질 때만 공통화를 다시 검토한다.
