package com.checkmate.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable, Cloneable {

    private final Piece[][] grid = new Piece[8][8];
    private Square enPassantTarget;
    private boolean whiteKingsideCastle = true;
    private boolean whiteQueensideCastle = true;
    private boolean blackKingsideCastle = true;
    private boolean blackQueensideCastle = true;

    public Board() {
        setupInitialPosition();
    }

    private Board(Piece[][] source, Square enPassantTarget,
                  boolean wK, boolean wQ, boolean bK, boolean bQ) {
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                this.grid[r][f] = source[r][f] != null ? source[r][f].clone() : null;
            }
        }
        this.enPassantTarget = enPassantTarget;
        this.whiteKingsideCastle = wK;
        this.whiteQueensideCastle = wQ;
        this.blackKingsideCastle = bK;
        this.blackQueensideCastle = bQ;
    }

    private void setupInitialPosition() {
        PieceType[] backRank = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        for (int f = 0; f < 8; f++) {
            grid[0][f] = new Piece(backRank[f], PieceColor.WHITE);
            grid[1][f] = new Piece(PieceType.PAWN, PieceColor.WHITE);
            grid[6][f] = new Piece(PieceType.PAWN, PieceColor.BLACK);
            grid[7][f] = new Piece(backRank[f], PieceColor.BLACK);
        }
    }

    public Piece getPiece(Square sq) {
        return grid[sq.getRank()][sq.getFile()];
    }

    public Piece getPiece(int rank, int file) {
        return grid[rank][file];
    }

    public void setPiece(Square sq, Piece piece) {
        grid[sq.getRank()][sq.getFile()] = piece;
    }

    public void setPiece(int rank, int file, Piece piece) {
        grid[rank][file] = piece;
    }

    public void clearSquare(Square sq) {
        grid[sq.getRank()][sq.getFile()] = null;
    }

    public Square findKing(PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece p = grid[r][f];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    return new Square(r, f);
                }
            }
        }
        throw new IllegalStateException("King not found for " + color);
    }

    public List<Square> getOccupiedSquares(PieceColor color) {
        List<Square> squares = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece p = grid[r][f];
                if (p != null && p.getColor() == color) {
                    squares.add(new Square(r, f));
                }
            }
        }
        return squares;
    }

    public Square getEnPassantTarget() { return enPassantTarget; }
    public void setEnPassantTarget(Square sq) { this.enPassantTarget = sq; }

    public boolean canCastle(PieceColor color, boolean kingside) {
        if (color == PieceColor.WHITE) return kingside ? whiteKingsideCastle : whiteQueensideCastle;
        return kingside ? blackKingsideCastle : blackQueensideCastle;
    }

    public void revokeCastle(PieceColor color, boolean kingside) {
        if (color == PieceColor.WHITE) {
            if (kingside) whiteKingsideCastle = false; else whiteQueensideCastle = false;
        } else {
            if (kingside) blackKingsideCastle = false; else blackQueensideCastle = false;
        }
    }

    public void revokeAllCastling(PieceColor color) {
        revokeCastle(color, true);
        revokeCastle(color, false);
    }

    @Override
    public Board clone() {
        return new Board(grid, enPassantTarget, whiteKingsideCastle,
                whiteQueensideCastle, blackKingsideCastle, blackQueensideCastle);
    }

    /**
     * Executes a move on the board, handling normal piece movement,
     * castling rights revocation, and En Passant target tracking.
     */
    public void applyMove(Move move) {
        Square from = move.getFrom();
        Square to = move.getTo();
        Piece movingPiece = getPiece(from);

        if (movingPiece == null) return;

        // Clear En Passant target by default on every new move
        Square previousEpTarget = this.enPassantTarget;
        this.enPassantTarget = null;

        // --- EN PASSANT CAPTURE HANDLING ---
        if (movingPiece.getType() == PieceType.PAWN && to.equals(previousEpTarget)) {
            int captureRank = movingPiece.getColor() == PieceColor.WHITE ? to.getRank() - 1 : to.getRank() + 1;
            clearSquare(new Square(captureRank, to.getFile())); // Remove captured pawn
        }

        // --- EN PASSANT TARGET CREATION ---
        if (movingPiece.getType() == PieceType.PAWN && Math.abs(to.getRank() - from.getRank()) == 2) {
            int epRank = movingPiece.getColor() == PieceColor.WHITE ? from.getRank() + 1 : from.getRank() - 1;
            this.enPassantTarget = new Square(epRank, from.getFile());
        }

        // --- CASTLING MOVEMENT HANDLING ---
        if (movingPiece.getType() == PieceType.KING && Math.abs(to.getFile() - from.getFile()) == 2) {
            boolean isKingside = to.getFile() == 6;
            int rank = movingPiece.getColor() == PieceColor.WHITE ? 0 : 7;
            int rookStartFile = isKingside ? 7 : 0;
            int rookEndFile = isKingside ? 5 : 3;

            // Move the corresponding Rook
            Piece rook = getPiece(rank, rookStartFile);
            setPiece(rank, rookEndFile, rook);
            clearSquare(new Square(rank, rookStartFile));
        }

        // --- CASTLING RIGHTS REVOCATION ---
        if (movingPiece.getType() == PieceType.KING) {
            revokeAllCastling(movingPiece.getColor());
        } else if (movingPiece.getType() == PieceType.ROOK) {
            if (from.getFile() == 7) revokeCastle(movingPiece.getColor(), true);  // Kingside
            if (from.getFile() == 0) revokeCastle(movingPiece.getColor(), false); // Queenside
        }

        // Execute actual piece movement
        setPiece(to, movingPiece);
        clearSquare(from);
    }
}
