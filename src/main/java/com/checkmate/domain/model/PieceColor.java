package com.checkmate.domain.model;

public enum PieceColor {
    WHITE, BLACK;

    public PieceColor opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
