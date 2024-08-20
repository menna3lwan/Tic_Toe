package com.example.tictactoe;

import java.util.List;

public class OnlineGameState {
    private List<Integer> boxPositions;
    private int playerTurn;
    private int totalSelectedBoxes;

    public OnlineGameState() {
        // Default constructor required for calls to DataSnapshot.getValue(OnlineGameState.class)
    }

    public OnlineGameState(List<Integer> boxPositions, int playerTurn, int totalSelectedBoxes) {
        this.boxPositions = boxPositions;
        this.playerTurn = playerTurn;
        this.totalSelectedBoxes = totalSelectedBoxes;
    }

    public List<Integer> getBoxPositions() {
        return boxPositions;
    }

    public void setBoxPositions(List<Integer> boxPositions) {
        this.boxPositions = boxPositions;
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(int playerTurn) {
        this.playerTurn = playerTurn;
    }

    public int getTotalSelectedBoxes() {
        return totalSelectedBoxes;
    }

    public void setTotalSelectedBoxes(int totalSelectedBoxes) {
        this.totalSelectedBoxes = totalSelectedBoxes;
    }
}
