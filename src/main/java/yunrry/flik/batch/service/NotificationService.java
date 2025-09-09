package yunrry.flik.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${discord.webhook.url:}")
    private String discordWebhookUrl;

    public void sendBatchCompletionAlert(String jobName, long totalCount, String status) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        String message = buildBatchMessage(jobName, totalCount, status);
        sendDiscordMessage(message);
    }

    public void sendEmptyDataAlert(String jobName) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        String message = String.format(
                "⚠️ **배치 작업 알림**\n" +
                        "**작업명**: %s\n" +
                        "**상태**: 수집 데이터 없음\n" +
                        "**시간**: %s\n" +
                        "**메시지**: 더 이상 수집할 새로운 데이터가 없습니다.",
                jobName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendDiscordMessage(message);
    }

    public void sendRateLimitAlert(String jobName, int usedCount) {
        String message = String.format(
                "⚠️ **API 제한 도달**\n" +
                        "**작업명**: %s\n" +
                        "**사용량**: %d/1000건\n" +
                        "**상태**: 내일 자동 재시작 예정\n" +
                        "**시간**: %s",
                jobName, usedCount,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendDiscordMessage(message);
    }

    public void sendGooglePlacesJobStartAlert(JobExecution jobExecution) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        String message = String.format(
                "🔍 **Google Places 데이터 업데이트 시작**\n" +
                        "• Job ID: %d\n" +
                        "• 시작 시간: %s\n" +
                        "• 상태: %s",
                jobExecution.getId(),
                formatDateTime(jobExecution.getStartTime()),
                jobExecution.getStatus()
        );

        sendDiscordMessage(message);
    }

    public void sendGooglePlacesJobCompletionAlert(JobExecution jobExecution) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        boolean isSuccess = jobExecution.getStatus() == BatchStatus.COMPLETED;

        // 전체 통계 계산 - long 타입으로 수정
        long totalRead = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadCount)
                .sum();
        long totalWritten = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();
        long totalSkipped = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getSkipCount)
                .sum();

        String emoji = isSuccess ? "✅" : "❌";
        String status = isSuccess ? "완료" : "실패";

        StringBuilder message = new StringBuilder();
        message.append(String.format("%s **Google Places 데이터 업데이트 %s**\n", emoji, status));
        message.append(String.format("• Job ID: %d\n", jobExecution.getId()));
        message.append(String.format("• 시작: %s\n", formatDateTime(jobExecution.getStartTime())));
        message.append(String.format("• 종료: %s\n", formatDateTime(jobExecution.getEndTime())));
        message.append(String.format("• 소요시간: %s\n", calculateDuration(jobExecution.getStartTime(), jobExecution.getEndTime())));
        message.append(String.format("• 전체 처리: %d건 (성공: %d, 스킵: %d)\n", totalRead, totalWritten, totalSkipped));

        if (isSuccess) {
            message.append(String.format("• 성공률: %.1f%%\n", totalRead > 0 ? (double) totalWritten / totalRead * 100 : 0.0));
        }

        // Step별 상세 결과
        message.append("\n**📊 테이블별 처리 결과:**\n");
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            String tableType = extractTableTypeFromStep(stepExecution.getStepName());
            message.append(String.format("• %s: %d건 처리 → %d건 업데이트 (%.1f%%)\n",
                    tableType,
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getReadCount() > 0 ? (double) stepExecution.getWriteCount() / stepExecution.getReadCount() * 100 : 0.0
            ));
        });

        if (!isSuccess) {
            message.append(String.format("\n❗ **오류**: %s", jobExecution.getExitStatus().getExitDescription()));
        }

        sendDiscordMessage(message.toString());
    }

    public void sendGooglePlacesSingleTableStartAlert(Long jobExecutionId, String tableType) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        String message = String.format(
                "🔍 **%s Google Places 업데이트 시작**\n• Job ID: %d\n• 시작 시간: %s",
                getTableTypeKorean(tableType),
                jobExecutionId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendDiscordMessage(message);
    }

    public void sendGooglePlacesSingleTableCompletionAlert(JobExecution jobExecution, String tableType) {
        if (discordWebhookUrl.isEmpty()) {
            log.warn("Discord webhook URL not configured");
            return;
        }

        boolean isSuccess = jobExecution.getStatus() == BatchStatus.COMPLETED;
        String emoji = isSuccess ? "✅" : "❌";
        String status = isSuccess ? "완료" : "실패";

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        String message = String.format(
                "%s **%s Google Places 업데이트 %s**\n" +
                        "• Job ID: %d\n" +
                        "• 소요시간: %s\n" +
                        "• 처리 결과: %d건 처리 → %d건 업데이트 (%.1f%%)",
                emoji,
                getTableTypeKorean(tableType),
                status,
                jobExecution.getId(),
                calculateDuration(jobExecution.getStartTime(), jobExecution.getEndTime()),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getReadCount() > 0 ?
                        (double) stepExecution.getWriteCount() / stepExecution.getReadCount() * 100 : 0.0
        );

        sendDiscordMessage(message);
    }

    public void sendDiscordMessage(String message) {
        try {
            Map<String, String> payload = Map.of("content", message);

            webClient.post()
                    .uri(discordWebhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Discord notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send Discord notification", e);
        }
    }

    public void sendNotification(String message) {
        sendDiscordMessage(message);
    }

    // 유틸리티 메서드들
    private String buildBatchMessage(String jobName, long totalCount, String status) {
        String emoji = status.equals("COMPLETED") ? "✅" : "❌";

        return String.format(
                "%s **배치 작업 완료**\n" +
                        "**작업명**: %s\n" +
                        "**상태**: %s\n" +
                        "**수집 건수**: %d건\n" +
                        "**완료 시간**: %s",
                emoji,
                jobName,
                status,
                totalCount,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    // Date 타입 매개변수로 수정
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";

        java.time.Duration duration = java.time.Duration.between(start, end);
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;

        return String.format("%d분 %d초", minutes, seconds);
    }

    private String extractTableTypeFromStep(String stepName) {
        if (stepName.contains("TouristAttractions")) return "관광지";
        if (stepName.contains("Restaurants")) return "음식점";
        if (stepName.contains("Accommodations")) return "숙박시설";
        if (stepName.contains("CulturalFacilities")) return "문화시설";
        if (stepName.contains("LeisureSports")) return "레포츠";
        if (stepName.contains("Shopping")) return "쇼핑";
        return stepName;
    }

    private String getTableTypeKorean(String tableType) {
        return switch (tableType.toLowerCase()) {
            case "attractions" -> "관광지";
            case "restaurants" -> "음식점";
            case "accommodations" -> "숙박시설";
            case "cultural" -> "문화시설";
            case "leisure" -> "레포츠";
            case "shopping" -> "쇼핑";
            default -> tableType;
        };
    }
}