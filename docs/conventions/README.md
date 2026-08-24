# Android Convention
현재 문서는 Mangro Android 팀의 코드 컨벤션을 담은 문서 입니다.

## Documents

### Architecture

- [Application Architecture Convention](architecture/ApplicationArchitecture.md)
    - Presentation, 선택적 Domain, Data 레이어 책임
    - 의존 방향과 모듈화 판단 기준
    - `consumer`, `owner` Product Flavor 앱 구성
    - Hilt 기반 의존성 주입 경계

### Presentation

- [Compose UI Convention](presentation/Compose.md)
    - Composable 작성 규칙
    - Screen 구성 규칙
    - Navigation 처리 규칙

- [UiState Convention](presentation/State.md)
    - 화면 렌더링 상태
    - 로딩 및 다이얼로그 상태
    - 파생 상태

- [UiAction Convention](presentation/Action.md)
    - UI에서 ViewModel로 전달되는 입력
    - Action 네이밍과 선택 기준

- [UiEvent Convention](presentation/Event.md)
    - ViewModel에서 UI로 전달되는 일회성 출력
    - Navigation과 Snackbar 처리 기준

- [ViewModel Convention](presentation/ViewModel.md)
    - ViewModel 규칙

각 화면은 필요한 `UiState`, `UiAction`, `UiEvent`를 직접 정의한다.
