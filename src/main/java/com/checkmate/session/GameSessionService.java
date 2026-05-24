package com.checkmate.session;

import com.checkmate.domain.model.GameState;
import com.checkmate.session.model.GameSession;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GameSessionService {

    private final com.checkmate.session.GameSessionRepository repository;

    public GameSessionService(com.checkmate.session.GameSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Triggered by the Kafka Matchmaking Consumer when a new match is found.
     * Generates the initial standard chess board and pushes it to Redis.
     */
    public Mono<GameSession> initializeSession(String matchId, String whitePlayerId, String blackPlayerId) {
        // Create the initial Board and GameState using your Domain models
        GameState initialState = new GameState(matchId);

        GameSession session = new GameSession(
                matchId,
                whitePlayerId,
                blackPlayerId,
                initialState,
                System.currentTimeMillis()
        );

        return repository.save(session).thenReturn(session);
    }

    /**
     * Retrieves the current board state for move validation.
     */
    public Mono<GameSession> getSession(String matchId) {
        return repository.findById(matchId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game Session expired or not found for ID: " + matchId)));
    }

    /**
     * Refreshes the session with a newly executed move and updates the timestamp.
     */
    public Mono<GameSession> updateSession(GameSession currentSession) {
        GameSession updatedSession = new GameSession(
                currentSession.matchId(),
                currentSession.whitePlayerId(),
                currentSession.blackPlayerId(),
                currentSession.gameState(),
                System.currentTimeMillis()
        );
        return repository.save(updatedSession).thenReturn(updatedSession);
    }

    /**
     * Purges the game from RAM.
     */
    public Mono<Void> endSession(String matchId) {
        return repository.delete(matchId).then();
    }
}