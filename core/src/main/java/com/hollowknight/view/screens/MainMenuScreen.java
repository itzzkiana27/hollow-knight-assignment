package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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
import com.hollowknight.controller.MainMenuController;
import com.hollowknight.view.theme.MenuThemeSkin;

public class MainMenuScreen extends ScreenAdapter {

    private final MainMenuController controller;

    private Stage stage;
    private Skin skin;
    private MenuThemeSkin menuTheme;

    public MainMenuScreen(MainMenuController controller) {
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
    }

    private void createMenu() {
        Table table = new Table();

        table.setFillParent(true);
        table.center();
        table.defaults().pad(7f);

        table.add(menuTheme.createTitleLogo(420f))
            .width(420f)
            .height(160f)
            .padBottom(8f)
            .row();

        table.add(menuTheme.createOrnament(260f))
            .width(260f)
            .height(30f)
            .padBottom(92f)
            .row();

        addButton(
            table,
            controller.text("main.startGame"),
            controller::startGame
        );

        addButton(
            table,
            controller.text("main.settings"),
            controller::openSettings
        );

        addButton(
            table,
            controller.text("main.guide"),
            controller::openGuide
        );

        addButton(
            table,
            controller.text("main.achievements"),
            controller::openAchievements
        );

        addButton(
            table,
            controller.text("main.quit"),
            controller::quitGame
        );

        TextButton themeButton = menuTheme.createMenuButton(
            "Theme: " + controller.getMenuThemeDisplayName()
        );
        themeButton.getLabel().setFontScale(0.82f);
        themeButton.getLabel().setAlignment(Align.center);
        themeButton.getLabel().setColor(0.55f, 0.62f, 0.70f, 0.82f);

        themeButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.cycleMenuTheme();
            }
        });

        table.add(themeButton)
            .width(300f)
            .height(36f)
            .padTop(28f)
            .row();

        stage.addActor(table);
    }

    private void addButton(
        Table table,
        String text,
        Runnable action
    ) {
        TextButton button = menuTheme.createMenuButton(text);

        button.getLabel().setFontScale(1.45f);
        button.getLabel().setAlignment(Align.center);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                action.run();
            }
        });

        table.add(button)
            .width(360f)
            .height(48f)
            .row();
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
            false
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
        if (
            Gdx.input.getInputProcessor() == stage
        ) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }

        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
