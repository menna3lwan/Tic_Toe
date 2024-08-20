package com.example.tictactoe;

import androidx.annotation.NonNull;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultDialog extends Dialog {

    private final String message;
    private final Context context;

    public ResultDialog(@NonNull Context context, String message) {
        super(context);
        this.message = message;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_dialog);

        TextView messageText = findViewById(R.id.messageText);
        Button startAgainButton = findViewById(R.id.startAgainButton);

        messageText.setText(message);

        startAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).restartMatch();
                } else if (context instanceof OnlineMultiplayerGameActivity) {
                    ((OnlineMultiplayerGameActivity) context).restartMatch();
                } else if (context instanceof PlayGameWithAI) {
                    ((PlayGameWithAI) context).restartMatch();
                }
                dismiss();
            }
        });
    }
}
