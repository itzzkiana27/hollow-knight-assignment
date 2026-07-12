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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.AchievementsController;
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.view.actors.AchievementPopupObserver;
import com.hollowknight.view.theme.MenuThemeSkin;

public class AchievementsScreen extends ScreenAdapter {

    private final AchievementsController controller;

    private Stage stage;

    private Skin skin;

    private MenuThemeSkin menuTheme;

    private Texture lockTexture;

    private Texture panelTexture;

    private AchievementPopupObserver popupObserver;

    public AchievementsScreen(AchievementsController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        menuTheme = MenuThemeSkin.fromSettings();
        skin = menuTheme.getSkin();

        lockTexture = createLockTexture();
        panelTexture = createPanelTexture();

        popupObserver = new AchievementPopupObserver(stage, skin, controller::text);

        controller.addObserver(popupObserver);

        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        menuTheme.drawBackground(delta, false);

        stage.act(Math.min(delta, 1f / 30f));

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        controller.removeObserver(popupObserver);

        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        controller.removeObserver(popupObserver);

        if (lockTexture != null) {
            lockTexture.dispose();
        }

        if (panelTexture != null) {
            panelTexture.dispose();
        }

        if (stage != null) {
            stage.dispose();
        }

        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }

    private void createMenu() {
        Table contentTable = new Table();

        contentTable.top();
        contentTable.pad(34f);
        contentTable.defaults().pad(7f);
        contentTable.setBackground(menuTheme.panelDrawable(0.58f));

        Label title = menuTheme.createTitleLabel(controller.text("achievements.title"));
        title.setAlignment(Align.center);

        contentTable.add(title).padBottom(3f).row();

        contentTable
                .add(menuTheme.createMenuHeaderFleur(360f))
                .width(360f)
                .height(50f)
                .padBottom(12f)
                .row();

        Label introduction =
                menuTheme.createBodyLabel(controller.text("achievements.introduction"));

        introduction.setWrap(true);
        introduction.setAlignment(Align.center, Align.top);

        contentTable.add(introduction).width(760f).padBottom(18f).row();

        for (Achievement achievement : controller.getAchievements()) {
            contentTable.add(createAchievementCard(achievement)).width(800f).padBottom(12f).row();
        }

        contentTable
                .add(menuTheme.createMenuFooterFleur(250f))
                .width(250f)
                .height(38f)
                .padTop(6f)
                .padBottom(4f)
                .row();

        TextButton backButton = menuTheme.createMenuButton(controller.text("common.back"));

        backButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        controller.backToMainMenu();
                    }
                });

        contentTable.add(backButton).width(220f).height(52f).padTop(20f).padBottom(25f).row();

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);

        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollBarPositions(false, true);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setVariableSizeKnobs(false);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        rootTable
                .add(scrollPane)
                .width(Math.min(900f, Gdx.graphics.getWidth() * 0.92f))
                .height(Math.min(690f, Gdx.graphics.getHeight() * 0.90f));

        stage.addActor(rootTable);
    }

    private Table createAchievementCard(Achievement achievement) {
        boolean unlocked = achievement.isUnlocked();

        Table card = new Table();
        card.pad(14f);

        Color backgroundColor;

        if (unlocked) {
            backgroundColor =
                    new Color(
                            menuTheme.highlightColor().r * 0.18f,
                            menuTheme.highlightColor().g * 0.18f,
                            menuTheme.highlightColor().b * 0.18f,
                            0.95f);
        } else {
            backgroundColor = new Color(0.06f, 0.065f, 0.075f, 0.92f);
        }

        card.setBackground(
                new TextureRegionDrawable(new TextureRegion(panelTexture)).tint(backgroundColor));

        Actor iconActor;

        if (unlocked) {
            iconActor = menuTheme.createMagicOrbIcon(34f);
        } else {
            Image lockImage = new Image(lockTexture);

            lockImage.setColor(0.50f, 0.50f, 0.55f, 0.88f);

            iconActor = lockImage;
        }

        card.add(iconActor).size(40f).padRight(15f);

        Table textTable = new Table();

        Label titleLabel = menuTheme.createSectionLabel(controller.text(achievement.getTitleKey()));

        Label descriptionLabel =
                menuTheme.createBodyLabel(controller.text(achievement.getDescriptionKey()));

        descriptionLabel.setWrap(true);

        if (!unlocked) {
            titleLabel.setColor(Color.GRAY);
            descriptionLabel.setColor(0.34f, 0.35f, 0.39f, 1f);
        }

        textTable.add(titleLabel).width(540f).left().row();

        textTable.add(descriptionLabel).width(540f).left().padTop(5f).row();

        card.add(textTable).width(560f).growX().left();

        Label statusLabel =
                menuTheme.createBodyLabel(
                        unlocked
                                ? controller.text("achievements.unlocked")
                                : controller.text("achievements.locked"));

        if (unlocked) {
            statusLabel.setColor(menuTheme.highlightColor());
        } else {
            statusLabel.setColor(Color.GRAY);
        }

        statusLabel.setAlignment(Align.right);

        card.add(statusLabel).width(130f).right();

        return card;
    }

    private Texture createPanelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture texture = new Texture(pixmap);

        pixmap.dispose();

        return texture;
    }

    private Texture createLockTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.CLEAR);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);

        pixmap.fillRectangle(6, 14, 20, 14);

        pixmap.fillRectangle(9, 7, 4, 9);

        pixmap.fillRectangle(19, 7, 4, 9);

        pixmap.fillRectangle(9, 5, 14, 4);

        pixmap.setColor(Color.CLEAR);

        pixmap.fillRectangle(14, 19, 4, 6);

        Texture texture = new Texture(pixmap);

        pixmap.dispose();

        return texture;
    }
}
