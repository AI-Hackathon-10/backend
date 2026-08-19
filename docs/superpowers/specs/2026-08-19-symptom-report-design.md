# 증상 기록 문서화 설계

## 목표

사용자가 선택한 증상·증상 시작 시각·메모와, 복용을 확인한 약의 정보 및 복용 시각을 하나의 리포트로 저장한다. 프론트엔드는 리포트 화면을 PNG로 렌더링해 S3에 직접 업로드하고, Spring Boot는 presigned URL 발급·업로드 완료 확인·마이페이지 조회를 담당한다.

## 확정된 흐름

1. 알약 판별 흐름이 약 이름(`itemName`)과 낱알 이미지 URL(`itemImage`)을 제공한다.
2. 사용자가 복용 확인 모달에서 확정한 시각이 복용 시각이 된다.
3. 프론트엔드는 증상·복용 데이터를 포함해 리포트를 생성한다.
4. Spring Boot는 리포트와 데이터 스냅샷을 MySQL에 저장한다.
5. 프론트엔드는 발급받은 PUT presigned URL로 문서 PNG를 S3에 업로드한다.
6. Spring Boot는 S3 `HeadObject`로 업로드를 확인하고 리포트를 `UPLOADED` 상태로 변경한다.
7. 마이페이지는 리포트 카드와 짧은 만료 시간의 GET presigned URL을 조회한다.

## 모듈 경계

- `report`: 리포트, 증상 목록, 복용 약 정보 스냅샷, S3 스냅샷 상태를 소유한다.
- `medication`: 향후 알약 판별과 `drug bol`을 소유한다. 리포트 생성 시 필요한 `drugBolId`, `drugName`, `drugImageUrl`, `takenAt`을 제공한다.
- `user`: 향후 로그인 사용자 식별과 이름을 소유한다. 리포트 생성 시 필요한 `userId`, `userName`을 제공한다.
- `config`: AWS S3 클라이언트·presigner 설정을 소유한다.

현재 저장소에는 `user`, `medication`, JWT 구현이 없으므로 MVP API는 리포트 생성 요청에서 사용자와 복용 데이터의 스냅샷을 받는다. 사용자·복용 모듈이 병합되면 컨트롤러가 SecurityContext와 `drug bol` 조회 결과로 같은 서비스를 호출하도록 바꾼다. 리포트 엔티티와 S3 저장 방식은 바꾸지 않는다.

## 데이터 모델

`Report`

- `id`: 리포트 식별자
- `userId`, `userName`: 문서 작성 시점 사용자 스냅샷
- `symptoms`: `SymptomType` enum 목록; 별도 `report_symptoms` 테이블에 enum 이름으로 저장
- `symptomStartedAt`: UTC `Instant`
- `timezoneId`: 문서 표시용 IANA 시간대, 기본 `Asia/Seoul`
- `memo`: 최대 200자
- `drugBolId`, `drugName`, `drugImageUrl`, `takenAt`: 복용 확인 시점의 데이터 스냅샷
- `snapshotStatus`: `PENDING` 또는 `UPLOADED`
- `snapshotObjectKey`, `snapshotFileName`, `snapshotContentLength`: S3 업로드가 완료된 뒤 저장
- `createdAt`, `updatedAt`

현재 시나리오는 한 리포트당 약 1건이다. 여러 약 복용이 필요한 경우 `ReportMedication` 자식 엔티티로 확장한다.

`SymptomType`은 프론트의 고정 증상 선택지와 정확히 일치한다.

- `HEADACHE` (두통), `FEVER` (발열), `COUGH` (기침), `SORE_THROAT` (인후통), `RUNNY_NOSE` (콧물)
- `NASAL_CONGESTION` (코막힘), `ABDOMINAL_PAIN` (복통), `INDIGESTION` (소화불량), `DIARRHEA` (설사), `CONSTIPATION` (변비)
- `HEARTBURN` (속쓰림), `NAUSEA_OR_VOMITING` (구토/메스꺼움), `MUSCLE_PAIN` (근육통), `MENSTRUAL_CRAMPS` (생리통), `TOOTHACHE` (치통)
- `ALLERGY` (알레르기), `ITCHY_SKIN` (피부 가려움), `BODY_ACHES` (몸살), `DIZZINESS` (어지러움), `CHILLS` (오한)

