package com.checkmate.matchmaking;

import org.springframework.stereotype.Component;

@Component
public class EloPartitionStrategy {

    /**
     * Maps an Elo rating to a specific Kafka partition index (0-5).
     * Band 0: 0-800, Band 1: 801-1200, Band 2: 1201-1600, etc.
     */
    public int determinePartition(int eloRating) {
        if (eloRating < 800) return 0;
        if (eloRating < 1200) return 1;
        if (eloRating < 1600) return 2;
        if (eloRating < 2000) return 3;
        if (eloRating < 2400) return 4;
        return 5; // Grandmaster tier
    }
}