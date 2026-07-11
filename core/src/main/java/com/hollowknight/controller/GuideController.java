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
        return settings.getUpKey();
    }

    public int getDownKey() {
        return settings.getDownKey();
    }

    public int getAlternateAttackKey() {
        return settings.getAlternateAttackKey();
    }

    public int getFocusKey() {
        return settings.getFocusKey();
    }

    public int getFireballKey() {
        return settings.getFireballKey();
    }

    public int getScreamKey() {
        return settings.getScreamKey();
    }

    public int getInteractKey() {
        return settings.getInteractKey();
    }

    public int getDialogueAdvanceKey() {
        return settings.getDialogueAdvanceKey();
    }

    public int getInventoryKey() {
        return settings.getInventoryKey();
    }

    public int getPauseKey() {
        return settings.getPauseKey();
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }
}
