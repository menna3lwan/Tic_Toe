package com.example.tictactoe;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.tictactoe.databinding.ActivityOnlineMultiplayerGameBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OnlineMultiplayerGameActivity extends AppCompatActivity {

    private ActivityOnlineMultiplayerGameBinding binding;
    private final List<int[]> combinationList = new ArrayList<>();
    private List<Integer> boxPositions = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0);
    private int playerTurn = 1, count = 0;
    private int totalSelectedBoxes = 1;
    private String gameCode, codeKey, playerName, opponentName, winnerOfTheGame = null;
    private boolean isCodeMaker, isActivityTerminated = false;
    private boolean gameOver = false, soundPlayed = false;
    private ValueEventListener gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineMultiplayerGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        combinationList.add(new int[]{0, 1, 2});
        combinationList.add(new int[]{3, 4, 5});
        combinationList.add(new int[]{6, 7, 8});
        combinationList.add(new int[]{0, 3, 6});
        combinationList.add(new int[]{1, 4, 7});
        combinationList.add(new int[]{2, 5, 8});
        combinationList.add(new int[]{2, 4, 6});
        combinationList.add(new int[]{0, 4, 8});

        gameCode = getIntent().getStringExtra("gameCode");
        isCodeMaker = getIntent().getBooleanExtra("isCodeMaker", false);
        codeKey = getIntent().getStringExtra("codeKey");
        playerName = getIntent().getStringExtra("playerName");
        opponentName = getIntent().getStringExtra("opponentName");

        initializeBoard();
        setupGameListener();

        // Set player names
        binding.playerOneName.setText(isCodeMaker ? playerName : opponentName);
        binding.playerTwoName.setText(isCodeMaker ? opponentName : playerName);

        // Set initial turn
        if (isCodeMaker) {
            playerTurn = 1;
            updatePlayerTurnUI();
        } else {
            playerTurn = 2;
            updatePlayerTurnUI();
        }
    }

    private void initializeBoard() {
        binding.image1.setOnClickListener(view -> handleClick(view, 0));
        binding.image2.setOnClickListener(view -> handleClick(view, 1));
        binding.image3.setOnClickListener(view -> handleClick(view, 2));
        binding.image4.setOnClickListener(view -> handleClick(view, 3));
        binding.image5.setOnClickListener(view -> handleClick(view, 4));
        binding.image6.setOnClickListener(view -> handleClick(view, 5));
        binding.image7.setOnClickListener(view -> handleClick(view, 6));
        binding.image8.setOnClickListener(view -> handleClick(view, 7));
        binding.image9.setOnClickListener(view -> handleClick(view, 8));
    }

    private void handleClick(View view, int position) {
        if (isBoxSelectable(position) && isCurrentPlayerTurn() && !gameOver) {
            SoundUtil.playSound(this, R.raw.click_sound); // Play click sound
            performAction((ImageView) view, position);
            updateGameInFirebase();
        }
    }

    private void performAction(ImageView imageView, int selectedBoxPosition) {
        boxPositions.set(selectedBoxPosition, playerTurn);
        imageView.setImageResource(playerTurn == 1 ? R.drawable.ximage : R.drawable.oimage);

        if (checkResults()) {
            gameOver = true;
            String resultMessage = isCodeMaker == (playerTurn == 1) ? "You won the game!" : "You lost the game!";
            String opponentResultMessage = isCodeMaker == (playerTurn == 1) ? "You lost the game!" : "You won the game!";

            if (isCodeMaker == (playerTurn == 1)) {
                winnerOfTheGame = isCodeMaker ? "CodeMaker" : "CodeBreaker";
                Log.d(TAG, "i am code maker, winner sound");
                soundPlayed = true;
                SoundUtil.playSound(this, R.raw.win_sound); // Play win sound
            } else {
                winnerOfTheGame = isCodeMaker ? "CodeBreaker" : "CodeMaker";
                Log.d(TAG, "i am not code maker, loser sound");
                soundPlayed = true;
                SoundUtil.playSound(this, R.raw.lose_sound); // Play lose sound
            }

            updateResultInFirebase(resultMessage, opponentResultMessage);
        } else if (totalSelectedBoxes == 9) {
            gameOver = true;
            Log.d(TAG, "just draw sound");
            soundPlayed = true;
            SoundUtil.playSound(this, R.raw.draw_sound); // Play draw sound
            updateResultInFirebase("Game Draw!", "Game Draw!");
        } else {
            totalSelectedBoxes++;
            changePlayerTurn();
        }
    }

    private void updateResultInFirebase(String resultMessage, String opponentResultMessage) {
        FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("games").child(gameCode).child("resultMessage").setValue(opponentResultMessage);
        showResultDialog(resultMessage);
    }

    private void showResultDialog(String message) {
        ResultDialog resultDialog = new ResultDialog(this, message);
        resultDialog.setCancelable(false);
        resultDialog.show();
    }

    private boolean isCurrentPlayerTurn() {
        return (isCodeMaker && playerTurn == 1) || (!isCodeMaker && playerTurn == 2);
    }

    private void changePlayerTurn() {
        playerTurn = playerTurn == 1 ? 2 : 1;
        updatePlayerTurnUI();
        updateGameInFirebase();
    }

    private void updatePlayerTurnUI() {
        if (playerTurn == 1) {
            binding.playerOneLayout.setBackgroundResource(R.drawable.black_border);
            binding.playerTwoLayout.setBackgroundResource(R.drawable.white_box);
        } else {
            binding.playerTwoLayout.setBackgroundResource(R.drawable.black_border);
            binding.playerOneLayout.setBackgroundResource(R.drawable.white_box);
        }
    }

    private boolean checkResults() {
        for (int[] combination : combinationList) {
            if (boxPositions.get(combination[0]) == playerTurn &&
                    boxPositions.get(combination[1]) == playerTurn &&
                    boxPositions.get(combination[2]) == playerTurn) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoxSelectable(int boxPosition) {
        return boxPositions.get(boxPosition) == 0;
    }

    public void restartMatch() {
        // Reset game state
        boxPositions = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0);
        totalSelectedBoxes = 1;
        gameOver = false;
        soundPlayed = false;
        count++;


        if (winnerOfTheGame != null) {
            if (winnerOfTheGame.equals("CodeMaker")) {
                playerTurn = 1; // Code maker starts
            } else if (winnerOfTheGame.equals("CodeBreaker")) {
                playerTurn = 2; // Code breaker starts
            }
        }

        if(count >= 2) {
            winnerOfTheGame = null;
            count = 0;
        }

        // Update UI
        for (int i = 0; i < boxPositions.size(); i++) {
            ImageView imageView = getImageViewAt(i);
            if (imageView != null) {
                imageView.setImageResource(0);
            }
        }

        updatePlayerTurnUI();

        // Update Firebase
        updateGameInFirebase();
    }

    private ImageView getImageViewAt(int index) {
        switch (index) {
            case 0:
                return binding.image1;
            case 1:
                return binding.image2;
            case 2:
                return binding.image3;
            case 3:
                return binding.image4;
            case 4:
                return binding.image5;
            case 5:
                return binding.image6;
            case 6:
                return binding.image7;
            case 7:
                return binding.image8;
            case 8:
                return binding.image9;
            default:
                return null;
        }
    }

    private void setupGameListener() {
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OnlineGameState gameState = snapshot.getValue(OnlineGameState.class);
                    if (gameState != null) {
                        boxPositions = gameState.getBoxPositions();
                        playerTurn = gameState.getPlayerTurn();
                        totalSelectedBoxes = gameState.getTotalSelectedBoxes();
                        updateBoardUI();
                        updatePlayerTurnUI();

                        String resultMessage = snapshot.child("resultMessage").getValue(String.class);
                        if (resultMessage != null && !resultMessage.isEmpty() && !gameOver) {
                            ResultDialog resultDialog = new ResultDialog(OnlineMultiplayerGameActivity.this, resultMessage);
                            resultDialog.setCancelable(false);
                            resultDialog.show();
                            FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("games")
                                    .child(gameCode)
                                    .child("resultMessage")
                                    .setValue("");
                        }

                        if(resultMessage != null && !resultMessage.isEmpty()) {
                            if (resultMessage.contains("You won the game!") && !soundPlayed) {
                                Log.d(TAG, "setupGameListener --> winer sound");
                                SoundUtil.playSound(OnlineMultiplayerGameActivity.this, R.raw.win_sound); // Play win sound
                            } else if (resultMessage.contains("You lost the game!") && !soundPlayed) {
                                Log.d(TAG, "setupGameListener -->  loser sound");
                                SoundUtil.playSound(OnlineMultiplayerGameActivity.this, R.raw.lose_sound); // Play lose sound
                            } else if (resultMessage.equals("Game Draw!") && !soundPlayed) {
                                Log.d(TAG, "setupGameListener -->  draw sound");
                                SoundUtil.playSound(OnlineMultiplayerGameActivity.this, R.raw.draw_sound); // Play draw sound
                            }
                        }

                        // Check if code maker has left the game
                        Boolean codeMakerLeft = snapshot.child("codeMakerLeft").getValue(Boolean.class);
                        if (codeMakerLeft != null && codeMakerLeft && !isCodeMaker) {
                            isActivityTerminated = true;
                            Intent intent = new Intent(OnlineMultiplayerGameActivity.this, OnlineCodeGeneratorActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineMultiplayerGameActivity.this, "Failed to read data from database.", Toast.LENGTH_SHORT).show();
            }
        };

        FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("games")
                .child(gameCode)
                .addValueEventListener(gameListener);
    }


    private void updateBoardUI() {
        for (int i = 0; i < boxPositions.size(); i++) {
            ImageView imageView = getImageViewAt(i);
            if (imageView != null) {
                int positionValue = boxPositions.get(i);
                imageView.setImageResource(positionValue == 1 ? R.drawable.ximage : (positionValue == 2 ? R.drawable.oimage : 0));
            }
        }
    }

    private void updateGameInFirebase() {
        FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("games")
                .child(gameCode)
                .setValue(new OnlineGameState(boxPositions, playerTurn, totalSelectedBoxes));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundUtil.release(); // Release MediaPlayer resources
    }

    private void uninitializeGameElements() {
        combinationList.clear();
        boxPositions = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0);
        playerTurn = 1;
        count = 0;
        totalSelectedBoxes = 1;
        gameCode = null;
        codeKey = null;
        playerName = null;
        opponentName = null;
        winnerOfTheGame = null;
        gameOver = false;

        // Reset UI elements
        binding.playerOneName.setText("");
        binding.playerTwoName.setText("");
        for (int i = 0; i < boxPositions.size(); i++) {
            ImageView imageView = getImageViewAt(i);
            if (imageView != null) {
                imageView.setImageResource(0);
            }
        }
    }


    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");

        // Notify the code joiner that the code maker has left the game
        if (isCodeMaker && gameCode != null) {
            FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("games").child(gameCode).child("codeMakerLeft").setValue(true);
        }

        // Remove game listener from Firebase
        if (gameListener != null && gameCode != null) {
            FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("games").child(gameCode).removeEventListener(gameListener);
        }

        // Remove game data from Firebase
        if (isCodeMaker) {
            if (gameCode != null && codeKey != null) {
                // Remove the game entry from both "games" and "codes" nodes
                FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("games").child(gameCode).removeValue();
                FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("codes").child(codeKey).removeValue();
            }
        } else {
            // Remove the code joiner's name from Firebase
            if (gameCode != null) {
                FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("codes").child(gameCode).child("player2").removeValue()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Failed to remove player2: " + task.getException());
                            }
                        });
            }
        }

        uninitializeGameElements();

        if(!isActivityTerminated) {
            Intent intent = new Intent(OnlineMultiplayerGameActivity.this, OnlineCodeGeneratorActivity.class);
            startActivity(intent);
            finish();
        }
    }

}