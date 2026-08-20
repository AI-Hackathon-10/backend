# 증상 문서 생성 흐름 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 사용자가 알약 판별 결과에서 `그래도 기록하기`를 누르면 복용 시각과 증상 기록을 바탕으로 리포트를 생성하고, 이후 로그인한 사용자가 문서 상세 정보를 조회할 수 있도록 한다. 스냅샷은 S3 업로드용 Presigned URL을 제공하며, 최종 디바이스 저장은 프론트엔드의 브라우저 다운로드로 처리한다.

**Architecture:** 증상 기록과 알약 복용 기록은 각각 기존 도메인 엔티티에서 조회한다. 복용 확인 API가 `MedicationEntity`를 `isTaken=true`, `takenAt`으로 확정하고, 리포트 생성 API는 두 ID만 받아 사용자 소유권과 복용 완료 여부를 검증한 뒤 백엔드에서 `summary`를 조합해 저장한다. 리포트 상세 조회는 조합된 문서 텍스트와 증상·약 이미지 키를 반환한다. 스냅샷은 리포트별 고정 S3 object key를 사용하고 `PENDING → UPLOADED` 상태로 관리한다.

**Tech Stack:** Java 17, Spring Boot, Spring MVC, Spring Data JPA, MySQL, AWS SDK v2 S3 Presigner, JUnit 5, MockMvc, Mockito

**Spec:** `docs/superpowers/specs/2026-08-20-report-create-flow-design.md`

## Global Constraints

- 작업 브랜치 `feat/#50-report-create-flow`에서만 작업한다.
- `main`, `develop`, `.env`, `.env.local`, `.DS_Store`는 수정하거나 커밋하지 않는다.
- 프론트엔드 코드는 수정하지 않는다. 디바이스 저장 버튼은 추후 프론트가 상세 조회 응답을 화면에 렌더링한 뒤 `Blob` 다운로드로 연결한다.
- 기존 인증 방식인 `SecurityContextUtil.extractUserId(HttpServletRequest)`를 사용한다.
- 한 리포트에는 약 하나만 연결한다. 기존 `Report.medication`의 unique 관계를 유지한다.
- 리포트 생성 요청은 `symptomRecordId`, `medicationId`만 받으며 `summary`는 서버에서 생성한다.

## Task 1: 증상 선택 최대 10개 제한

**Files:**
- Modify: `src/main/java/com/ktbaihackathon/symptom/dto/SymptomRecordCreateRequest.java`
- Modify: `src/main/java/com/ktbaihackathon/symptom/entity/SymptomRecord.java`
- Modify: `src/test/java/com/ktbaihackathon/symptom/controller/SymptomRecordControllerTest.java`

- [ ] 테스트에서 10개 증상 요청은 허용되고 11개 요청은 거절되는지 검증한다.
- [ ] 요청 DTO의 Bean Validation 최대값을 10으로 변경한다.
- [ ] 엔티티의 도메인 검증 최대값을 10으로 변경한다.
- [ ] 증상 관련 테스트를 실행한다.
- [ ] Commit: `fix: 증상 선택 최대 10개로 통일`

## Task 2: 복용 확인 API

**Files:**
- Add: `src/main/java/com/ktbaihackathon/medication/dto/MedicationIntakeResponse.java`
- Add: `src/main/java/com/ktbaihackathon/medication/service/MedicationIntakeService.java`
- Add: `src/main/java/com/ktbaihackathon/medication/controller/MedicationIntakeController.java`
- Modify: `src/main/java/com/ktbaihackathon/medication/entity/MedicationEntity.java`
- Modify: `src/main/java/com/ktbaihackathon/medication/repository/MedicationRepository.java` (필요한 경우에만)
- Modify: `src/main/java/com/ktbaihackathon/common/response/ResultCode.java`
- Add/Modify: medication controller/service tests

- [ ] 인증 사용자가 자신의 약 기록에 대해서만 복용 확인할 수 있는 실패 테스트를 먼저 작성한다.
- [ ] `POST /api/medications/{medicationId}/intake`를 추가한다.
- [ ] 서비스에서 소유권을 검증하고 `MedicationEntity.markAsTaken()`을 호출한다.
- [ ] 이미 복용 확정된 기록을 재호출해도 최초 `takenAt`을 보존하도록 멱등 처리한다.
- [ ] 약 ID, 약 이름, 복용 시각을 응답한다.
- [ ] 테스트를 실행한다.
- [ ] Commit: `feat: 약 복용 확인 API 구현`

