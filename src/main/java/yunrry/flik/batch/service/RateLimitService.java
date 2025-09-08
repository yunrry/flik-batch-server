package yunrry.flik.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int DAILY_LIMIT = 1000;

    public boolean canMakeRequest() {
        String key = "tourism-api:" + LocalDate.now();
        String countStr = redisTemplate.opsForValue().get(key);

        if (countStr == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofDays(1));
            return true;
        }

        int currentCount = Integer.parseInt(countStr);
        if (currentCount >= DAILY_LIMIT) {
            log.warn("API rate limit exceeded: {}/{}", currentCount, DAILY_LIMIT);
            return false;
        }

        redisTemplate.opsForValue().increment(key);
        return true;
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