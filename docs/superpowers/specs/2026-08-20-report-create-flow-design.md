# 증상 문서 구현 설계

## 목표

로그인한 사용자가 입력한 증상 기록과 복용 확정된 약 기록을 바탕으로 백엔드가 증상 문서 텍스트를 생성하고, 해당 리포트의 PNG 스냅샷을 S3에 직접 업로드·조회할 수 있는 백엔드 흐름을 제공한다.

## 범위

이번 작업은 백엔드만 포함한다. 프론트엔드는 이후 각 API를 호출하고 문서 UI를 렌더링한다.

## 데이터 흐름

```text
POST /api/symptoms/records
  -> SymptomRecord 저장

POST /api/medications/upload/presigned-url
  -> MedicationEntity 생성 및 이미지 업로드 URL 발급

POST /api/medications/identify
  -> AI 결과로 drugName 갱신

POST /api/medications/{medicationId}/intake
  -> MedicationEntity.isTaken=true, takenAt 저장

POST /api/reports
  -> 소유권·복용 확정 검증
  -> 사용자/증상/약 데이터를 조합해 summary 생성
  -> Report 저장

POST /api/reports/{reportId}/snapshot/presigned-url
  -> S3 Object Key 저장 및 PNG 업로드 URL 발급

S3 직접 업로드

POST /api/reports/{reportId}/snapshot/complete
  -> snapshotStatus=UPLOADED

GET /api/reports/{reportId}/snapshot-url
  -> 조회용 Presigned GET URL 발급
```

## 증상 선택 제한

증상은 최대 10개까지 선택한다. 요청 DTO의 Bean Validation과 도메인 엔티티의 도메인 검증을 모두 10개 기준으로 맞춘다. 중복 증상은 기존처럼 거부한다.

## 복용 확정

`MedicationEntity`를 리포트가 참조하는 복용 원장으로 사용한다. `POST /api/medications/{medicationId}/intake`는 로그인 사용자의 소유 약만 갱신하고 `markAsTaken()`을 호출한다. `MedicationIntake` 엔티티는 이번 흐름에서 사용하지 않는다.

복용 확정 API는 반복 호출되어도 이미 저장된 `takenAt`을 덮어쓰지 않는 멱등 동작으로 구현한다. 이미 복용 확정된 약을 다시 요청하면 기존 복용 시각을 반환한다.

## 리포트 생성

요청 DTO는 프론트가 만든 문서 텍스트를 받지 않고 식별자만 받는다.

```json
{
  "symptomRecordId": 1,
  "medicationId": 2
}
```

서비스는 다음을 검증한다.

1. 로그인 사용자가 존재하는지 확인한다.
2. 증상 기록과 약 기록이 로그인 사용자의 소유인지 확인한다.
3. 약 기록의 `isTaken=true` 및 `takenAt != null`을 확인한다.
4. 검증을 통과한 데이터를 사용해 summary를 생성하고 Report를 저장한다.

summary는 다음 정보를 포함하는 일반 텍스트로 생성한다.

```text
사용자: {사용자 이름}
증상: {증상명 목록}
증상 시작 시각: {startedAt}
메모: {메모 또는 입력 없음}
복용 약품: {drugName 또는 판별되지 않음}
복용 시각: {takenAt}
앞면 이미지: {frontImageObjectKey 또는 없음}
뒷면 이미지: {backImageObjectKey 또는 없음}
```

리포트는 하나의 `MedicationEntity`와만 연결하며, `Report.medication`의 unique 외래 키 제약을 유지한다.

## 스냅샷 S3

스냅샷은 PNG 하나로 고정하고 Object Key는 다음 규칙을 사용한다.

```text
reports/{userId}/{reportId}/snapshot.png
```

Presigned PUT URL 발급 시 리포트 소유권을 확인하고 Object Key를 Report에 저장한다. 이 시점에는 아직 클라이언트 업로드가 끝나지 않았으므로 상태는 `PENDING`으로 유지한다. 클라이언트가 S3 업로드 성공 후 완료 API를 호출하면 Object Key 존재 여부를 확인하고 `UPLOADED`로 변경한다.

조회용 URL은 `UPLOADED` 상태이며 Object Key가 있는 리포트에 대해서만 발급한다. 모든 리포트 조회와 스냅샷 URL 발급은 로그인 사용자의 소유권을 확인한다.

## 오류 처리

- 존재하지 않는 사용자·증상 기록·약 기록: 기존 공통 예외 코드 체계를 따른다.
- 다른 사용자의 증상 기록·약 기록·리포트 접근: 리소스가 없는 것과 동일하게 거부한다.
- 복용 확정 전 리포트 생성: 별도 결과 코드로 거부한다.
- 스냅샷 Object Key가 없거나 업로드 완료 전 조회: 별도 결과 코드로 거부한다.
- Presigned URL의 만료 시간은 기존 약 이미지 업로드 URL과 동일한 5분을 사용한다.

## 테스트 기준

- 증상 10개 요청 성공, 11개 요청 실패
- 복용 확정 API의 소유권 검증과 `takenAt` 저장
- 복용 확정 전 리포트 생성 거부
- 리포트 생성 시 summary 각 항목 조합
- 약 이름이 null일 때 `판별되지 않음` 처리
- 스냅샷 URL 발급·완료·조회 시 소유권 및 상태 검증
- 전체 Gradle 테스트와 애플리케이션 컨텍스트 로딩
