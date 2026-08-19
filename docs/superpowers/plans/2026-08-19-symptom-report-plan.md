# Symptom Report Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot report feature that records symptom and confirmed medication snapshots, issues S3 presigned URLs for PNG uploads, and returns report cards for the My Page UI.

**Architecture:** `report` owns a `Report` aggregate and receives the user and medication data as immutable creation-time snapshots because the `user` and `medication` modules are not yet present. A `ReportSnapshotStorage` interface hides AWS SDK calls from the service; its S3 adapter issues PUT/GET URLs and validates uploads with `HeadObject`.

**Tech Stack:** Java 17, Spring Boot 4.1, Spring MVC, Spring Data JPA, H2 for tests, MySQL for runtime, AWS SDK v2 S3, JUnit 5, MockMvc.

**Spec:** `backend/docs/superpowers/specs/2026-08-19-symptom-report-design.md`

## Global Constraints

- Keep all application code below `com.ktbaihackathon` and follow the team convention: `report`, `common`, and `config` are feature packages.
- Use PascalCase for classes, camelCase for methods and fields, `Request`/`Response` DTO suffixes, plural REST URLs, and no Entity API responses.
- Store event timestamps as `Instant`; persist and return `timezoneId` for client-side report rendering.
- The report PNG is uploaded directly from the browser to private S3 using a 10-minute presigned URL. Spring Boot never receives the image body.
- Preserve the existing deployment-facing environment variables and use AWS's default credentials provider chain; do not add credentials to source files.
- Do not alter existing frontend files or commit the working tree because it contains unrelated user changes.

---

### Task 1: Make the test context self-contained

**Files:**
- Modify: `backend/build.gradle`
- Modify: `backend/src/test/java/com/ktbaihackathon/KtbAiHackathonApplicationTests.java`
- Create: `backend/src/test/resources/application-test.yaml`

**Interfaces:**
- Produces an H2-backed `test` Spring profile used by all backend tests.

- [ ] **Step 1: Write the failing context test profile assertion**

```java
@SpringBootTest
@ActiveProfiles("test")
class KtbAiHackathonApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

The production change this catches is removal or breakage of the self-contained test datasource configuration.

- [ ] **Step 2: Run the context test to verify it fails**

Run: `./gradlew test --tests com.ktbaihackathon.KtbAiHackathonApplicationTests`

Expected: FAIL because no `application-test.yaml` H2 datasource exists yet.

- [ ] **Step 3: Add the smallest test-only datasource configuration**

Add H2 as `testRuntimeOnly` and create `application-test.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:report-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: ''
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
```

- [ ] **Step 4: Run the context test to verify it passes**

Run: `./gradlew test --tests com.ktbaihackathon.KtbAiHackathonApplicationTests`

Expected: PASS.

### Task 2: Persist and validate the report aggregate

**Files:**
- Create: `backend/src/main/java/com/ktbaihackathon/common/entity/BaseTimeEntity.java`
- Create: `backend/src/main/java/com/ktbaihackathon/config/JpaAuditingConfig.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/entity/Report.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/entity/ReportSnapshotStatus.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/entity/SymptomType.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/repository/ReportRepository.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/exception/InvalidReportRequestException.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/dto/ReportCreateRequest.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/dto/ReportResponse.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/service/ReportService.java`
- Test: `backend/src/test/java/com/ktbaihackathon/report/service/ReportServiceTest.java`

**Interfaces:**
- Consumes: `ReportCreateRequest(userId, userName, List<SymptomType> symptoms, symptomStartedAt, timezoneId, memo, drugBolId, drugName, drugImageUrl, takenAt)`.
- Produces: `ReportResponse` with `snapshotStatus=PENDING` and no S3 key on creation.

- [ ] **Step 1: Write failing service tests for a valid report and rejected invalid report**

```java
@Test
void createsPendingReportFromImmutableSymptomAndMedicationSnapshots() {
    ReportResponse response = reportService.create(validRequest());

    assertThat(response.userName()).isEqualTo("홍길동");
    assertThat(response.symptoms()).containsExactly(SymptomType.HEADACHE, SymptomType.FEVER);
    assertThat(response.drugName()).isEqualTo("타이레놀정");
    assertThat(response.snapshotStatus()).isEqualTo(ReportSnapshotStatus.PENDING);
}

