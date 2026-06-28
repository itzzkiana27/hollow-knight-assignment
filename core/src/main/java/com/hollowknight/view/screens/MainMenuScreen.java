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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.MainMenuController;

public class MainMenuScreen extends ScreenAdapter {

    private final MainMenuController controller;

    private Stage stage;
    private Skin skin;

    public MainMenuScreen(MainMenuController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(
            new ScreenViewport()
        );

        skin = new Skin(
            Gdx.files.internal("ui/uiskin.json")
        );

        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    private void createMenu() {
        Table table = new Table();

        table.setFillParent(true);
        table.center();

        Label title = new Label(
            controller.text("main.title"),
            skin
        );

        title.setFontScale(1.7f);

        table.add(title)
            .padBottom(25f)
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

        stage.addActor(table);
    }

    private void addButton(
        Table table,
        String text,
        Runnable action
    ) {
        TextButton button = new TextButton(
            text,
            skin
        );

        button.getLabel().setFontScale(1.1f);

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
            .width(280f)
            .height(55f)
            .pad(6f)
            .row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            0.02f,
            0.02f,
            0.05f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
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

        if (skin != null) {
            skin.dispose();
        }
    }
}
