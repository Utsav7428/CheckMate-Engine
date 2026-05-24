package com.checkmate.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "game_records")
@Getter
@Setter
public class GameRecord {

    @Id
    @Column(name = "match_id", nullable = false, updatable = false)
    private String matchId;

    @Column(name = "white_player_id", nullable = false)
    private String whitePlayerId;

    @Column(name = "black_player_id", nullable = false)
    private String blackPlayerId;

    @Column(name = "winner_id")
    private String winnerId; // Null if it was a draw

    @Column(name = "termination_reason", nullable = false)
    private String terminationReason; // CHECKMATE, STALEMATE, RESIGNATION, etc.

    @Column(name = "pgn_history", columnDefinition = "TEXT")
    private String pgnHistory; // The entire move list as a concatenated PGN string

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}