@Test
void rejectsFutureMedicationTime() {
    assertThatThrownBy(() -> reportService.create(requestWithTakenAt(Instant.now().plusSeconds(60))))
        .isInstanceOf(InvalidReportRequestException.class);
}
```

The first test catches a missing persistence/snapshot mapping; the second catches accepting a clinically impossible future medication time.

- [ ] **Step 2: Run the service tests to verify they fail**

Run: `./gradlew test --tests com.ktbaihackathon.report.service.ReportServiceTest`

Expected: FAIL because report classes and service do not exist.

- [ ] **Step 3: Implement the smallest aggregate and creation service**

Implement a `SymptomType` enum containing the 20 frontend symptoms and mapping its Korean display names with Jackson `@JsonCreator`/`@JsonValue`. Implement a `Report` entity with `@ElementCollection List<SymptomType>` and `EnumType.STRING`, `Instant` timestamps, `String timezoneId`, creation-time user/drug snapshots, and `ReportSnapshotStatus.PENDING`. The service must reject an empty/null enum list, blank names, memo over 200 characters, future timestamps, invalid time zone IDs, and non-HTTP(S) image URLs. Add `findByUserIdOrderByCreatedAtDesc` to the repository.

- [ ] **Step 4: Run the service tests to verify they pass**

Run: `./gradlew test --tests com.ktbaihackathon.report.service.ReportServiceTest`

Expected: PASS.

### Task 3: Add S3 presigned URL and upload-completion behavior

**Files:**
- Modify: `backend/build.gradle`
- Modify: `backend/src/main/resources/application.yaml`
- Create: `backend/src/main/java/com/ktbaihackathon/config/s3/S3Properties.java`
- Create: `backend/src/main/java/com/ktbaihackathon/config/s3/S3Config.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/storage/ReportSnapshotStorage.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/storage/S3ReportSnapshotStorage.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/storage/PresignedUpload.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/dto/ReportSnapshotUploadUrlRequest.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/dto/ReportSnapshotUploadUrlResponse.java`
- Modify: `backend/src/main/java/com/ktbaihackathon/report/service/ReportService.java`
- Test: `backend/src/test/java/com/ktbaihackathon/report/service/ReportServiceTest.java`

**Interfaces:**
- Consumes: `ReportSnapshotStorage.createUploadUrl(String objectKey, Duration duration)` and `ReportSnapshotStorage.verifyPng(String objectKey, long maxBytes)`.
- Produces: an image/png PUT URL and an object key ending in `{safeUserName}_{reportId}.png`.

- [ ] **Step 1: Write failing service tests for URL issuance and verified completion**

```java
@Test
void issuesPngUploadUrlWithTheRequiredReportFileName() {
    ReportResponse report = reportService.create(validRequest());

    ReportSnapshotUploadUrlResponse response = reportService.createSnapshotUploadUrl(report.id(), "image/png");

    assertThat(response.fileName()).isEqualTo("홍길동_1.png");
    assertThat(response.objectKey()).isEqualTo("reports/7/홍길동_1.png");
}

