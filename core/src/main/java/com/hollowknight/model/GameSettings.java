package com.hollowknight.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.hollowknight.model.input.KeyBindings;

public class GameSettings {

    private static final String PREFERENCES_NAME =
        "hollow-knight-settings";

    private static final String CONTROLS_VERSION_KEY =
        "controlsVersion";

    private static final int CURRENT_CONTROLS_VERSION = 2;

    private static final String SLIDER_DEFAULTS_VERSION_KEY =
        "sliderDefaultsVersion";

    private static final int CURRENT_SLIDER_DEFAULTS_VERSION = 1;

    private static final float DEFAULT_MUSIC_VOLUME = 0.5f;
    private static final float DEFAULT_SOUND_EFFECTS_VOLUME = 0.5f;

    public static final float MIN_BRIGHTNESS = 0.3f;
    public static final float MAX_BRIGHTNESS = 1f;
    public static final float DEFAULT_BRIGHTNESS =
        (MIN_BRIGHTNESS + MAX_BRIGHTNESS) / 2f;

    private final Preferences preferences;

    private boolean musicEnabled;
    private boolean soundEffectsEnabled;

    private float musicVolume;
    private float soundEffectsVolume;
    private float brightness;

    private String musicStyle;
    private String language;
    private String menuTheme;

    private int moveLeftKey;
    private int moveRightKey;
    private int jumpKey;
    private int dashKey;
    private int attackKey;

