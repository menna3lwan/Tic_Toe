package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class twoPlayers extends AppCompatActivity {
    Button offline, online;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_players);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        offline = findViewById(R.id.offlineMode);
        online = findViewById(R.id.onlineMode);

        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(twoPlayers.this, AddPlayers.class);
                startActivity(intent);
            }
        });

        online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(twoPlayers.this, OnlineCodeGeneratorActivity.class);
                startActivity(intent);
            }
        });
    }
}