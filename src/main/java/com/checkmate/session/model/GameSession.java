package com.checkmate.session.model;

import com.checkmate.domain.model.GameState;
import java.io.Serializable;

/**
 * A record acting as the root aggregate for Redis storage.
 */
public record GameSession(
        String matchId,
        String whitePlayerId,
        String blackPlayerId,
        GameState gameState,
        long lastUpdatedAt
) implements Serializable {}