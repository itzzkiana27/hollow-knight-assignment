package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.EndGameController;
import com.hollowknight.model.EndGameStats;
import com.hollowknight.view.theme.MenuThemeSkin;

public final class EndGameScreen extends ScreenAdapter {

    private static final String[] VICTORY_MUSIC_PATHS = {
        "audio/music/victory.ogg",
        "audio/music/victory.mp3",
        "audio/music/victory.wav"
    };

    private final EndGameController controller;

    private Stage stage;
    private Skin skin;
    private MenuThemeSkin menuTheme;
    private Music victoryMusic;

    public EndGameScreen(EndGameController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(
            new ScreenViewport()
        );

        menuTheme = MenuThemeSkin.fromSettings();
        skin = menuTheme.getSkin();

        Gdx.input.setInputProcessor(stage);

        createMenu();
        startVictoryMusicIfAvailable();
    }

    private void createMenu() {
        float contentWidth = Math.min(
            620f,
            Gdx.graphics.getWidth() * 0.82f
        );

        float buttonWidth = Math.min(
            280f,
            contentWidth * 0.62f
        );

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.pad(24f);
        table.defaults().pad(6f);

        Table panel = new Table();
        panel.pad(32f);
        panel.setBackground(
            menuTheme.panelDrawable(0.56f)
        );

        Label title = menuTheme.createTitleLabel(
            controller.text("end.title")
        );

        title.setFontScale(2.20f);
        title.setAlignment(Align.center);

        panel.add(title)
            .width(contentWidth)
            .padBottom(5f)
            .row();

        panel.add(menuTheme.createOrnament(310f))
            .width(310f)
            .height(34f)
            .padBottom(18f)
            .row();

        EndGameStats stats =
            controller.getStats();

        addStatLine(
            panel,
            controller.text("end.deaths"),
            String.valueOf(stats.getDeathCount()),
            contentWidth
        );

        addStatLine(
            panel,
            controller.text("end.enemiesKilled"),
            String.valueOf(stats.getTotalEnemiesKilled()),
            contentWidth
        );

        addStatLine(
            panel,
            controller.text("end.totalTime"),
            controller.getFormattedTime(),
            contentWidth
        );

        addButton(
            panel,
            controller.text("end.restart"),
            controller::restartGame,
            buttonWidth
        );

        addButton(
            panel,
            controller.text("end.mainMenu"),
            controller::backToMainMenu,
            buttonWidth
        );

        table.add(panel)
            .width(contentWidth + 80f);

        stage.addActor(table);
    }

    private void addStatLine(
        Table table,
        String label,
        String value,
        float contentWidth
    ) {
        Label stat = menuTheme.createBodyLabel(
            label + ": " + value
        );

        stat.setFontScale(1.12f);
        stat.setAlignment(Align.center);

        table.add(stat)
            .width(contentWidth)
            .height(34f)
            .row();
    }

    private void addButton(
        Table table,
        String text,
        Runnable action,
        float buttonWidth
    ) {
        TextButton button = menuTheme.createMenuButton(
            text
        );

        button.getLabel().setFontScale(1.12f);

        button.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    stopVictoryMusic();
                    action.run();
                }
            }
        );

        table.add(button)
            .width(buttonWidth)
            .height(46f)
            .padTop(9f)
            .row();
    }

    private void startVictoryMusicIfAvailable() {
        if (!controller.shouldPlayVictoryMusic()) {
            return;
        }

        for (String path : VICTORY_MUSIC_PATHS) {
            FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                continue;
            }

            victoryMusic =
                Gdx.audio.newMusic(file);

            victoryMusic.setLooping(true);
            victoryMusic.setVolume(
                controller.getVictoryMusicVolume()
            );
            victoryMusic.play();
            return;
        }
    }

    private void stopVictoryMusic() {
        if (victoryMusic == null) {
            return;
        }

        victoryMusic.stop();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            0.01f,
            0.01f,
            0.015f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        menuTheme.drawBackground(
            delta,
            false,
            0.58f,
            0.72f
        );

        stage.act(
            Math.min(delta, 1f / 30f)
        );

        stage.draw();
    }

    @Override
    public void resize(
        int width,
        int height
    ) {
        stage.getViewport().update(
            width,
            height,
            true
        );
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        stopVictoryMusic();

        if (victoryMusic != null) {
            victoryMusic.dispose();
        }

        if (stage != null) {
            stage.dispose();
        }

        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
