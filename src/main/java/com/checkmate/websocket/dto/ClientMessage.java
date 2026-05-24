package com.checkmate.websocket.dto;

import com.checkmate.domain.model.Move;

public record ClientMessage(
        Type type,
        String matchId,
        String playerId,
        Move move // populated only if type is MOVE
) {
    public enum Type { JOIN, MOVE }
}