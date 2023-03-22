package com.fresh.musicplayer;

/**
 * Класс хранящий путь и названия треков
 */
public class PlaylistHolder {
    private final String path;
    private final String[] playlist;
    private int ind = 0;

    public PlaylistHolder(String path, String[] playlist) {
        this.path = path;
        this.playlist = playlist;
    }

    /**
     * Метод получения пути текущего трека
     * @return путь текущего трека
     */
    public String current() {
        return path + '/' + playlist[ind];
    }

    /**
     * Метод получения пути следующего трека. Увеличивает индекс плэйлиста на 1
     * @return путь следующего трека
     */
    public String next() {
        ind++;
        if (ind >= playlist.length) ind = 0;
        return path + '/' + playlist[ind];
    }

    /**
     * Метод получения пути предыдущего трека. Уменьшает индекс плэйлиста на 1
     * @return путь предыдущего трека
     */
    public String previous() {
        ind--;
        if (ind < 0) ind = playlist.length;
        return path + '/' + playlist[ind];
    }
}
