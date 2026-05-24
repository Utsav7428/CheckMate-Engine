package com.checkmate.matchmaking.dto;

public record MatchCreatedEvent(
        String matchId,
        String whitePlayerId,
        String blackPlayerId,
        int averageElo
) {}