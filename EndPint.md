POST /api/batch/google-places/run - 모든 테이블의 Google Places 데이터 업데이트
POST /api/batch/google-places/run/attractions - 관광지만 업데이트
POST /api/batch/google-places/run/restaurants - 음식점만 업데이트
POST /api/batch/google-places/run/accommodations - 숙박시설만 업데이트
POST /api/batch/google-places/run/festivals - 축제/이벤트만 업데이트
GET /api/batch/google-places/status/{jobExecutionId} - 작업 상태 조회
GET /api/batch/google-places/summary - 전체 Google Places 데이터 현황 조회


### 1. 모든 테이블 Google Places 데이터 업데이트

```bash
curl -X POST http://localhost:8081/api/batch/google-places/run \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

### 2. 관광지만 업데이트

```bash
curl -X POST http://localhost:8081/api/batch/google-places/run/attractions \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

### 3. 음식점만 업데이트

```bash
curl -X POST http://localhost:8081/api/batch/google-places/run/restaurants \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

### 4. 숙박시설만 업데이트

```bash
curl -X POST http://localhost:8081/api/batch/google-places/run/accommodations \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

### 5. 축제/이벤트만 업데이트

```bash
curl -X POST http://localhost:8081/api/batch/google-places/run/festivals \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

### 6. 작업 상태 조회

```bash
# jobExecutionId를 실제 값으로 변경하세요 (예: 123)
curl -X GET http://localhost:8081/api/batch/google-places/status/123 \
  -H "Accept: application/json"
```

### 7. 전체 Google Places 데이터 현황 조회

```bash
curl -X GET http://localhost:8081/api/batch/google-places/summary \
  -H "Accept: application/json"
```

### 응답 결과 예시

### 작업 시작 성공 시:
```json
{
  "success": true,
  "jobExecutionId": 123,
  "status": "STARTED",
  "tableType": "attractions",
  "startTime": "2024-01-15T10:30:00",
  "apiCallsRemaining": 1450
}
```

### 상태 조회 응답:
```json
{
  "success": true,
  "jobExecutionId": 123,
  "status": "COMPLETED",
  "startTime": "2024-01-15T10:30:00",
  "endTime": "2024-01-15T10:35:00",
  "exitStatus": "COMPLETED",
  "steps": [
    {
      "stepName": "enrichTouristAttractionsStep",
      "status": "COMPLETED",
      "readCount": 100,
      "writeCount": 85,
      "skipCount": 0,
      "successRate": 85.0,
      "tableType": "attractions"
    }
  ]
}
```

### 데이터 현황 조회 응답:
```json
{
  "success": true,
  "attractions": {
    "totalCount": 500,
    "enrichedCount": 420,
    "pendingCount": 80,
    "averageRating": 4.2,
    "enrichmentProgress": 84.0
  },
  "restaurants": {
    "totalCount": 300,
    "enrichedCount": 250,
    "pendingCount": 50,
    "averageRating": 4.1,
    "enrichmentProgress": 83.3
  }
}
```