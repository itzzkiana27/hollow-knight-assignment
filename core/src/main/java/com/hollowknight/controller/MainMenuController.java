package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.view.theme.MenuThemeType;

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
        game.showGuideMenu();
    }


    public void openAchievements() {
        game.showAchievementsMenu();
    }

    public String getMenuThemeDisplayName() {
        return MenuThemeType
            .fromId(game.getSettings().getMenuTheme())
            .getDisplayName();
    }

    public void cycleMenuTheme() {
        MenuThemeType nextTheme = MenuThemeType
            .fromId(game.getSettings().getMenuTheme())
            .next();

        game.getSettings().setMenuTheme(
            nextTheme.getId()
        );

        game.getSettings().save();

        Gdx.app.postRunnable(
            game::showMainMenu
        );
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
