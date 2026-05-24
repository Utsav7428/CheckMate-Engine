package com.checkmate.domain.model;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Move implements Serializable {

    private final Square from;
    private final Square to;
    private final PieceType promotionPiece; // null unless pawn promotion

    public Move(Square from, Square to) {
        this(from, to, null);
    }

    public Move(Square from, Square to, PieceType promotionPiece) {
        this.from = from;
        this.to = to;
        this.promotionPiece = promotionPiece;
    }

    public boolean isPromotion() {
        return promotionPiece != null;
    }

    @Override
    public String toString() {
        return from + "->" + to + (isPromotion() ? "=" + promotionPiece : "");
    }
}
