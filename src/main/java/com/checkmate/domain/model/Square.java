package com.checkmate.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class Square implements Serializable {

    private final int rank; // 0-7 (rows)
    private final int file; // 0-7 (columns)

    public Square(int rank, int file) {
        if (rank < 0 || rank > 7 || file < 0 || file > 7) {
            throw new IllegalArgumentException("Square out of bounds: rank=" + rank + " file=" + file);
        }
        this.rank = rank;
        this.file = file;
    }

    public boolean isValid() {
        return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
    }

    public static boolean inBounds(int rank, int file) {
        return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (rank + 1);
    }
}
