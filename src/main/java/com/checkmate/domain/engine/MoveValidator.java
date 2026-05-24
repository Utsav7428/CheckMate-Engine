package com.checkmate.domain.engine;

import com.checkmate.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoveValidator {

    private final PieceMovementRules movementRules;
    private final SpeculativeSimulator simulator;

    public MoveValidator(PieceMovementRules movementRules, SpeculativeSimulator simulator) {
        this.movementRules = movementRules;
        this.simulator = simulator;
    }
    public List<Move> getStrictlyLegalMoves(Board board, Square from) {
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return List.of();
        }

        List<Square> pseudoLegalTargets = movementRules.pseudoLegalTargets(board, from);

        return pseudoLegalTargets.stream()
                .map(target -> new Move(from, target))
                .filter(move -> simulator.isMoveStrictlyLegal(board, move, piece.getColor()))
                .collect(Collectors.toList());
    }
}