JPA에는 `EnumType.STRING`으로 안정적인 enum 이름을 저장한다. JSON API는 프론트 호환성을 위해 한글 표시명을 수신·응답한다.

## API

- `POST /api/v1/reports`: 리포트를 생성한다.
- `POST /api/v1/reports/{reportId}/snapshot-upload-url`: `image/png` PUT presigned URL을 발급한다.
- `POST /api/v1/reports/{reportId}/snapshot-upload-complete`: S3 업로드를 확인하고 상태를 `UPLOADED`로 전환한다.
- `GET /api/v1/reports/{reportId}`: 리포트 상세를 반환한다.
- `GET /api/v1/users/{userId}/reports`: 마이페이지 리포트 카드 목록을 최신순으로 반환한다. 업로드된 카드에는 GET presigned URL을 포함한다.

모든 응답은 `ApiResponse<T>`의 `result`에 담는다. 엔티티는 API 응답으로 직접 반환하지 않는다.

## S3 규칙

- 파일명: `{안전한사용자명}_{reportId}.png`
- object key: `{reportPrefix}/{userId}/{fileName}`
- `reportPrefix` 기본값: `reports`
- 업로드·조회 URL 만료 기본값: 10분
- 허용 Content-Type: `image/png`
- 최대 크기: 10 MiB
- 버킷은 private이며 공개 URL을 DB에 저장하지 않는다.
- 리포트 소유자만 업로드 URL·조회 URL을 받아야 한다. 현재 MVP의 `userId` 입력은 JWT 연동 시 SecurityContext 값으로 대체한다.

## 설정 및 운영 인계

애플리케이션은 기본 AWS 자격 증명 체인을 사용한다. EC2 배포에서는 Access Key를 파일에 넣지 않고 IAM Role을 사용한다.

필요한 환경변수:

- `AWS_REGION`
- `AWS_S3_BUCKET`
- `AWS_S3_REPORT_PREFIX` (선택, 기본 `reports`)
- `AWS_S3_PRESIGNED_URL_EXPIRATION_SECONDS` (선택, 기본 `600`)

Spring Boot 애플리케이션에는 별도 CORS 설정을 두지 않는다. 다만 프론트가 브라우저에서 presigned URL로 S3에 직접 PNG를 업로드하므로, S3 버킷에는 프론트 Origin과 `PUT`·`GET`·`HEAD`, `Content-Type` 헤더를 허용하는 CORS 규칙이 필요하다. Docker Compose, EC2, JWT, MySQL 운영 설정은 인프라 담당자가 기존 환경변수 계약에 맞춰 적용한다.

## 검증 및 오류

- 증상은 `SymptomType`의 고정값 중 1개 이상 20개 이하만 허용한다.
- 사용자명·약 이름은 비어 있을 수 없고, 메모는 최대 200자다.
- 증상 시작·복용 시각은 미래일 수 없다.
- `timezoneId`는 유효한 IANA 시간대여야 한다.
- 업로드 완료 시 S3 객체가 없거나 PNG가 아니거나 0바이트이거나 10 MiB를 초과하면 실패한다.
- 존재하지 않는 리포트는 404를 반환한다.

## 프론트 연동 주의사항

프론트가 `itemImage`를 포함한 DOM을 PNG로 캡처할 때 원본 이미지 서버가 CORS 헤더를 제공하지 않으면 캔버스가 오염되어 캡처가 실패할 수 있다. 이 경우 `medication` 모듈이 이미지 파일을 S3로 복사하거나, 이미지 제공처의 CORS 허용 여부를 확인해야 한다. 본 구현은 CORS가 허용된 `drugImageUrl`을 입력받는 것으로 가정한다.
