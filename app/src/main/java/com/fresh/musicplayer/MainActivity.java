package com.fresh.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private TextView nowPlaying;

    private MediaPlayer mediaPlayer;

    private final String musicLocation = "local/music";
    private final String RADIO = "https://pub0301.101.ru:8443/stream/air/mp3/256/99";
    private PlaylistHolder playlistHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowPlaying = findViewById(R.id.nowPlaying);
        Switch switchLoop = findViewById(R.id.switchLoop);
        switchLoop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mediaPlayer != null) mediaPlayer.setLooping(isChecked);
        });

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
                if (mediaPlayer != null) mediaPlayer.start();
                else nowPlaying.setText("Выберите источник");
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
                }
                break;

        }
    }

    public void setContentSource(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer = null;
        }

        switch (view.getId()) {
            case R.id.btnRadio:
                try {
                    mediaPlayer = new MediaPlayer();

                    mediaPlayer.setDataSource(RADIO);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.prepareAsync();

                    nowPlaying.setText("Онлайн радио");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.btnLocal:
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnCompletionListener(this);

                    AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    mediaPlayer.prepare();

                    nowPlaying.setText("Локальная музыка");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer.isLooping()) return;

        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

}