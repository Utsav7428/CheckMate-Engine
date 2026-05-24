package com.checkmate.session;

import com.checkmate.session.model.GameSession;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class GameSessionRepository {

    private static final String KEY_PREFIX = "game:session:";
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public GameSessionRepository(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Reactively saves the session to Redis with a 24-hour Time-To-Live.
     */
    public Mono<Boolean> save(GameSession session) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + session.matchId(), session, Duration.ofHours(24));
    }

    /**
     * Fetches the active game state from memory.
     */
    public Mono<GameSession> findById(String matchId) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + matchId)
                .cast(GameSession.class);
    }

    public Mono<Boolean> delete(String matchId) {
        return redisTemplate.opsForValue().delete(KEY_PREFIX + matchId);
    }
}