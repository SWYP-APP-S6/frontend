# 점포 관리 찜 UI 통합 검증

검증일: 2026-09-14. 작업 위치는 기본 체크아웃의 `feat/owner-pickup`이다. 커밋·푸시는 수행하지 않았다.

## 화면 대응

Figma 파일: `hqglQXERCwjFx1W4amjPHI`. 화면은 `:feature:owner:product`에서 구현한다.

| Figma 노드 | 구현 | 실기기 캡처 |
|---|---|---|
| 1172:17266 | 찜 현황 탭, 상태 필터, 신규 표시, 타이머, 완료 액션 | [목록](files/pickup-01-list.png) |
| 1172:17700 | 조건에 맞는 찜 없음 | [목록 빈 상태](files/pickup-02-list-empty.png) |
| 1172:17748 | 찜 상세, 마감 시각, 주문 정보, 총액 | [상세](files/pickup-03-detail.png) |
| 1172:17864 | 완료 시각, 00:00, 비활성 완료 버튼 | [완료](files/pickup-04-detail-completed.png) |
| 1318:26538 | 상품별 취소 대상, 알림 미동의, 체크 선택 | [취소](files/pickup-05-cancellation.png) |
| 1318:26762 | 취소 대상 없음, 비활성 0건 버튼 | [취소 빈 상태](files/pickup-07-cancellation-empty.png) |
| 1318:26713 | 선택 건수, 상품 요약, 안내 미리보기, 확인 시트 | [확인 시트](files/pickup-06-confirmation.png) |

캡처는 SM-F711N / Android 15의 실제 화면이며 테스트 데이터의 날짜·이름·재고를 사용한다. Figma 원본과 기기 종횡비가 다르다. 신규 아이콘은 SVG에서 변환한 VectorDrawable이다. 이 폴더의 PNG는 화면 검증 자료다.

## 실행한 검증

- product/core-designsystem/app `ktlintCheck` 통과.
- product `testDebugUnitTest`: 31개 통과. 재고 할당, 중복 완료 방지, 만료, 취소 처리 및 Release 모의 처리 차단 포함.
- app `testConsumerDebugUnitTest`, `testOwnerDebugUnitTest`: 각 1개 통과.
- app `lintConsumerDebug`, `lintOwnerDebug`, `assembleConsumerDebug`, `assembleOwnerDebug` 통과.
- product `compileReleaseKotlin` 통과.
- product `connectedDebugAndroidTest`에서 `OwnerPickupFlowTest` 2개 통과. 목록→상세→완료→목록 상태 동기화, 필터 빈 상태, 취소 대상 변경, 시트 복귀, 취소 후 빈 상태를 확인했다.
- 화면 캡처를 위해 동일 APK를 직접 계측 실행하여 2개 통과 후 7개 캡처를 확인했다.
- 초기 테스트 실패는 텍스트 기대값(님/뒤로)과 시트의 복수 Compose root 캡처를 수정했다. 마지막 포맷 후 ktlint는 별도 실행으로 다시 확인했다.

## 미연결 및 미실행

실제 주문 API·메시지 전송은 미연결이다. Debug에서만 로컬 예시 상태를 처리하고 Release는 빈 목록이다. 로컬 상태는 프로세스 종료 후 유지하지 않는다. 상품 재고 변경은 찜 재고 할당에 반영하지만 찜 완료에 따른 카탈로그 역방향 동기화는 실제 Repository 연결 범위로 남아 있다.

앱 로그인부터 시작하는 전체 E2E 및 Release 기기 실행은 미실행이다. 실기기 테스트는 product의 실제 Route/ViewModel/Navigation을 테스트 저장소로 구성한다.

## 코드와 규칙

- [모듈 설명](../../../feature/owner/product/README.md)
- [컨벤션](../../conventions/README.md)
- [통합 ADR](../../adr/0001-owner-product-pickup.md)
- [벡터 리소스](../../../feature/owner/product/README.md#찜-관리-아이콘-리소스)

## 만료 상세 추가 검증

Figma `1172:17976`에 맞춰 주문 만료 문구, 주문 만료된 찜 비활성 버튼, 회색 타이머와 홈 버튼을 반영했다. 홈 이동은 활성 상태를 유지한다. [실기기 캡처](files/pickup-08-detail-expired.png)에서 확인했다.

product ktlint·단위 테스트와 Owner Debug 빌드 통과. SM-S937N / Android 16에서 기존 완료·취소 및 신규 만료 흐름 계측 테스트 3개 통과. 초기 복귀 assertion은 같은 점포 관리 문구가 두 곳에 있어 실패했고, 복귀한 주문으로 검증 대상을 수정했다. 전체 Flavor 린트는 이번 수정에서 재실행하지 않았다.
