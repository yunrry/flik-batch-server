package yunrry.flik.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private void sendDiscordMessage(String message) {
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
}
