package com.checkmate.websocket.dto;

import com.checkmate.domain.model.GameState;

public record ServerMessage(
        Type type,
        String matchId,
        GameState gameState, // populated during active updates
        String errorMessage  // populated during errors
) {
    public enum Type { SYSTEM, STATE_UPDATE, ERROR }

    public static ServerMessage update(String matchId, GameState state) {
        return new ServerMessage(Type.STATE_UPDATE, matchId, state, null);
    }

    public static ServerMessage error(String matchId, String error) {
        return new ServerMessage(Type.ERROR, matchId, null, error);
    }
}