    public GameSettings() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        load();
    }

    private void load() {
        musicEnabled =
            preferences.getBoolean("musicEnabled", true);

        soundEffectsEnabled =
            preferences.getBoolean("soundEffectsEnabled", true);

        musicVolume =
            preferences.getFloat(
                "musicVolume",
                DEFAULT_MUSIC_VOLUME
            );

        soundEffectsVolume =
            preferences.getFloat(
                "soundEffectsVolume",
                DEFAULT_SOUND_EFFECTS_VOLUME
            );

        brightness =
            preferences.getFloat(
                "brightness",
                DEFAULT_BRIGHTNESS
            );

        musicStyle =
            preferences.getString("musicStyle", "Original");

        language =
            preferences.getString("language", "en");

        menuTheme =
            preferences.getString("menuTheme", "voidheart");

        int controlsVersion = preferences.getInteger(
            CONTROLS_VERSION_KEY,
            0
        );

        if (controlsVersion < CURRENT_CONTROLS_VERSION) {
            /*
             * Earlier builds used A/D, Space, Shift, and J.
             * Migrate once to the assignment's required defaults.
             */
            setDefaultControls();
            save();
        } else {
            moveLeftKey = preferences.getInteger(
                "moveLeftKey",
                KeyBindings.DEFAULT_MOVE_LEFT
            );

            moveRightKey = preferences.getInteger(
                "moveRightKey",
                KeyBindings.DEFAULT_MOVE_RIGHT
            );

            jumpKey = preferences.getInteger(
                "jumpKey",
                KeyBindings.DEFAULT_JUMP
            );

            dashKey = preferences.getInteger(
                "dashKey",
                KeyBindings.DEFAULT_DASH
            );

            attackKey = preferences.getInteger(
                "attackKey",
                KeyBindings.DEFAULT_ATTACK
            );
        }

        migrateLegacySliderDefaults();
    }

    private void migrateLegacySliderDefaults() {
        int defaultsVersion = preferences.getInteger(
            SLIDER_DEFAULTS_VERSION_KEY,
            0
        );

        if (
            defaultsVersion
                >= CURRENT_SLIDER_DEFAULTS_VERSION
        ) {
            return;
        }

        /*
         * Preserve values the player deliberately changed, but move the
         * old 80%, 80%, 100% defaults to the new centered positions.
         */
        if (
            !preferences.contains("musicVolume")
                || isApproximately(musicVolume, 0.8f)
        ) {
            musicVolume = DEFAULT_MUSIC_VOLUME;
        }

        if (
            !preferences.contains("soundEffectsVolume")
                || isApproximately(soundEffectsVolume, 0.8f)
        ) {
            soundEffectsVolume =
                DEFAULT_SOUND_EFFECTS_VOLUME;
        }

        if (
            !preferences.contains("brightness")
                || isApproximately(brightness, 1f)
        ) {
            brightness = DEFAULT_BRIGHTNESS;
        }

        preferences.putInteger(
            SLIDER_DEFAULTS_VERSION_KEY,
            CURRENT_SLIDER_DEFAULTS_VERSION
        );

        save();
    }

    private boolean isApproximately(
        float first,
        float second
    ) {
        return Math.abs(first - second) < 0.0001f;
    }

    public void save() {
        preferences.putBoolean(
            "musicEnabled",
            musicEnabled
        );

        preferences.putBoolean(
            "soundEffectsEnabled",
            soundEffectsEnabled
        );

        preferences.putFloat(
            "musicVolume",
            musicVolume
        );

        preferences.putFloat(
            "soundEffectsVolume",
            soundEffectsVolume
        );

        preferences.putFloat(
            "brightness",
            brightness
        );

        preferences.putString(
            "musicStyle",
            musicStyle
        );

        preferences.putString(
            "language",
            language
        );

        preferences.putString(
            "menuTheme",
            menuTheme
        );

        preferences.putInteger(
            "moveLeftKey",
            moveLeftKey
        );

        preferences.putInteger(
            "moveRightKey",
            moveRightKey
        );

        preferences.putInteger(
            "jumpKey",
            jumpKey
        );

        preferences.putInteger(
            "dashKey",
            dashKey
        );

        preferences.putInteger(
            "attackKey",
            attackKey
        );

        preferences.putInteger(
            CONTROLS_VERSION_KEY,
            CURRENT_CONTROLS_VERSION
        );

        preferences.flush();
    }

    private void setDefaultControls() {
        moveLeftKey = KeyBindings.DEFAULT_MOVE_LEFT;
        moveRightKey = KeyBindings.DEFAULT_MOVE_RIGHT;
        jumpKey = KeyBindings.DEFAULT_JUMP;
        dashKey = KeyBindings.DEFAULT_DASH;
        attackKey = KeyBindings.DEFAULT_ATTACK;
    }

    public void resetControls() {
        setDefaultControls();
        save();
    }

    public void resetAudio() {
        musicEnabled = true;
        soundEffectsEnabled = true;

        musicVolume = DEFAULT_MUSIC_VOLUME;
        soundEffectsVolume =
            DEFAULT_SOUND_EFFECTS_VOLUME;

        musicStyle = "Original";

        save();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public void setSoundEffectsEnabled(
        boolean soundEffectsEnabled
    ) {
        this.soundEffectsEnabled = soundEffectsEnabled;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume =
            MathUtils.clamp(musicVolume, 0f, 1f);
    }

    public float getSoundEffectsVolume() {
        return soundEffectsVolume;
    }

    public void setSoundEffectsVolume(
        float soundEffectsVolume
    ) {
        this.soundEffectsVolume =
            MathUtils.clamp(soundEffectsVolume, 0f, 1f);
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness =
            MathUtils.clamp(
                brightness,
                MIN_BRIGHTNESS,
                MAX_BRIGHTNESS
            );
    }

    public String getMusicStyle() {
        return musicStyle;
    }

    public void setMusicStyle(String musicStyle) {
        this.musicStyle = musicStyle;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMenuTheme() {
        return menuTheme;
    }

    public void setMenuTheme(String menuTheme) {
        if (menuTheme == null || menuTheme.isEmpty()) {
            this.menuTheme = "voidheart";
        } else {
            this.menuTheme = menuTheme;
        }
    }
    public void resetAll() {
        musicEnabled = true;
        soundEffectsEnabled = true;

        musicVolume = DEFAULT_MUSIC_VOLUME;
        soundEffectsVolume =
            DEFAULT_SOUND_EFFECTS_VOLUME;

        brightness = DEFAULT_BRIGHTNESS;
        musicStyle = "Original";
        language = "en";
        menuTheme = "voidheart";

        setDefaultControls();

        save();
    }
    public int getMoveLeftKey() {
        return moveLeftKey;
    }

    public void setMoveLeftKey(int moveLeftKey) {
        this.moveLeftKey = moveLeftKey;
    }

    public int getMoveRightKey() {
        return moveRightKey;
    }

    public void setMoveRightKey(int moveRightKey) {
        this.moveRightKey = moveRightKey;
    }

    public int getJumpKey() {
        return jumpKey;
    }

    public void setJumpKey(int jumpKey) {
        this.jumpKey = jumpKey;
    }

    public int getDashKey() {
        return dashKey;
    }

    public void setDashKey(int dashKey) {
        this.dashKey = dashKey;
    }

    public int getAttackKey() {
        return attackKey;
    }

    public void setAttackKey(int attackKey) {
        this.attackKey = attackKey;
    }
}
