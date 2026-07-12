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

    public String format(String key, Object... arguments) {
        return game.getLocalization().format(key, arguments);
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
        MenuThemeType theme = MenuThemeType.fromId(game.getSettings().getMenuTheme());

        return text(theme.getLocalizationKey());
    }

    public void cycleMenuTheme() {
        MenuThemeType nextTheme = MenuThemeType.fromId(game.getSettings().getMenuTheme()).next();

        game.getSettings().setMenuTheme(nextTheme.getId());

        game.getSettings().save();

        Gdx.app.postRunnable(game::showMainMenu);
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
