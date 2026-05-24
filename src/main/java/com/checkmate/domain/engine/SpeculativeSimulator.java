package com.checkmate.domain.engine;

import com.checkmate.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeculativeSimulator {

    private final PieceMovementRules movementRules;

    public SpeculativeSimulator(PieceMovementRules movementRules) {
        this.movementRules = movementRules;
    }

    /**
     * Clones the board state and executes the candidate move in isolation
     * to verify if the King is left exposed.
     */
    public boolean isMoveStrictlyLegal(Board currentBoard, Move candidateMove, PieceColor movingColor) {
        // 1. Deep copy the board to isolate the simulation boundary
        Board speculativeBoard = currentBoard.clone();

        // 2. Apply the speculative mutation
        speculativeBoard.applyMove(candidateMove);

        // 3. Rollback Threat Audit: Check if the moving player's King is under attack
        return !isKingInCheck(speculativeBoard, movingColor);
    }

    /**
     * Scans opponent pieces to see if any pseudo-legal target lands on the King.
     * Heavily optimized using Board.getOccupiedSquares().
     */
    public boolean isKingInCheck(Board board, PieceColor kingColor) {
        Square kingSquare = board.findKing(kingColor);
        PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        // Only iterate over squares that actually contain opponent pieces
        List<Square> opponentSquares = board.getOccupiedSquares(opponentColor);

        for (Square enemySquare : opponentSquares) {
            List<Square> threatSquares = movementRules.pseudoLegalTargets(board, enemySquare);
            if (threatSquares.contains(kingSquare)) {
                return true; // King is in check
            }
        }

        return false;
    }
}