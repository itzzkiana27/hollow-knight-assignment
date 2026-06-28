package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.AchievementsController;
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.view.actors.AchievementPopupObserver;

public class AchievementsScreen extends ScreenAdapter {

    private final AchievementsController controller;

    private Stage stage;
    private Skin skin;

    private Texture lockTexture;
    private Texture panelTexture;

    private AchievementPopupObserver popupObserver;

    public AchievementsScreen(
        AchievementsController controller
    ) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(
            Gdx.files.internal("ui/uiskin.json")
        );

        lockTexture = createLockTexture();
        panelTexture = createPanelTexture();

        popupObserver =
            new AchievementPopupObserver(
                stage,
                skin,
                controller::text
            );

        controller.addObserver(popupObserver);

        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    private void createMenu() {
        Table contentTable = new Table();

        contentTable.top();
        contentTable.pad(35f);
        contentTable.defaults().pad(7f);

        Label title = new Label(
            controller.text("achievements.title"),
            skin
        );

        title.setFontScale(1.7f);

        contentTable.add(title)
            .padBottom(15f)
            .row();

        Label introduction = new Label(
            controller.text(
                "achievements.introduction"
            ),
            skin
        );

        introduction.setWrap(true);
        introduction.setAlignment(
            Align.center,
            Align.top
        );

        contentTable.add(introduction)
            .width(780f)
            .padBottom(25f)
            .row();

        for (
            Achievement achievement
            : controller.getAchievements()
        ) {
            contentTable.add(
                    createAchievementCard(achievement)
                )
                .width(820f)
                .padBottom(12f)
                .row();
        }

        TextButton backButton = new TextButton(
            controller.text("common.back"),
            skin
        );

        backButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.backToMainMenu();
                }
            }
        );

        contentTable.add(backButton)
            .width(220f)
            .height(52f)
            .padTop(20f)
            .padBottom(25f)
            .row();

        ScrollPane scrollPane = new ScrollPane(
            contentTable,
            skin
        );

        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        rootTable.add(scrollPane)
            .grow()
            .pad(10f);

        stage.addActor(rootTable);
    }

    private Table createAchievementCard(
        Achievement achievement
    ) {
        boolean unlocked =
            achievement.isUnlocked();

        Table card = new Table();
        card.pad(15f);

        Color backgroundColor;

        if (unlocked) {
            backgroundColor = new Color(
                0.08f,
                0.18f,
                0.22f,
                0.95f
            );
        } else {
            backgroundColor = new Color(
                0.12f,
                0.12f,
                0.12f,
                0.95f
            );
        }

        card.setBackground(
            new TextureRegionDrawable(
                new TextureRegion(panelTexture)
            ).tint(backgroundColor)
        );

        Actor iconActor;

        if (unlocked) {
            iconActor = new Actor();
        } else {
            Image lockImage = new Image(lockTexture);

            lockImage.setColor(
                0.65f,
                0.65f,
                0.65f,
                1f
            );

            iconActor = lockImage;
        }

        card.add(iconActor)
            .size(36f)
            .padRight(15f);

        Table textTable = new Table();

        Label titleLabel = new Label(
            controller.text(
                achievement.getTitleKey()
            ),
            skin
        );

        titleLabel.setFontScale(1.2f);

        Label descriptionLabel = new Label(
            controller.text(
                achievement.getDescriptionKey()
            ),
            skin
        );

        descriptionLabel.setWrap(true);

        if (!unlocked) {
            titleLabel.setColor(Color.GRAY);
            descriptionLabel.setColor(
                Color.DARK_GRAY
            );
        }

        textTable.add(titleLabel)
            .width(560f)
            .left()
            .row();

        textTable.add(descriptionLabel)
            .width(560f)
            .left()
            .padTop(5f)
            .row();

        card.add(textTable)
            .width(580f)
            .growX()
            .left();

        Label statusLabel = new Label(
            unlocked
                ? controller.text(
                "achievements.unlocked"
            )
                : controller.text(
                "achievements.locked"
            ),
            skin
        );

        if (unlocked) {
            statusLabel.setColor(
                0.35f,
                0.9f,
                0.55f,
                1f
            );
        } else {
            statusLabel.setColor(Color.GRAY);
        }

        statusLabel.setAlignment(Align.right);

        card.add(statusLabel)
            .width(130f)
            .right();

        return card;
    }

    private Texture createPanelTexture() {
        Pixmap pixmap = new Pixmap(
            1,
            1,
            Pixmap.Format.RGBA8888
        );

        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture texture = new Texture(pixmap);

        pixmap.dispose();

        return texture;
    }

    private Texture createLockTexture() {
        Pixmap pixmap = new Pixmap(
            32,
            32,
            Pixmap.Format.RGBA8888
        );

        pixmap.setColor(Color.CLEAR);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);

        // Lock body
        pixmap.fillRectangle(
            6,
            14,
            20,
            14
        );

        // Lock shackle
        pixmap.fillRectangle(
            9,
            7,
            4,
            9
        );

        pixmap.fillRectangle(
            19,
            7,
            4,
            9
        );

        pixmap.fillRectangle(
            9,
            5,
            14,
            4
        );

        // Keyhole
        pixmap.setColor(Color.CLEAR);

        pixmap.fillRectangle(
            14,
            19,
            4,
            6
        );

        Texture texture = new Texture(pixmap);

        pixmap.dispose();

        return texture;
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
        controller.removeObserver(
            popupObserver
        );

        if (
            Gdx.input.getInputProcessor() == stage
        ) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        controller.removeObserver(
            popupObserver
        );

        if (lockTexture != null) {
            lockTexture.dispose();
        }

        if (panelTexture != null) {
            panelTexture.dispose();
        }

        if (stage != null) {
            stage.dispose();
        }

        if (skin != null) {
            skin.dispose();
        }
    }
}
