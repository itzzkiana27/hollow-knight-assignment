package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.hollowknight.HollowKnightGame;

public class StartGameController {

    private final HollowKnightGame game;

    public StartGameController(HollowKnightGame game) {
        this.game = game;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public String format(String key, Object... arguments) {
        return game.getLocalization().format(key, arguments);
    }

    public void startNewGame() {
        game.showGameScreen();
        //opening actual game screen
    }

    public void selectSaveSlot(int slotNumber) {
        Gdx.app.log(
            "StartGame",
            "Save slot " + slotNumber + " selected"
        );

        // Loading saved games will be implemented later.
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }
}
