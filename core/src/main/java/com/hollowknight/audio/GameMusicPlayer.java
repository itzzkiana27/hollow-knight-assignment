package com.hollowknight.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.hollowknight.model.GameSettings;

public final class GameMusicPlayer implements Disposable {

    public enum Track {
        MENU("audio/music/hollow_knight.mp3"),
        CROSSROADS("audio/music/crossroads.mp3"),
        CITY_OF_TEARS("audio/music/city_of_tears.mp3"),
        FALSE_KNIGHT("audio/music/false_knight.mp3"),
        VICTORY("audio/music/furious_gods.mp3");

        private final String path;

        Track(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    private final GameSettings settings;

    private Music currentMusic;
    private Track currentTrack;

    public GameMusicPlayer(
        GameSettings settings
    ) {
        this.settings = settings;
    }

    public void playMenuTheme() {
        play(Track.MENU);
    }

    public void playVictoryTheme() {
        play(Track.VICTORY);
    }

    public void playForRoom(
        String roomId,
        boolean falseKnightActive
    ) {
        if (
            "false_knight_arena".equals(roomId)
                && falseKnightActive
        ) {
            play(Track.FALSE_KNIGHT);
            return;
        }

        if ("city_of_tears".equals(roomId)) {
            play(Track.CITY_OF_TEARS);
            return;
        }

        play(Track.CROSSROADS);
    }

    public void refreshSettings() {
        if (currentTrack == null) {
            return;
        }

        if (!settings.isMusicEnabled()) {
            stopCurrentMusic();
            return;
        }

        if (currentMusic == null) {
            Track requestedTrack = currentTrack;
            currentTrack = null;
            play(requestedTrack);
            return;
        }

        currentMusic.setVolume(
            settings.getMusicVolume()
        );

        if (!currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    private void play(
        Track track
    ) {
        if (track == null) {
            return;
        }

        if (
            currentTrack == track
                && currentMusic != null
        ) {
            refreshSettings();
            return;
        }

        currentTrack = track;

        stopCurrentMusic();

        if (!settings.isMusicEnabled()) {
            return;
        }

        FileHandle file = Gdx.files.internal(
            track.getPath()
        );

        if (!file.exists()) {
            Gdx.app.log(
                "GameMusicPlayer",
                "Missing music file: " + track.getPath()
            );
            return;
        }

        currentMusic = Gdx.audio.newMusic(file);
        currentMusic.setLooping(true);
        currentMusic.setVolume(
            settings.getMusicVolume()
        );
        currentMusic.play();
    }

    private void stopCurrentMusic() {
        if (currentMusic == null) {
            return;
        }

        currentMusic.stop();
        currentMusic.dispose();
        currentMusic = null;
    }

    @Override
    public void dispose() {
        stopCurrentMusic();
    }
}
