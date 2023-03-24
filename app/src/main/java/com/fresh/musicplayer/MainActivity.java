package com.fresh.musicplayer;

import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, Runnable  {
    private final String MUSIC_LOCATION = "local/music";

    private MediaPlayer mediaPlayer;
    private PlaylistHolder playlistHolder;
    private SeekBar seekBar;
    private TextView playingTime;
    private boolean isLooping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playlistHolder = new PlaylistHolder(MUSIC_LOCATION, getSongsList());
        seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        playingTime = findViewById(R.id.playingTime);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                playingTime.setVisibility(View.VISIBLE);
                int timeTrack = (int) Math.ceil(progress/1000f);
                playingTime.setText(String.format("%02d:%02d", timeTrack / 60, timeTrack % 60));

                double percentTrack = progress / (double) seekBar.getMax();
                playingTime.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.9));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playingTime.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    public void onControlPanelClick(View view) {
        switch (view.getId()) {
            case R.id.fabPlayPause:
                if (mediaPlayer != null) {
                    FloatingActionButton fabPlayPause = findViewById(R.id.fabPlayPause);
                    if (mediaPlayer.isPlaying()) {
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                        mediaPlayer.pause();
                    }
                    else {
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                        mediaPlayer.start();
                        new Thread(this).start();
                    }
                }
                else {
                    startMusic();
                }
                break;
            case R.id.fabNext:
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();

                        mediaPlayer.prepare();
                        setMetadata(playlistHolder.current());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case R.id.fabPrevious:
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        AssetFileDescriptor afd = getAssets().openFd(playlistHolder.previous());
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();

                        mediaPlayer.prepare();
                        setMetadata(playlistHolder.current());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case R.id.fabRepeat:
                isLooping = ! isLooping;
                if (mediaPlayer != null) mediaPlayer.setLooping(isLooping);
                FloatingActionButton fabRepeat = findViewById(R.id.fabRepeat);
                if (isLooping) {
                    fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat_single));
                }
                else {
                    fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat));
                }
                break;
        }
    }

    private void startMusic() {
        FloatingActionButton fabPlayPause = findViewById(R.id.fabPlayPause);
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setLooping(isLooping);

            AssetFileDescriptor afd = getAssets().openFd(playlistHolder.next());
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.prepare();
            fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
            new Thread(this).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMetadata(String path) {
        try {
            AssetFileDescriptor descriptor = getAssets().openFd(path);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            TextView fileName = findViewById(R.id.fileName);
            TextView artist = findViewById(R.id.artist);
            fileName.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            artist.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String[]  getSongsList() {
        try {
            return getAssets().list(MUSIC_LOCATION);
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
        setMetadata(playlistHolder.current());
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();
        System.out.println("start" + total + ' ' + currentPosition);
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                Thread.sleep(500);

            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                return;
            }

        }
        System.out.println("stop");
    }
}