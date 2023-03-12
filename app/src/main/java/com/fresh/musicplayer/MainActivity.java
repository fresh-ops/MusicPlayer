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
    private PlaylistHolder playlistHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowPlaying = findViewById(R.id.nowPlaying);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        playlistHolder = new PlaylistHolder(musicLocation, getSongsList());
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
            case R.id.btnNext:
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();

                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mediaPlayer.start();
                }
                break;
            case R.id.btnPrev:
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        AssetFileDescriptor afd = getAssets().openFd(playlistHolder.previous());
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();

                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mediaPlayer.start();
                }
                break;

        }
    }

    public void runLocalMusic() {
        if (mediaPlayer == null) {
            Toast.makeText(this, "Проигрывается локальная музыка", Toast.LENGTH_SHORT).show();
            mediaPlayer = new MediaPlayer();
            try {
                AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        mediaPlayer.start();
        nowPlaying.setText("Локальная музыка");
    }

    private String[]  getSongsList() {
        try {
            return getAssets().list(musicLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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