package com.example.tictactoe;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundUtil {
    private static MediaPlayer mediaPlayer;

    public static void playSound(Context context, int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context, soundResId);
        mediaPlayer.start();
    }

    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
