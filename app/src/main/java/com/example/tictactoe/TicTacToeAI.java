package com.example.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeAI {
    public static final int HUMAN = -1;
    public static final int COMP = +1;

    public static class Move {
        public int row, col, score;

        public Move(int row, int col, int score) {
            this.row = row;
            this.col = col;
            this.score = score;
        }
    }

    public static class StateNode {
        public int[][] board;

        public StateNode() {
            board = new int[3][3];
        }

        public int getScoreValue() {
            if (isWin(COMP)) {
                return +1;
            } else if (isWin(HUMAN)) {
                return -1;
            } else {
                return 0;
            }
        }

        public boolean isGameOver() {
            return isWin(HUMAN) || isWin(COMP) || getEmptyCells().isEmpty();
        }

        public List<int[]> getEmptyCells() {
            List<int[]> cells = new ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    if (board[x][y] == 0) {
                        cells.add(new int[]{x, y});
                    }
                }
            }
            return cells;
        }

        public boolean isValidMove(int x, int y) {
            return board[x][y] == 0;
        }

        public boolean setMove(int x, int y, int player) {
            if (isValidMove(x, y)) {
                board[x][y] = player;
                return true;
            }
            return false;
        }

        public boolean isWin(int player) {
            int[][] winPositions = {
                    {0, 0, 0, 1, 0, 2},
                    {1, 0, 1, 1, 1, 2},
                    {2, 0, 2, 1, 2, 2},
                    {0, 0, 1, 0, 2, 0},
                    {0, 1, 1, 1, 2, 1},
                    {0, 2, 1, 2, 2, 2},
                    {0, 0, 1, 1, 2, 2},
                    {0, 2, 1, 1, 2, 0}
            };
            for (int[] winPos : winPositions) {
                if (board[winPos[0]][winPos[1]] == player &&
                        board[winPos[2]][winPos[3]] == player &&
                        board[winPos[4]][winPos[5]] == player) {
                    return true;
                }
            }
            return false;
        }
    }

    public Move minimax(StateNode state, int depth, int player) {
        List<int[]> availableMoves = state.getEmptyCells();

        Move bestMove = new Move(-1, -1, player == COMP ? Integer.MIN_VALUE : Integer.MAX_VALUE);

        if (depth == 0 || state.isGameOver()) {
            int score = state.getScoreValue();
            return new Move(-1, -1, score);
        }

        for (int[] move : availableMoves) {
            state.board[move[0]][move[1]] = player;
            Move currentMove = minimax(state, depth - 1, -player);
            state.board[move[0]][move[1]] = 0;
            currentMove.row = move[0];
            currentMove.col = move[1];

            if (player == COMP) {
                if (currentMove.score > bestMove.score) {
                    bestMove = currentMove;
                }
            } else {
                if (currentMove.score < bestMove.score) {
                    bestMove = currentMove;
                }
            }
        }

        return bestMove;
    }

    public void computerMove(StateNode state) {
        int depth = state.getEmptyCells().size();
        if (depth == 0 || state.isGameOver()) {
            return;
        }

        if (depth == 9) {
            Random random = new Random();
            int x = random.nextInt(3);
            int y = random.nextInt(3);
            state.setMove(x, y, COMP);
        } else {
            Move bestMove = minimax(state, depth, COMP);
            state.setMove(bestMove.row, bestMove.col, COMP);
        }
    }
}
