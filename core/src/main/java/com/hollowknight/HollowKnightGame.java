package com.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.hollowknight.controller.AchievementsController;
import com.hollowknight.controller.GuideController;
import com.hollowknight.controller.MainMenuController;
import com.hollowknight.controller.SettingsController;
import com.hollowknight.controller.StartGameController;
import com.hollowknight.localization.LocalizationManager;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.achievement.AchievementManager;
import com.hollowknight.view.screens.AchievementsScreen;
import com.hollowknight.view.screens.GuideScreen;
import com.hollowknight.view.screens.MainMenuScreen;
import com.hollowknight.view.screens.SettingsScreen;
import com.hollowknight.view.screens.StartGameScreen;
import com.hollowknight.controller.GameController;
import com.hollowknight.view.screens.GameScreen;

public class HollowKnightGame extends Game {

    private GameSettings settings;
    private LocalizationManager localization;
    private AchievementManager achievementManager;

    @Override
    public void create() {
        settings = new GameSettings();

        localization = new LocalizationManager(
            settings.getLanguage()
        );

        achievementManager =
            new AchievementManager();

        showMainMenu();
    }

    public GameSettings getSettings() {
        return settings;
    }

    public LocalizationManager getLocalization() {
        return localization;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public void applyLanguage(String language) {
        settings.setLanguage(language);
        settings.save();

        localization.setLanguage(language);
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

    public void showGuideMenu() {
        changeScreen(
            new GuideScreen(
                new GuideController(this)
            )
        );
    }

    public void showAchievementsMenu() {
        changeScreen(
            new AchievementsScreen(
                new AchievementsController(this)
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

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();

        if (currentScreen != null) {
            currentScreen.dispose();
        }
    }
    public void showGameScreen() {
        changeScreen(
            new GameScreen(
                new GameController(this)
            )
        );
    }
}
