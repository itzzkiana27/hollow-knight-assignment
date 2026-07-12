package com.hollowknight.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.hollowknight.model.GameSettings;

public final class GameSfxPlayer {

    private static final String BASE_PATH = "audio/sfx/";

    private final GameSettings settings;

    private final Sound[] nailSlashSounds;
    private final Sound[] playerDamageSounds;
    private final Sound[] soulGainSounds;

    private final Sound enemyDamageSound;
    private final Sound breakableWallHit1Sound;
    private final Sound breakableWallHit2Sound;
    private final Sound breakableWallDestroyedSound;
    private final Sound focusReadySound;
    private final Sound focusChargingSound;
    private final Sound focusHealSound;

    private long focusChargingSoundId = -1L;

    public GameSfxPlayer(GameSettings settings) {
        this.settings = settings;

        nailSlashSounds =
                loadMany("sword_1.wav", "sword_2.wav", "sword_3.wav", "sword_4.wav", "sword_5.wav");

        playerDamageSounds = loadMany("hero_damage.wav", "hero_damage_less_harsh.wav");

        soulGainSounds =
                loadMany(
                        "soul_pickup_1.wav",
                        "soul_pickup_2.wav",
                        "soul_pickup_3.wav",
                        "soul_pickup_4.wav",
                        "soul_pickup_5.wav",
                        "soul_pickup_6.wav",
                        "soul_pickup_7.wav");

        enemyDamageSound = load("sword_hit_reject.wav");
        breakableWallHit1Sound = load("breakable_wall/breakable_wall_hit_1.wav");
        breakableWallHit2Sound = load("breakable_wall/breakable_wall_hit_2.wav");
        breakableWallDestroyedSound = load("breakable_wall/breakable_wall_destroyed.wav");
        focusReadySound = load("focus_ready.wav");
        focusChargingSound = load("focus_health_charging.wav");
        focusHealSound = load("focus_health_heal.wav");
    }

    public void playNailSlash() {
        playRandom(nailSlashSounds, 0.62f);
    }

    public void playPlayerDamage() {
        playRandom(playerDamageSounds, 0.78f);
    }

    public void playEnemyDamage() {
        play(enemyDamageSound, 0.42f);
    }

    public void playBreakableWallHit1() {
        play(breakableWallHit1Sound, 0.72f);
    }

    public void playBreakableWallHit2() {
        play(breakableWallHit2Sound, 0.78f);
    }

    public void playBreakableWallDestroyed() {
        play(breakableWallDestroyedSound, 0.86f);
    }

    public void playSoulGain() {
        playRandom(soulGainSounds, 0.55f);
    }

    public void playFocusStart() {
        play(focusReadySound, 0.70f);
        startFocusCharging();
    }

    public void playFocusHeal() {
        stopFocusCharging();
        play(focusHealSound, 0.82f);
    }

    public void startFocusCharging() {
        if (focusChargingSound == null || focusChargingSoundId != -1L || !canPlay()) {
            return;
        }

        focusChargingSoundId = focusChargingSound.loop(scaledVolume(0.42f));
    }

    public void stopFocusCharging() {
        if (focusChargingSound == null || focusChargingSoundId == -1L) {
            return;
        }

        focusChargingSound.stop(focusChargingSoundId);
        focusChargingSoundId = -1L;
    }

    public void dispose() {
        stopFocusCharging();

        disposeAll(nailSlashSounds);
        disposeAll(playerDamageSounds);
        disposeAll(soulGainSounds);

        dispose(enemyDamageSound);
        dispose(breakableWallHit1Sound);
        dispose(breakableWallHit2Sound);
        dispose(breakableWallDestroyedSound);
        dispose(focusReadySound);
        dispose(focusChargingSound);
        dispose(focusHealSound);
    }

    private Sound[] loadMany(String... filenames) {
        Sound[] sounds = new Sound[filenames.length];

        for (int index = 0; index < filenames.length; index++) {
            sounds[index] = load(filenames[index]);
        }

        return sounds;
    }

    private Sound load(String filename) {
        FileHandle file = Gdx.files.internal(BASE_PATH + filename);

        if (!file.exists()) {
            return null;
        }

        return Gdx.audio.newSound(file);
    }

    private void playRandom(Sound[] sounds, float volume) {
        if (sounds == null || sounds.length == 0) {
            return;
        }

        int startIndex = MathUtils.random(sounds.length - 1);

        for (int offset = 0; offset < sounds.length; offset++) {
            Sound sound = sounds[(startIndex + offset) % sounds.length];

            if (sound != null) {
                play(sound, volume);
                return;
            }
        }
    }

    private void play(Sound sound, float volume) {
        if (sound == null || !canPlay()) {
            return;
        }

        sound.play(scaledVolume(volume));
    }

    private boolean canPlay() {
        return settings.isSoundEffectsEnabled() && settings.getSoundEffectsVolume() > 0f;
    }

    private float scaledVolume(float volume) {
        return MathUtils.clamp(volume * settings.getSoundEffectsVolume(), 0f, 1f);
    }

    private void disposeAll(Sound[] sounds) {
        if (sounds == null) {
            return;
        }

        for (Sound sound : sounds) {
            dispose(sound);
        }
    }

    private void dispose(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }
}
