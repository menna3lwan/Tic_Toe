package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class PlayGameWithAI extends AppCompatActivity {
    private TicTacToeAI.StateNode gameState;
    private Map<Integer, int[]> idToCellMap;
    private Map<Integer, ImageView> cellToImageViewMap;
    private boolean isGameOver;
    private boolean isPlayerTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game_with_ai);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        gameState = new TicTacToeAI.StateNode();
        isGameOver = false;
        isPlayerTurn = true;  // Human starts first

        idToCellMap = new HashMap<>();
        cellToImageViewMap = new HashMap<>();

        initializeBoard();
    }

    private void initializeBoard() {
        idToCellMap.put(R.id.image1, new int[]{0, 0});
        idToCellMap.put(R.id.image2, new int[]{0, 1});
        idToCellMap.put(R.id.image3, new int[]{0, 2});
        idToCellMap.put(R.id.image4, new int[]{1, 0});
        idToCellMap.put(R.id.image5, new int[]{1, 1});
        idToCellMap.put(R.id.image6, new int[]{1, 2});
        idToCellMap.put(R.id.image7, new int[]{2, 0});
        idToCellMap.put(R.id.image8, new int[]{2, 1});
        idToCellMap.put(R.id.image9, new int[]{2, 2});

        for (int id : idToCellMap.keySet()) {
            ImageView imageView = findViewById(id);
            cellToImageViewMap.put(id, imageView);
            imageView.setOnClickListener(this::onCellClick);
        }
    }

    private void onCellClick(View view) {
        if (!isPlayerTurn || isGameOver) return;

        int[] cell = idToCellMap.get(view.getId());
        if (gameState.isValidMove(cell[0], cell[1])) {
            gameState.setMove(cell[0], cell[1], TicTacToeAI.HUMAN);
            updateBoard();
            SoundUtil.playSound(this, R.raw.click_sound);
            checkGameOver();

            if (!isGameOver) {
                isPlayerTurn = false;
                TicTacToeAI ai = new TicTacToeAI();
                ai.computerMove(gameState);
                updateBoard();
                SoundUtil.playSound(this, R.raw.click_sound);
                checkGameOver();
                isPlayerTurn = true;
            }
        } else {
            Toast.makeText(this, "Cell already taken", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBoard() {
        for (Map.Entry<Integer, int[]> entry : idToCellMap.entrySet()) {
            int[] cell = entry.getValue();
            ImageView imageView = cellToImageViewMap.get(entry.getKey());
            if (gameState.board[cell[0]][cell[1]] == TicTacToeAI.HUMAN) {
                imageView.setImageResource(R.drawable.ximage);
            } else if (gameState.board[cell[0]][cell[1]] == TicTacToeAI.COMP) {
                imageView.setImageResource(R.drawable.oimage);
            } else {
                imageView.setImageResource(0);
            }
        }
    }

    private void checkGameOver() {
        if (gameState.isWin(TicTacToeAI.HUMAN)) {
            isGameOver = true;
            SoundUtil.playSound(this, R.raw.win_sound);
            showResultDialog("You win!");
        } else if (gameState.isWin(TicTacToeAI.COMP)) {
            isGameOver = true;
            SoundUtil.playSound(this, R.raw.lose_sound);
            showResultDialog("You lose!");
        } else if (gameState.getEmptyCells().isEmpty()) {
            isGameOver = true;
            SoundUtil.playSound(this, R.raw.draw_sound);
            showResultDialog("It's a draw!");
        }
    }

    private void showResultDialog(String message) {
        ResultDialog resultDialog = new ResultDialog(this, message);
        resultDialog.setCancelable(false);
        resultDialog.show();
    }

    public void restartMatch() {
        if (gameState.isWin(TicTacToeAI.HUMAN)) {
            isPlayerTurn = true;  // Player starts first if player won
        } else if (gameState.isWin(TicTacToeAI.COMP)) {
            isPlayerTurn = false; // Computer starts first if computer won
        } else {
            isPlayerTurn = true;  // For simplicity, assume player starts if it's a draw
        }

        gameState = new TicTacToeAI.StateNode(); // Reset game state
        isGameOver = false; // Reset game over flag
        updateBoard(); // Clear the UI board representation

        // If computer is to start, make its move immediately
        if (!isPlayerTurn) {
            TicTacToeAI ai = new TicTacToeAI();
            ai.computerMove(gameState);
            updateBoard();
            SoundUtil.playSound(this, R.raw.click_sound);
            checkGameOver();
            isPlayerTurn = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundUtil.release(); // Release MediaPlayer resources
    }
}
