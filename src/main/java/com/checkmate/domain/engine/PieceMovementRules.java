package com.checkmate.domain.engine;

import com.checkmate.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class PieceMovementRules {

    public List<Square> pseudoLegalTargets(Board board, Square from) {
        Piece piece = board.getPiece(from);
        if (piece == null) return List.of();
        return switch (piece.getType()) {
            case PAWN   -> pawnTargets(board, from, piece.getColor());
            case KNIGHT -> knightTargets(board, from, piece.getColor());
            case BISHOP -> slidingTargets(board, from, piece.getColor(), new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
            case ROOK   -> slidingTargets(board, from, piece.getColor(), new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
            case QUEEN  -> slidingTargets(board, from, piece.getColor(),
                               new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}});
            case KING   -> kingTargets(board, from, piece.getColor());
        };
    }

    private List<Square> pawnTargets(Board board, Square from, PieceColor color) {
        List<Square> targets = new ArrayList<>();
        int dir = color == PieceColor.WHITE ? 1 : -1;
        int startRank = color == PieceColor.WHITE ? 1 : 6;
        int r = from.getRank(), f = from.getFile();

        if (Square.inBounds(r + dir, f) && board.getPiece(r + dir, f) == null) {
            targets.add(new Square(r + dir, f));
            if (r == startRank && board.getPiece(r + 2 * dir, f) == null) {
                targets.add(new Square(r + 2 * dir, f));
            }
        }
        for (int df : new int[]{-1, 1}) {
            if (Square.inBounds(r + dir, f + df)) {
                Piece target = board.getPiece(r + dir, f + df);
                if (target != null && target.getColor() != color) {
                    targets.add(new Square(r + dir, f + df));
                }
                Square ep = board.getEnPassantTarget();
                if (ep != null && ep.getRank() == r + dir && ep.getFile() == f + df) {
                    targets.add(ep);
                }
            }
        }
        return targets;
    }

    private List<Square> knightTargets(Board board, Square from, PieceColor color) {
        List<Square> targets = new ArrayList<>();
        int[][] offsets = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] o : offsets) {
            int nr = from.getRank() + o[0], nf = from.getFile() + o[1];
            if (Square.inBounds(nr, nf)) {
                Piece p = board.getPiece(nr, nf);
                if (p == null || p.getColor() != color) targets.add(new Square(nr, nf));
            }
        }
        return targets;
    }

    private List<Square> slidingTargets(Board board, Square from, PieceColor color, int[][] directions) {
        List<Square> targets = new ArrayList<>();
        for (int[] d : directions) {
            int nr = from.getRank() + d[0], nf = from.getFile() + d[1];
            while (Square.inBounds(nr, nf)) {
                Piece p = board.getPiece(nr, nf);
                if (p == null) {
                    targets.add(new Square(nr, nf));
                } else {
                    if (p.getColor() != color) targets.add(new Square(nr, nf));
                    break;
                }
                nr += d[0]; nf += d[1];
            }
        }
        return targets;
    }

    private List<Square> kingTargets(Board board, Square from, PieceColor color) {
        List<Square> targets = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0) continue;
                int nr = from.getRank() + dr, nf = from.getFile() + df;
                if (Square.inBounds(nr, nf)) {
                    Piece p = board.getPiece(nr, nf);
                    if (p == null || p.getColor() != color) targets.add(new Square(nr, nf));
                }
            }
        }
        addCastlingTargets(board, from, color, targets);
        return targets;
    }

    private void addCastlingTargets(Board board, Square from, PieceColor color, List<Square> targets) {
        int rank = color == PieceColor.WHITE ? 0 : 7;
        if (from.getRank() != rank || from.getFile() != 4) return;

        if (board.canCastle(color, true)
                && board.getPiece(rank, 5) == null
                && board.getPiece(rank, 6) == null) {
            targets.add(new Square(rank, 6));
        }
        if (board.canCastle(color, false)
                && board.getPiece(rank, 3) == null
                && board.getPiece(rank, 2) == null
                && board.getPiece(rank, 1) == null) {
            targets.add(new Square(rank, 2));
        }
    }
}