## Task 3: 리포트 생성 시 서버 측 문서 조합

**Files:**
- Modify: `src/main/java/com/ktbaihackathon/report/dto/ReportCreateRequest.java`
- Modify: `src/main/java/com/ktbaihackathon/report/service/ReportService.java`
- Modify: `src/main/java/com/ktbaihackathon/common/response/ResultCode.java`
- Modify: `src/test/java/com/ktbaihackathon/report/controller/ReportControllerTest.java`
- Add/Modify: report service tests

- [ ] `summary` 없이 두 ID만 보내는 생성 요청 계약으로 테스트를 수정한다.
- [ ] 증상 기록과 약 기록의 사용자 소유권을 각각 검증한다.
- [ ] 약 기록이 `isTaken=true`이고 `takenAt`이 존재하지 않으면 생성하지 않는 테스트를 추가한다.
- [ ] 사용자 이름, 증상 목록, 증상 시작 시각, 메모, 약 이름, 복용 시각, 앞·뒷면 object key를 서버에서 조합한다.
- [ ] null 메모는 `입력 없음`, null 약 이름은 `판별되지 않음`, null 이미지 키는 `없음`으로 표현한다.
- [ ] 조합한 `summary`로 `Report`를 저장하고 기존 목록·상세 응답을 유지한다.
- [ ] 리포트 생성 관련 테스트를 실행한다.
- [ ] Commit: `feat: 복용 기록 기반 증상 문서 생성 구현`

## Task 4: 리포트 스냅샷 Presigned URL 흐름

**Files:**
- Add: `src/main/java/com/ktbaihackathon/report/dto/ReportSnapshotPresignedUrlResponse.java`
- Add: `src/main/java/com/ktbaihackathon/report/dto/ReportSnapshotUrlResponse.java`
- Add: `src/main/java/com/ktbaihackathon/report/service/ReportSnapshotService.java`
- Modify: `src/main/java/com/ktbaihackathon/report/controller/ReportController.java`
- Modify: `src/main/java/com/ktbaihackathon/report/entity/Report.java` (상태 전이 검증이 필요한 경우에만)
- Modify: `src/main/java/com/ktbaihackathon/common/response/ResultCode.java`
- Add/Modify: report snapshot controller/service tests

- [ ] 사용자 소유 리포트만 스냅샷 작업을 수행할 수 있는지 테스트한다.
- [ ] `POST /api/reports/{reportId}/snapshot/presigned-url`에서 `reports/{userId}/{reportId}/snapshot.png` PUT URL과 object key를 반환한다.
- [ ] Presigned URL 발급 시 리포트의 object key를 저장하고 상태를 `PENDING`으로 둔다.
- [ ] `POST /api/reports/{reportId}/snapshot/complete`에서 업로드 완료 상태를 `UPLOADED`로 변경한다.
- [ ] `GET /api/reports/{reportId}/snapshot-url`에서 `UPLOADED` 리포트에 한해 GET URL을 반환한다.
- [ ] 아직 업로드되지 않은 리포트, 타 사용자 리포트, 없는 리포트의 오류를 검증한다.
- [ ] 스냅샷 관련 테스트를 실행한다.
- [ ] Commit: `feat: 리포트 스냅샷 Presigned URL 흐름 구현`

## Task 5: 통합 검증 및 커밋 상태 정리

- [ ] `./gradlew test`를 실행한다.
- [ ] `./gradlew build`를 실행한다.
- [ ] `git diff --check`를 실행한다.
- [ ] `git status --short`로 `.env*`, `.DS_Store`, `.gitignore` 변경이 커밋에 포함되지 않았는지 확인한다.
- [ ] 각 작업 커밋이 하나의 기능 단위인지 확인한다.
- [ ] 최종적으로 원격 저장소에는 push하지 않고 로컬 커밋만 남긴다.

## API 흐름 요약

```text
알약 이미지 업로드/판별
  → 사용자가 "그래도 기록하기" 클릭
  → POST /api/medications/{medicationId}/intake
  → POST /api/reports { symptomRecordId, medicationId }
  → GET /api/reports
  → GET /api/reports/{reportId}
  → POST /api/reports/{reportId}/snapshot/presigned-url
  → 프론트가 문서 UI를 PNG로 만들어 S3 PUT 업로드
  → POST /api/reports/{reportId}/snapshot/complete
  → 상세 화면의 "저장하기" 버튼으로 디바이스 다운로드
```
