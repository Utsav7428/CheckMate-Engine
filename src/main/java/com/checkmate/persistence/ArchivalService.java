package com.checkmate.persistence;

import com.checkmate.domain.model.GameState;
import com.checkmate.domain.model.PieceColor;
import com.checkmate.persistence.entity.GameRecord;
import com.checkmate.persistence.entity.PlayerProfile;
import com.checkmate.persistence.repository.GameRecordRepository;
import com.checkmate.persistence.repository.PlayerProfileRepository;
import com.checkmate.session.model.GameSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ArchivalService {

    private static final Logger log = LoggerFactory.getLogger(ArchivalService.class);
    private static final int ELO_K_FACTOR = 32;

    private final GameRecordRepository gameRepository;
    private final PlayerProfileRepository playerRepository;

    public ArchivalService(GameRecordRepository gameRepository, PlayerProfileRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Extracts data from the in-memory GameSession, computes the Elo change,
     * and archives everything to PostgreSQL.
     */
    @Transactional
    public void archiveGame(GameSession session) {
        GameState state = session.gameState();

        String whiteId = session.whitePlayerId();
        String blackId = session.blackPlayerId();
        String winnerId = determineWinnerId(state, whiteId, blackId);

        // 1. Create and save the Game Record
        GameRecord record = new GameRecord();
        record.setMatchId(session.matchId());
        record.setWhitePlayerId(whiteId);
        record.setBlackPlayerId(blackId);
        record.setWinnerId(winnerId);
        record.setTerminationReason(state.getStatus().name());
        record.setPgnHistory(String.join(" ", state.getMoveHistory()));
        record.setCompletedAt(Instant.now());

        gameRepository.save(record);

        // 2. Fetch player profiles (or create them if they don't exist yet)
        PlayerProfile whiteProfile = playerRepository.findById(whiteId).orElseGet(() -> createDefaultProfile(whiteId));
        PlayerProfile blackProfile = playerRepository.findById(blackId).orElseGet(() -> createDefaultProfile(blackId));

        // 3. Update stats and calculate Elo
        updatePlayerStats(whiteProfile, blackProfile, winnerId);

        playerRepository.save(whiteProfile);
        playerRepository.save(blackProfile);

        log.info("Match {} archived. New Elos -> White: {}, Black: {}",
                session.matchId(), whiteProfile.getEloRating(), blackProfile.getEloRating());
    }

    private String determineWinnerId(GameState state, String whiteId, String blackId) {
        if (state.getStatus() == GameState.Status.CHECKMATE || state.getStatus() == GameState.Status.RESIGNED) {
            // If the current turn is White, Black delivered the checkmate, and vice versa
            return state.getCurrentTurn() == PieceColor.WHITE ? blackId : whiteId;
        }
        return null; // Draw or Stalemate
    }

    private void updatePlayerStats(PlayerProfile p1, PlayerProfile p2, String winnerId) {
        p1.setGamesPlayed(p1.getGamesPlayed() + 1);
        p2.setGamesPlayed(p2.getGamesPlayed() + 1);

        double p1Expected = 1.0 / (1.0 + Math.pow(10, (p2.getEloRating() - p1.getEloRating()) / 400.0));
        double p2Expected = 1.0 / (1.0 + Math.pow(10, (p1.getEloRating() - p2.getEloRating()) / 400.0));

        double p1Actual = 0.5;
        double p2Actual = 0.5;

        if (winnerId != null) {
            if (winnerId.equals(p1.getPlayerId())) {
                p1Actual = 1.0;
                p2Actual = 0.0;
                p1.setWins(p1.getWins() + 1);
                p2.setLosses(p2.getLosses() + 1);
            } else {
                p1Actual = 0.0;
                p2Actual = 1.0;
                p1.setLosses(p1.getLosses() + 1);
                p2.setWins(p2.getWins() + 1);
            }
        } else {
            p1.setDraws(p1.getDraws() + 1);
            p2.setDraws(p2.getDraws() + 1);
        }

        p1.setEloRating((int) (p1.getEloRating() + ELO_K_FACTOR * (p1Actual - p1Expected)));
        p2.setEloRating((int) (p2.getEloRating() + ELO_K_FACTOR * (p2Actual - p2Expected)));
    }

    private PlayerProfile createDefaultProfile(String playerId) {
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        return profile;
    }
}