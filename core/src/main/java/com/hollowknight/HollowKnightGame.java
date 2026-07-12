package com.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hollowknight.controller.AchievementsController;
import com.hollowknight.controller.GuideController;
import com.hollowknight.controller.EndGameController;
import com.hollowknight.controller.MainMenuController;
import com.hollowknight.controller.SettingsController;
import com.hollowknight.controller.StartGameController;
import com.hollowknight.localization.LocalizationManager;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.save.SaveManager;
import com.hollowknight.model.achievement.AchievementManager;
import com.hollowknight.model.EndGameStats;
import com.hollowknight.audio.GameMusicPlayer;
import com.hollowknight.view.screens.*;
import com.hollowknight.controller.GameController;

public class HollowKnightGame extends Game {

    private GameSettings settings;
    private LocalizationManager localization;
    private AchievementManager achievementManager;
    private GameMusicPlayer musicPlayer;
    private ShapeRenderer brightnessRenderer;
    private Matrix4 brightnessProjection;
    private int activeSaveSlot;
    private GameController suspendedGameController;

    @Override
    public void create() {
        settings = new GameSettings();

        localization = new LocalizationManager(
            settings.getLanguage()
        );

        achievementManager =
            new AchievementManager();

        musicPlayer = new GameMusicPlayer(
            settings
        );

        brightnessRenderer = new ShapeRenderer();
        brightnessProjection = new Matrix4();

        activeSaveSlot =
            SaveManager.DEFAULT_SLOT;

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

    public void refreshMusicSettings() {
        if (musicPlayer != null) {
            musicPlayer.refreshSettings();
        }
    }

    public void playGameplayMusic(
        String roomId,
        boolean falseKnightActive
    ) {
        if (musicPlayer != null) {
            musicPlayer.playForRoom(
                roomId,
                falseKnightActive
            );
        }
    }

    public int getActiveSaveSlot() {
        return activeSaveSlot;
    }

    public void setActiveSaveSlot(
        int activeSaveSlot
    ) {
        this.activeSaveSlot =
            SaveManager.normalizeSlot(
                activeSaveSlot
            );
    }

    public void applyLanguage(String language) {
        settings.setLanguage(language);
        settings.save();

        localization.setLanguage(language);
    }

    public void showMainMenu() {
        if (musicPlayer != null) {
            musicPlayer.playMenuTheme();
        }

        changeScreen(
            new MainMenuScreen(
                new MainMenuController(this)
            )
        );
    }

    public void showStartGameMenu() {
        if (musicPlayer != null) {
            musicPlayer.playMenuTheme();
        }

        changeScreen(
            new StartGameScreen(
                new StartGameController(this)
            )
        );
    }

    public void showSettingsMenu() {
        suspendedGameController = null;
        showSettingsScreen();
    }

    public void showSettingsMenuFromPause(
        GameController controller
    ) {
        Screen currentScreen = getScreen();

        if (currentScreen instanceof GameScreen) {
            ((GameScreen) currentScreen)
                .preserveControllerOnDispose();
        }

        suspendedGameController = controller;
        showSettingsScreen();
    }

    public void refreshSettingsMenu() {
        showSettingsScreen();
    }

    public void closeSettingsMenu() {
        if (suspendedGameController == null) {
            showMainMenu();
            return;
        }

        GameController controller =
            suspendedGameController;

        suspendedGameController = null;

        changeScreen(
            new GameScreen(
                controller,
                true
            )
        );

        controller.resumeAfterSettings();
    }

    private void showSettingsScreen() {
        if (musicPlayer != null) {
            musicPlayer.playMenuTheme();
        }

        changeScreen(
            new SettingsScreen(
                new SettingsController(this)
            )
        );
    }

    public void showGuideMenu() {
        if (musicPlayer != null) {
            musicPlayer.playMenuTheme();
        }

        changeScreen(
            new GuideScreen(
                new GuideController(this)
            )
        );
    }

    public void showAchievementsMenu() {
        if (musicPlayer != null) {
            musicPlayer.playMenuTheme();
        }

        changeScreen(
            new AchievementsScreen(
                new AchievementsController(this)
            )
        );
    }

    public void showEndGameScreen(
        EndGameStats stats
    ) {
        if (musicPlayer != null) {
            musicPlayer.playVictoryTheme();
        }

        Texture gameplaySnapshot = captureCurrentScreenTexture();

        changeScreen(
            new EndGameScreen(
                new EndGameController(
                    this,
                    stats
                ),
                gameplaySnapshot
            )
        );
    }

    private Texture captureCurrentScreenTexture() {
        try {
            Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(
                0,
                0,
                Gdx.graphics.getBackBufferWidth(),
                Gdx.graphics.getBackBufferHeight()
            );

            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void changeScreen(Screen newScreen) {
        Screen oldScreen = getScreen();

        setScreen(newScreen);

        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }


    @Override
    public void render() {
        super.render();

        drawBrightnessOverlay();

        if (musicPlayer != null) {
            musicPlayer.update(
                Gdx.graphics.getDeltaTime()
            );
        }
    }

    private void drawBrightnessOverlay() {
        if (
            settings == null
                || brightnessRenderer == null
        ) {
            return;
        }

        float darkness = 1f - settings.getBrightness();

        if (darkness <= 0f) {
            return;
        }

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        brightnessProjection.setToOrtho2D(
            0f,
            0f,
            width,
            height
        );

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        brightnessRenderer.setProjectionMatrix(
            brightnessProjection
        );
        brightnessRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );
        brightnessRenderer.setColor(
            0f,
            0f,
            0f,
            darkness
        );
        brightnessRenderer.rect(
            0f,
            0f,
            width,
            height
        );
        brightnessRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();

        if (currentScreen != null) {
            currentScreen.dispose();
        }

        if (suspendedGameController != null) {
            suspendedGameController.dispose();
            suspendedGameController = null;
        }

        if (musicPlayer != null) {
            musicPlayer.dispose();
        }

        if (brightnessRenderer != null) {
            brightnessRenderer.dispose();
        }
    }
    public void showGameScreen() {
        showGameScreenForSlot(
            activeSaveSlot
        );
    }

    public void showGameScreenForSlot(
        int saveSlot
    ) {
        setActiveSaveSlot(
            saveSlot
        );

        changeScreen(
            new GameScreen(
                new GameController(
                    this,
                    false,
                    activeSaveSlot
                )
            )
        );
    }

    public void showLoadedGameScreen() {
        showLoadedGameScreenForSlot(
            activeSaveSlot
        );
    }

    public void showLoadedGameScreenForSlot(
        int saveSlot
    ) {
        setActiveSaveSlot(
            saveSlot
        );

        changeScreen(
            new GameScreen(
                new GameController(
                    this,
                    true,
                    activeSaveSlot
                )
            )
        );
    }
}
