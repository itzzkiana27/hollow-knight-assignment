package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
import com.hollowknight.controller.GameController;

public class GameScreen extends ScreenAdapter {

    private final GameController controller;

    private Stage stage;
    private Skin skin;
    private InputMultiplexer inputMultiplexer;

    public GameScreen(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(
            Gdx.files.internal("ui/uiskin.json")
        );

        createTemporaryGameView();
        configureInput();
    }

    private void createTemporaryGameView() {
        Table table = new Table();

        table.setFillParent(true);
        table.center();

        Label title = new Label(
            controller.text("game.placeholderTitle"),
            skin
        );

        title.setFontScale(1.6f);

        Label description = new Label(
            controller.text("game.placeholderDescription"),
            skin
        );

        description.setWrap(true);

        TextButton backButton = new TextButton(
            controller.text("game.returnToMainMenu"),
            skin
        );

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.returnToMainMenu();
            }
        });

        table.add(title)
            .padBottom(20f)
            .row();

        table.add(description)
            .width(500f)
            .padBottom(25f)
            .row();

        table.add(backButton)
            .width(250f)
            .height(52f)
            .row();

        stage.addActor(table);
    }

    private void configureInput() {
        inputMultiplexer = new InputMultiplexer();

        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.log(
                        "GameScreen",
                        "Pause menu will open here"
                    );

                    return true;
                }

                if (keycode == Input.Keys.I) {
                    Gdx.app.log(
                        "GameScreen",
                        "Inventory will open here"
                    );

                    return true;
                }

                return false;
            }
        });

        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(inputMultiplexer);
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
            Gdx.input.getInputProcessor()
                == inputMultiplexer
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
