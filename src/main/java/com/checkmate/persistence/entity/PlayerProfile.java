package com.checkmate.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "player_profiles")
@Getter
@Setter
public class PlayerProfile {

    @Id
    @Column(name = "player_id", nullable = false, updatable = false)
    private String playerId;

    @Column(name = "elo_rating", nullable = false)
    private int eloRating = 1200; // Default starting Elo

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @Column(name = "wins", nullable = false)
    private int wins = 0;

    @Column(name = "losses", nullable = false)
    private int losses = 0;

    @Column(name = "draws", nullable = false)
    private int draws = 0;
}