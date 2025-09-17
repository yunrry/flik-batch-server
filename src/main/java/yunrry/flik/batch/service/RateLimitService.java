package yunrry.flik.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import yunrry.flik.batch.exception.RateLimitExceededException;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate-limit.tourism-api.daily-limit:600}")
    private int DAILY_LIMIT;

    public void checkRateLimit() throws RateLimitExceededException {
        if (!canMakeRequest()) {
            int remaining = getRemainingCount();
            throw new RateLimitExceededException(
                    String.format("Daily API limit exceeded. Remaining: %d", remaining));
        }
    }

    public boolean canMakeRequest() {
        return getCurrentCount() < DAILY_LIMIT;
    }


    public int getCurrentCount() {
        String key = "tourism-api:" + LocalDate.now();
        String countStr = redisTemplate.opsForValue().get(key);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    public int getRemainingCount() {
        return DAILY_LIMIT - getCurrentCount();
    }
}