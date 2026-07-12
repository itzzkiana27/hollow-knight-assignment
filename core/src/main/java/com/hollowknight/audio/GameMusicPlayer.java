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

    private enum FadeMode {
        NONE,
        OUT_FOR_SWITCH,
        IN
    }

    private static final float FADE_DURATION = 0.85f;

    private final GameSettings settings;

    private Music currentMusic;

    private Track currentTrack;

    private Track desiredTrack;

    private Track queuedTrack;

    private FadeMode fadeMode = FadeMode.NONE;

    private float fadeTime;

    private float fadeStartVolume;

    public GameMusicPlayer(GameSettings settings) {
        this.settings = settings;
    }

    public void playMenuTheme() {
        play(Track.MENU);
    }

    public void playVictoryTheme() {
        play(Track.VICTORY);
    }

    public void playForRoom(String roomId, boolean falseKnightActive) {
        if ("false_knight_arena".equals(roomId) && falseKnightActive) {
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
        if (!settings.isMusicEnabled()) {
            stopCurrentMusic();
            queuedTrack = null;
            fadeMode = FadeMode.NONE;
            return;
        }

        if (currentMusic == null && desiredTrack != null) {
            startTrack(desiredTrack, 0f);
            startFadeIn();
            return;
        }

        if (currentMusic != null && fadeMode == FadeMode.NONE) {
            currentMusic.setVolume(settings.getMusicVolume());

            if (!currentMusic.isPlaying()) {
                currentMusic.play();
            }
        }
    }

    public void update(float delta) {
        if (!settings.isMusicEnabled()) {
            if (currentMusic != null) {
                stopCurrentMusic();
            }
            return;
        }

        if (currentMusic == null) {
            if (desiredTrack != null) {
                startTrack(desiredTrack, 0f);
                startFadeIn();
            }
            return;
        }

        float safeDelta = Math.min(Math.max(delta, 0f), 1f / 15f);

        if (fadeMode == FadeMode.OUT_FOR_SWITCH) {
            updateFadeOutForSwitch(safeDelta);
            return;
        }

        if (fadeMode == FadeMode.IN) {
            updateFadeIn(safeDelta);
            return;
        }

        currentMusic.setVolume(settings.getMusicVolume());

        if (!currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    @Override
    public void dispose() {
        stopCurrentMusic();
        queuedTrack = null;
        fadeMode = FadeMode.NONE;
    }

    private void play(Track track) {
        if (track == null) {
            return;
        }

        desiredTrack = track;

        if (!settings.isMusicEnabled()) {
            stopCurrentMusic();
            queuedTrack = null;
            fadeMode = FadeMode.NONE;
            return;
        }

        if (currentTrack == track && currentMusic != null && fadeMode != FadeMode.OUT_FOR_SWITCH) {
            return;
        }

        if (queuedTrack == track && fadeMode == FadeMode.OUT_FOR_SWITCH) {
            return;
        }

        queuedTrack = track;

        if (currentMusic == null) {
            startTrack(queuedTrack, 0f);
            queuedTrack = null;
            startFadeIn();
            return;
        }

        fadeMode = FadeMode.OUT_FOR_SWITCH;
        fadeTime = 0f;
        fadeStartVolume = currentMusic.getVolume();
    }

    private void updateFadeOutForSwitch(float delta) {
        fadeTime += delta;

        float progress = Math.min(1f, fadeTime / FADE_DURATION);

        currentMusic.setVolume(fadeStartVolume * (1f - progress));

        if (progress < 1f) {
            return;
        }

        stopCurrentMusic();

        Track nextTrack = queuedTrack;
        queuedTrack = null;

        if (nextTrack == null) {
            fadeMode = FadeMode.NONE;
            return;
        }

        startTrack(nextTrack, 0f);
        startFadeIn();
    }

    private void updateFadeIn(float delta) {
        fadeTime += delta;

        float progress = Math.min(1f, fadeTime / FADE_DURATION);

        currentMusic.setVolume(settings.getMusicVolume() * progress);

        if (progress < 1f) {
            return;
        }

        fadeMode = FadeMode.NONE;
        currentMusic.setVolume(settings.getMusicVolume());
    }

    private void startFadeIn() {
        if (currentMusic == null) {
            fadeMode = FadeMode.NONE;
            return;
        }

        fadeMode = FadeMode.IN;
        fadeTime = 0f;
        fadeStartVolume = 0f;
    }

    private void startTrack(Track track, float initialVolume) {
        if (track == null) {
            return;
        }

        FileHandle file = Gdx.files.internal(track.getPath());

        if (!file.exists()) {
            Gdx.app.log("GameMusicPlayer", "Missing music file: " + track.getPath());
            currentTrack = null;
            return;
        }

        currentMusic = Gdx.audio.newMusic(file);
        currentTrack = track;
        currentMusic.setLooping(true);
        currentMusic.setVolume(Math.max(0f, initialVolume));
        currentMusic.play();
    }

    private void stopCurrentMusic() {
        if (currentMusic == null) {
            currentTrack = null;
            return;
        }

        currentMusic.stop();
        currentMusic.dispose();
        currentMusic = null;
        currentTrack = null;
    }
}
