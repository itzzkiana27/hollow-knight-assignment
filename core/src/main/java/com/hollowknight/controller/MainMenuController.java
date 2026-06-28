package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.hollowknight.HollowKnightGame;

public class MainMenuController {

    private final HollowKnightGame game;

    public MainMenuController(HollowKnightGame game) {
        this.game = game;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public void startGame() {
        game.showStartGameMenu();
    }

    public void openSettings() {
        game.showSettingsMenu();
    }

    public void openGuide() {
        Gdx.app.log(
            "MainMenu",
            "Guide selected"
        );

        // Later:
        // game.showGuideMenu();
    }

    public void openAchievements() {
        Gdx.app.log(
            "MainMenu",
            "Achievements selected"
        );

        // Later:
        // game.showAchievementsMenu();
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
