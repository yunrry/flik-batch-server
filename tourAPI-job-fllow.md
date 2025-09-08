**docs:** flik-batch-server 구현 가이드

## 구현 목표

### 데이터 수집 플로우
1. `/areaBasedList2` 호출 → 컨텐츠 기본정보 수집
2. `/detailIntro2` 호출 → 컨텐츠별 상세정보 보완

### 핵심 요구사항
- **정렬**: `arrange=R` (생성일순)
- **페이지네이션**: 중단지점 저장 후 다음날 이어서 수집
- **타입별 저장**: content_type_id로 적절한 테이블 분기
- **상세정보 매핑**: Python 코드 로직 참조

## 배치 Job 설계

### 1. TourismDataBatchJob
```java
@Bean
public Job tourismDataJob() {
    return jobBuilderFactory.get("tourismDataJob")
        .start(areaBasedListStep())
        .next(detailIntroStep())
        .build();
}
```

### 2. Step 구성

#### Step 1: areaBasedListStep
- **Reader**: API 페이징 호출
- **Processor**: 컨텐츠 타입별 데이터 변환
- **Writer**: 타입별 테이블 저장

#### Step 2: detailIntroStep
- **Reader**: 저장된 content_id 조회
- **Processor**: `/detailIntro2` 호출 + 매핑
- **Writer**: 상세정보 업데이트

## API 호출 로직

### areaBasedList2 파라미터
```java
Map<String, Object> params = Map.of(
    "arrange", "R",           // 생성일순
    "numOfRows", 100,
    "pageNo", getLastPageNo() + 1
);
```

### 페이지네이션 상태 관리
```java
// 배치 실행 컨텍스트에 저장
executionContext.putInt("lastPageNo", currentPage);
executionContext.putString("lastProcessedDate", LocalDate.now().toString());
```

## 데이터 매핑 로직

### 공통 컬럼 매핑
```java
// 컨텐츠별 다른 필드명을 공통 컬럼으로 통합
Map<String, List<String>> commonMappings = Map.of(
    "usetime", List.of("usetime", "usetimeculture", "opentimefood"),
    "parking", List.of("parking", "parkingculture", "parkingshopping")
);
```

### 컨텐츠별 특화 필드
- **12(관광지)**: heritage1, heritage2, expguide
- **15(축제)**: sponsor1, eventenddate, eventstartdate
- **39(음식점)**: firstmenu, treatmenu, smoking

## Rate Limiting 전략

### Redis 기반 제한
```java
String key = "tourism-api:" + LocalDate.now();
if (redisTemplate.opsForValue().increment(key) > 1000) {
    throw new RateLimitExceededException();
}
```


## 모니터링 포인트

- API 호출 횟수 추적
- 배치 실행 상태 로깅
- 실패 건수 및 재시도 로직
- 페이지네이션 상태 저장