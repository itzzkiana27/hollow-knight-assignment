package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.StartGameController;
import com.hollowknight.view.theme.MenuThemeSkin;

public class StartGameScreen extends ScreenAdapter {

    private final StartGameController controller;

    private Stage stage;
    private Skin skin;
    private MenuThemeSkin menuTheme;

    public StartGameScreen(StartGameController controller) {
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
        table.defaults().pad(6f);

        Label title = menuTheme.createTitleLabel(
            controller.text("start.title")
        );
        title.setAlignment(Align.center);

        table.add(title)
            .width(520f)
            .padBottom(2f)
            .row();

        table.add(menuTheme.createOrnament(260f))
            .width(260f)
            .height(30f)
            .padBottom(16f)
            .row();

        TextButton newGame = menuTheme.createMenuButton(
            controller.text("start.newGame")
        );
        newGame.getLabel().setFontScale(1.25f);
        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.startNewGame();
            }
        });

        table.add(newGame)
            .width(360f)
            .height(44f)
            .padBottom(14f)
            .row();

        for (int slot = 1; slot <= 4; slot++) {
            final int selectedSlot = slot;

            table.add(createSlotCard(slot))
                .width(720f)
                .height(96f)
                .padBottom(8f)
                .row();
        }

        addButton(
            table,
            controller.text("common.back"),
            controller::backToMainMenu
        );

        stage.addActor(table);
    }

    private Table createSlotCard(int slotNumber) {
        boolean saved =
            controller.hasSaveSlot(slotNumber);

        Table card = new Table();
        card.pad(8f);
        card.setBackground(
            menuTheme.panelDrawable(0.56f)
        );

        Image preview = menuTheme.createSlotPreview(
            slotNumber,
            saved
        );

        card.add(preview)
            .width(300f)
            .height(48f)
            .padRight(18f);

        Table textTable = new Table();

        Label slotTitle = menuTheme.createSectionLabel(
            controller.format(
                "start.slotTitle",
                slotNumber
            )
        );
        slotTitle.setAlignment(Align.left);

        Label slotStatus = menuTheme.createBodyLabel(
            saved
                ? controller.getSaveSlotLabel(slotNumber)
                : controller.text("start.emptySlot")
        );
        slotStatus.setColor(
            saved
                ? menuTheme.bodyColor()
                : new com.badlogic.gdx.graphics.Color(
                    0.48f,
                    0.52f,
                    0.58f,
                    1f
                )
        );

        textTable.add(slotTitle)
            .width(240f)
            .left()
            .row();

        textTable.add(slotStatus)
            .width(240f)
            .left()
            .padTop(4f)
            .row();

        card.add(textTable)
            .width(260f)
            .left();

        TextButton openButton = menuTheme.createMenuButton(
            controller.text(
                saved
                    ? "start.load"
                    : "start.begin"
            )
        );
        openButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.selectSaveSlot(
                    slotNumber
                );
            }
        });

        card.add(openButton)
            .width(110f)
            .height(42f)
            .right();

        return card;
    }

    private void addButton(
        Table table,
        String text,
        Runnable action
    ) {
        TextButton button = menuTheme.createMenuButton(text);

        button.getLabel().setFontScale(1.18f);

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
            .width(260f)
            .height(48f)
            .padTop(8f)
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
            true
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
        if (stage != null) {
            stage.dispose();
        }

        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
