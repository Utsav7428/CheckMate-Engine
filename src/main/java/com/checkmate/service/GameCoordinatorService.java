package com.checkmate.service;

import com.checkmate.domain.engine.CheckMateDetector;
import com.checkmate.domain.engine.MoveValidator;
import com.checkmate.domain.model.Board;
import com.checkmate.domain.model.GameState;
import com.checkmate.domain.model.Move;
import com.checkmate.domain.model.PieceColor;
import com.checkmate.persistence.ArchivalService;
import com.checkmate.session.GameSessionService;
import com.checkmate.session.model.GameSession;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GameCoordinatorService {

    private final GameSessionService sessionService;
    private final MoveValidator moveValidator;
    private final CheckMateDetector checkmateDetector;
    private final ArchivalService archivalService;

    public GameCoordinatorService(GameSessionService sessionService, MoveValidator moveValidator,
                                  CheckMateDetector checkmateDetector, ArchivalService archivalService) {
        this.sessionService = sessionService;
        this.moveValidator = moveValidator;
        this.checkmateDetector = checkmateDetector;
        this.archivalService = archivalService;
    }

    /**
     * Reactively processes a move request. Validates the turn, audits the move geometry,
     * updates the board, and triggers archival if the game terminates.
     */
    public Mono<GameSession> processPlayerMove(String matchId, String playerId, Move candidateMove) {
        return sessionService.getSession(matchId)
                .flatMap(session -> {
                    GameState state = session.gameState();

                    // 1. Verify Turn Ownership
                    boolean isWhite = playerId.equals(session.whitePlayerId());
                    PieceColor currentTurn = state.getCurrentTurn();

                    if ((isWhite && currentTurn == PieceColor.BLACK) || (!isWhite && currentTurn == PieceColor.WHITE)) {
                        return Mono.error(new IllegalArgumentException("It is not your turn."));
                    }

                    // 2. Strict Engine Validation
                    Board board = state.getBoard();
                    List<Move> legalMoves = moveValidator.getStrictlyLegalMoves(board, candidateMove.getFrom());

                    if (!legalMoves.contains(candidateMove)) {
                        return Mono.error(new IllegalArgumentException("Illegal move."));
                    }

                    // 3. Execute Mutation
                    board.applyMove(candidateMove);
                    state.recordMove(candidateMove.toString());
                    state.flipTurn();

                    // 4. Terminal State Evaluation
                    GameState.Status newStatus = checkmateDetector.evaluateTerminalState(board, state.getCurrentTurn());
                    state.setStatus(newStatus);

                    // 5. Update Redis & Handle Archival asynchronously
                    return sessionService.updateSession(session)
                            .doOnSuccess(updatedSession -> {
                                if (newStatus != GameState.Status.ACTIVE && newStatus != GameState.Status.CHECK) {
                                    // Game over: Archive to Postgres and flush from Redis RAM
                                    archivalService.archiveGame(updatedSession);
                                    sessionService.endSession(matchId).subscribe();
                                }
                            });
                });
    }
}