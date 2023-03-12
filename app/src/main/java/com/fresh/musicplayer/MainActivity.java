package com.fresh.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private TextView nowPlaying;
    private Switch switchLoop;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private final String musicLocation = "local/music";
    private PlaylistHolder playlistHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowPlaying = findViewById(R.id.nowPlaying);
        switchLoop = findViewById(R.id.switchLoop);
        switchLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mediaPlayer != null) mediaPlayer.setLooping(isChecked);
            }
        });

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

    public void runLocalMusic() {
        if (mediaPlayer == null) {
            Toast.makeText(this, "Проигрывается локальная музыка", Toast.LENGTH_SHORT).show();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            try {
                AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else mediaPlayer.start();
        mediaPlayer.setLooping(switchLoop.isChecked());
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