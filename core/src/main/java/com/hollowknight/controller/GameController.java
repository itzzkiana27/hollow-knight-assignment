package com.hollowknight.controller;

import com.hollowknight.HollowKnightGame;

public class GameController {

    private final HollowKnightGame game;

    public GameController(HollowKnightGame game) {
        this.game = game;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
