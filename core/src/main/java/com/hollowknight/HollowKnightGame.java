package com.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.hollowknight.controller.MainMenuController;
import com.hollowknight.controller.SettingsController;
import com.hollowknight.controller.StartGameController;
import com.hollowknight.model.GameSettings;
import com.hollowknight.view.screens.MainMenuScreen;
import com.hollowknight.view.screens.SettingsScreen;
import com.hollowknight.view.screens.StartGameScreen;
import com.hollowknight.localization.LocalizationManager;

public class HollowKnightGame extends Game {

    private GameSettings settings;
    private LocalizationManager localization;

    @Override
    public void create() {
        settings = new GameSettings();

        localization = new LocalizationManager(
            settings.getLanguage()
        );

        showMainMenu();
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void showMainMenu() {
        changeScreen(
            new MainMenuScreen(
                new MainMenuController(this)
            )
        );
    }

    public void showStartGameMenu() {
        changeScreen(
            new StartGameScreen(
                new StartGameController(this)
            )
        );
    }

    public void showSettingsMenu() {
        changeScreen(
            new SettingsScreen(
                new SettingsController(this)
            )
        );
    }

    private void changeScreen(Screen newScreen) {
        Screen oldScreen = getScreen();

        setScreen(newScreen);

        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }
    public LocalizationManager getLocalization() {
        return localization;
    }

    public void applyLanguage(String language) {
        settings.setLanguage(language);
        settings.save();

        localization.setLanguage(language);
    }
}
