package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class OnlineCodeGeneratorActivity extends AppCompatActivity {

    private EditText playerNameEditText, codeEditText;
    private Button generateButton, joinButton;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_code_generator);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        playerNameEditText = findViewById(R.id.playerName);
        codeEditText = findViewById(R.id.code);
        generateButton = findViewById(R.id.generateButton);
        joinButton = findViewById(R.id.joinButton);
        progressBar = findViewById(R.id.progressCir);

        databaseReference = FirebaseDatabase.getInstance("https://tic-tac-toe-game-3915d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        generateButton.setOnClickListener(view -> generateCode());
        joinButton.setOnClickListener(view -> joinGame());
    }

    private void generateCode() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            // Proceed with online functionality
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String playerName = playerNameEditText.getText().toString().trim();
        String code = codeEditText.getText().toString().trim();

        if (playerName.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Please enter your name and code", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check if code already exists
        DatabaseReference codeRef = databaseReference.child("codes").child(code);
        codeRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Code already exists. Please enter a different code.", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> gameDetails = new HashMap<>();
                gameDetails.put("player1", playerName);
                gameDetails.put("player2", "");

                codeRef.setValue(gameDetails)
                        .addOnSuccessListener(aVoid -> {
                            ValueEventListener player2Listener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String player2Name = dataSnapshot.getValue(String.class);
                                    if (player2Name != null && !player2Name.isEmpty()) {
                                        progressBar.setVisibility(View.GONE);
                                        // Prevent starting the activity multiple times
                                        codeRef.child("player2").removeEventListener(this);
                                        Intent intent = new Intent(OnlineCodeGeneratorActivity.this, OnlineMultiplayerGameActivity.class);
                                        intent.putExtra("gameCode", code);
                                        intent.putExtra("isCodeMaker", true);
                                        intent.putExtra("codeKey", code);
                                        intent.putExtra("playerName", playerName);
                                        intent.putExtra("opponentName", player2Name);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(OnlineCodeGeneratorActivity.this, "Failed to generate code. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            };
                            codeRef.child("player2").addValueEventListener(player2Listener);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to generate code. Please try again.", Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to check code existence. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }


    private void joinGame() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            // Proceed with online functionality
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String playerName = playerNameEditText.getText().toString().trim();
        String code = codeEditText.getText().toString().trim();

        if (playerName.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Please enter your name and code", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child("codes").child(code).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Map<String, Object> gameDetails = (Map<String, Object>) dataSnapshot.getValue();
                if (gameDetails != null) {
                    String player1 = (String) gameDetails.get("player1");
                    String player2 = (String) gameDetails.get("player2");

                    if (player2 == null || player2.isEmpty()) {
                        gameDetails.put("player2", playerName);

                        databaseReference.child("codes").child(code).setValue(gameDetails)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(OnlineCodeGeneratorActivity.this, OnlineMultiplayerGameActivity.class);
                                    intent.putExtra("gameCode", code);
                                    intent.putExtra("isCodeMaker", false);
                                    intent.putExtra("codeKey", code);
                                    intent.putExtra("playerName", playerName);
                                    intent.putExtra("opponentName", player1);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to join game. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Game is already full.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to join game. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

}