@Test
void marksReportUploadedOnlyAfterStorageVerificationSucceeds() {
    ReportResponse report = reportService.create(validRequest());
    reportService.createSnapshotUploadUrl(report.id(), "image/png");

    ReportResponse response = reportService.completeSnapshotUpload(report.id());

    assertThat(response.snapshotStatus()).isEqualTo(ReportSnapshotStatus.UPLOADED);
}
```

Use a test fake of `ReportSnapshotStorage` that returns a literal upload URL and a positive verified object size; do not assert mock invocations.

- [ ] **Step 2: Run the storage-related tests to verify they fail**

Run: `./gradlew test --tests com.ktbaihackathon.report.service.ReportServiceTest`

Expected: FAIL because upload URL and completion behavior do not exist.

- [ ] **Step 3: Implement storage abstraction and AWS adapter**

Add AWS SDK v2 S3 dependencies. Configure `S3Presigner` and `S3Client` from `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_S3_REPORT_PREFIX`, and `AWS_S3_PRESIGNED_URL_EXPIRATION_SECONDS` using the default credential chain. The adapter must issue a PUT request with `Content-Type: image/png`, generate a GET URL for cards, and use `HeadObject` to reject absent, non-PNG, empty, or >10 MiB objects.

- [ ] **Step 4: Run the service tests to verify they pass**

Run: `./gradlew test --tests com.ktbaihackathon.report.service.ReportServiceTest`

Expected: PASS.

### Task 4: Expose REST APIs and error responses

**Files:**
- Create: `backend/src/main/java/com/ktbaihackathon/common/response/ApiResponse.java`
- Create: `backend/src/main/java/com/ktbaihackathon/common/exception/ApiExceptionHandler.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/exception/ReportNotFoundException.java`
- Create: `backend/src/main/java/com/ktbaihackathon/report/controller/ReportController.java`
- Modify: `backend/src/main/java/com/ktbaihackathon/report/service/ReportService.java`
- Test: `backend/src/test/java/com/ktbaihackathon/report/controller/ReportControllerTest.java`

**Interfaces:**
- Produces `POST /api/v1/reports`, `POST /api/v1/reports/{reportId}/snapshot-upload-url`, `POST /api/v1/reports/{reportId}/snapshot-upload-complete`, `GET /api/v1/reports/{reportId}`, and `GET /api/v1/users/{userId}/reports`.
- Each response is `{ "result": ... }`.

- [ ] **Step 1: Write failing MockMvc contract tests**

```java
mockMvc.perform(post("/api/v1/reports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(validCreateJson()))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.result.snapshotStatus").value("PENDING"));

mockMvc.perform(post("/api/v1/reports/1/snapshot-upload-url")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"contentType\":\"image/jpeg\"}"))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.code").value("INVALID_REPORT_REQUEST"));
```

The tests catch endpoint/response-contract regressions and accepting JPEG where the required snapshot format is PNG.

- [ ] **Step 2: Run controller tests to verify they fail**

Run: `./gradlew test --tests com.ktbaihackathon.report.controller.ReportControllerTest`

Expected: FAIL because controller and common response classes do not exist.

- [ ] **Step 3: Implement controller and exception mapping**

Use `@Valid` request bodies, plural paths, and `ResponseEntity` statuses: 201 for creation, 200 for query/upload URL/completion, 400 for invalid input, and 404 for a missing report. Do not expose AWS exceptions or entities in the response.

- [ ] **Step 4: Run controller tests to verify they pass**

Run: `./gradlew test --tests com.ktbaihackathon.report.controller.ReportControllerTest`

Expected: PASS.

### Task 5: Verify the complete backend contract and hand off infrastructure requirements

**Files:**
- Create: `backend/.env.example`
- Modify: `backend/docs/superpowers/specs/2026-08-19-symptom-report-design.md`

**Interfaces:**
- Produces a safe environment-variable template and an explicit S3 bucket CORS handoff.

- [ ] **Step 1: Add the safe environment template**

Include blank values for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`, `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_S3_REPORT_PREFIX`, and `AWS_S3_PRESIGNED_URL_EXPIRATION_SECONDS`. Do not include actual credentials or JWT secret values.

- [ ] **Step 2: Run all backend tests**

Run: `./gradlew test`

Expected: PASS with the H2 `test` profile and no AWS network calls.

- [ ] **Step 3: Review the feature diff and API behavior**

Run: `git status --short backend`

Expected: Only backend report, test, configuration, and documentation files changed. Confirm that no frontend files or secrets are included.

## PR 분할 인계

`backend`는 루트 프론트엔드와 별도 Git 저장소다. 현재 원격에는 `main`만 있고, 팀의 frontend PR #5도 `main`에서 기능 브랜치를 만든 뒤 `main`으로 병합했다. 별도 합의가 있기 전까지는 `main`에서 아래 단위의 브랜치를 만들고, 선행 PR 병합 후 다음 PR을 올린다.

1. `feat/<issue>-report-domain`: `SymptomType`, `Report` aggregate, JPA/repository, 리포트 생성 API와 단위 테스트
2. `feat/<issue>-report-snapshot`: S3 presigned URL, 업로드 완료 확인, 리포트 카드 조회와 통합 테스트
3. `docs/<issue>-report-handoff`: `.env.example`, API/운영 인계 문서와 검증 계획

각 PR에는 해당 범위의 테스트 결과와 관련 이슈만 포함한다. CORS 설정은 Spring Boot에 추가하지 않으며, 브라우저의 S3 직접 업로드에 필요한 버킷 CORS 규칙만 인프라 담당자에게 전달한다.
