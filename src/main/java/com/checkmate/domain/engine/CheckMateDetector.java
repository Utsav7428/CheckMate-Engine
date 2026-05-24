package com.checkmate.domain.engine;

import com.checkmate.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckMateDetector {

    private final MoveValidator moveValidator;
    private final SpeculativeSimulator simulator;

    public CheckMateDetector(MoveValidator moveValidator, SpeculativeSimulator simulator) {
        this.moveValidator = moveValidator;
        this.simulator = simulator;
    }

    /**
     * Determines the overarching status of the game state based on the active player's options.
     */
    public GameState.Status evaluateTerminalState(Board board, PieceColor activeTurnColor) {
        boolean isInCheck = simulator.isKingInCheck(board, activeTurnColor);
        boolean hasAnyLegalMove = false;

        List<Square> activePieces = board.getOccupiedSquares(activeTurnColor);

        for (Square square : activePieces) {
            List<Move> legalMoves = moveValidator.getStrictlyLegalMoves(board, square);
            if (!legalMoves.isEmpty()) {
                hasAnyLegalMove = true;
                break;
            }
        }

        if (!hasAnyLegalMove) {
            return isInCheck ? GameState.Status.CHECKMATE : GameState.Status.STALEMATE;
        }

        return isInCheck ? GameState.Status.CHECK : GameState.Status.ACTIVE;
    }
}