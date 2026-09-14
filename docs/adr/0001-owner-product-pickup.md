# 점포 관리 모듈 안에서 찜 관리 구현

- 상태: 채택
- 날짜: 2026-09-14

## 배경

Figma의 점포 관리 화면은 등록된 상품과 찜 현황을 탭으로 전환한다. 별도 pickup 모듈은 동일한 진입 화면과 상품 재고 연결을 중복 소유하게 된다.

## 결정

찜 목록은 `:feature:owner:product`의 기존 ProductList에서 표시한다. 상세와 취소 화면은 `screen/pickup` 아래 각각 State, Action, Event, ViewModel, Route, Screen으로 나눈다. 앱은 목적지 연결만 담당한다. 공통 카드·스캐폴드·바텀시트를 재사용한다.

아이콘은 Figma SVG를 변환한 Android VectorDrawable XML을 모듈의 `src/main/res/drawable`에서 관리한다. 디자인 출처와 리소스 대응은 모듈의 `README.md`에 기록한다.

## 현재 한계와 후속 연결

API 연결 전에는 OwnerPickupStore가 화면 간 찜 상태와 할당 재고를 공유한다. Debug에만 예시 데이터와 완료·취소 처리가 있으며 Release는 빈 상태다. 실제 서버 재고 변경과 안내 메시지 전송은 구현하지 않는다. 상품 카탈로그의 재고 수정은 찜 저장소에 반영되지만 로컬 찜 완료 결과를 앱의 상품 카탈로그로 역전파하지 않는다. 실제 상품/찜 Repository 연결 시 서버를 재고의 단일 원본으로 사용해야 한다.

## 참조

- [프로젝트 컨벤션](../conventions/README.md)
- [화면 State](../conventions/presentation/State.md)
- [Compose](../conventions/presentation/Compose.md)
