package com.checkmate.matchmaking.dto;

public record PlayerRegistrationEvent(
        String playerId,
        int eloRating,
        long timestamp
) {}