package com.hollowknight.controller;

import com.badlogic.gdx.Input;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.GameSettings;

public class GuideController {

    private final HollowKnightGame game;
    private final GameSettings settings;

    public GuideController(HollowKnightGame game) {
        this.game = game;
        this.settings = game.getSettings();
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

    public int getMoveRightKey() {
        return settings.getMoveRightKey();
    }

    public int getJumpKey() {
        return settings.getJumpKey();
    }

    public int getDashKey() {
        return settings.getDashKey();
    }

    public int getAttackKey() {
        return settings.getAttackKey();
    }

    public int getUpKey() {
        return com.hollowknight.model.input.KeyBindings.DEFAULT_UP;
    }

    public int getDownKey() {
        return com.hollowknight.model.input.KeyBindings.DEFAULT_DOWN;
    }

    public int getFocusKey() {
        return com.hollowknight.model.input.KeyBindings.DEFAULT_FOCUS;
    }

    public int getFireballKey() {
        return com.hollowknight.model.input.KeyBindings.DEFAULT_FIREBALL_TEST;
    }

    public int getScreamKey() {
        return com.hollowknight.model.input.KeyBindings.DEFAULT_SCREAM_TEST;
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }
}
