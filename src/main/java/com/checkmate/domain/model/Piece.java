package com.checkmate.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Piece implements Serializable, Cloneable {

    private final PieceType type;
    private final PieceColor color;

    @Override
    public Piece clone() {
        return new Piece(type, color);
    }

    @Override
    public String toString() {
        return color.name().charAt(0) + type.name().substring(0, 2);
    }
}
