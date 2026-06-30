package com.hollowknight.controller;

import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class SettingsController {

    private final HollowKnightGame game;
    private final GameSettings settings;

    public SettingsController(HollowKnightGame game) {
        this.game = game;
        this.settings = game.getSettings();
    }

    public boolean isMusicEnabled() {
        return settings.isMusicEnabled();
    }

    public void setMusicEnabled(boolean enabled) {
        settings.setMusicEnabled(enabled);
    }

    public float getMusicVolume() {
        return settings.getMusicVolume();
    }

    public void setMusicVolume(float volume) {
        settings.setMusicVolume(volume);
    }

    public boolean isSoundEffectsEnabled() {
        return settings.isSoundEffectsEnabled();
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        settings.setSoundEffectsEnabled(enabled);
    }

    public float getSoundEffectsVolume() {
        return settings.getSoundEffectsVolume();
    }

    public void setSoundEffectsVolume(float volume) {
        settings.setSoundEffectsVolume(volume);
    }

    public float getBrightness() {
        return settings.getBrightness();
    }

    public void setBrightness(float brightness) {
        settings.setBrightness(brightness);
    }

    public String getMusicStyle() {
        return settings.getMusicStyle();
    }

    public void setMusicStyle(String musicStyle) {
        settings.setMusicStyle(musicStyle);
    }

    public String getLanguage() {
        return settings.getLanguage();
    }

    public void setLanguage(String language) {
        game.applyLanguage(language);

        // Recreate the Settings screen after the current click event finishes.
        Gdx.app.postRunnable(game::showSettingsMenu);
    }
    public void resetAudio() {
        settings.resetAudio();
    }
    public void resetAllSettings() {
        settings.resetAll();

        game.getLocalization().setLanguage(
            settings.getLanguage()
        );

        Gdx.app.postRunnable(
            game::showSettingsMenu
        );
    }

    public void backToMainMenu() {
        settings.save();
        game.showMainMenu();
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }


    public String keyName(int keycode) {
        return Input.Keys.toString(keycode);
    }

    public int getMoveLeftKey() {
        return settings.getMoveLeftKey();
    }

    public void setMoveLeftKey(int key) {
        settings.setMoveLeftKey(key);
    }

    public int getMoveRightKey() {
        return settings.getMoveRightKey();
    }

    public void setMoveRightKey(int key) {
        settings.setMoveRightKey(key);
    }

    public int getJumpKey() {
        return settings.getJumpKey();
    }

    public void setJumpKey(int key) {
        settings.setJumpKey(key);
    }

    public int getDashKey() {
        return settings.getDashKey();
    }

    public void setDashKey(int key) {
        settings.setDashKey(key);
    }

    public int getAttackKey() {
        return settings.getAttackKey();
    }

    public void setAttackKey(int key) {
        settings.setAttackKey(key);
    }

    public void resetControls() {
        settings.resetControls();
    }

    public void saveSettings() {
        settings.save();
    }
}
