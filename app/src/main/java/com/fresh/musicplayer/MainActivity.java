package com.fresh.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView nowPlaying;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private final String musicLocation = "local/music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowPlaying = findViewById(R.id.nowPlaying);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    public void onControlPanelClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                runLocalMusic();
                break;
            case R.id.btnPause:
                mediaPlayer.pause();
                break;
        }
    }

    public void runLocalMusic() {
        Toast.makeText(this, "Проигрывается локальная музыка", Toast.LENGTH_SHORT).show();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                AssetFileDescriptor afd = getAssets().openFd(musicLocation + "/the_phoenix.mp3");
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        mediaPlayer.start();
        nowPlaying.setText("Fall Out Boy - The Phoenix");
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}