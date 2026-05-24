package com.checkmate.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameState implements Serializable {

    public enum Status { ACTIVE, CHECK, CHECKMATE, STALEMATE, DRAW, RESIGNED, TIMEOUT }

    private final String gameId;
    private Board board;
    private PieceColor currentTurn;
    private Status status;
    private final List<String> moveHistory = new ArrayList<>();
    private int halfMoveClock;
    private int fullMoveNumber;

    public GameState(String gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.currentTurn = PieceColor.WHITE;
        this.status = Status.ACTIVE;
        this.halfMoveClock = 0;
        this.fullMoveNumber = 1;
    }

    public void recordMove(String pgn) {
        moveHistory.add(pgn);
    }

    public void flipTurn() {
        if (currentTurn == PieceColor.BLACK) fullMoveNumber++;
        currentTurn = currentTurn.opponent();
    }
}
