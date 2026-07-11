package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.GuideController;
import com.hollowknight.view.theme.MenuThemeSkin;

public class GuideScreen extends ScreenAdapter {

    private final GuideController controller;

    private Stage stage;
    private Skin skin;
    private MenuThemeSkin menuTheme;

    public GuideScreen(GuideController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        menuTheme = MenuThemeSkin.fromSettings();
        skin = menuTheme.getSkin();

        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    private void createMenu() {
        Table contentTable = new Table();

        contentTable.top();
        contentTable.pad(30f);
        contentTable.defaults()
            .pad(7f)
            .padLeft(12f)
            .padRight(12f);
        contentTable.setBackground(
            menuTheme.panelDrawable(0.60f)
        );

        Label title = menuTheme.createTitleLabel(
            controller.text("guide.title")
        );
        title.setAlignment(Align.center);

        contentTable.add(title)
            .colspan(3)
            .padBottom(5f)
            .row();

        contentTable.add(menuTheme.createOrnament(260f))
            .colspan(3)
            .width(260f)
            .height(30f)
            .padBottom(18f)
            .row();

        createControlsSection(contentTable);
        createAbilitiesSection(contentTable);
        createCheatCodesSection(contentTable);
        createBackButton(contentTable);

        ScrollPane scrollPane = new ScrollPane(
            contentTable,
            skin
        );

        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        rootTable.add(scrollPane)
            .width(Math.min(920f, Gdx.graphics.getWidth() * 0.92f))
            .height(Math.min(700f, Gdx.graphics.getHeight() * 0.92f));

        stage.addActor(rootTable);
    }

    private void createControlsSection(Table table) {
        addSectionTitle(
            table,
            controller.text("guide.controlsTitle")
        );

        addControlRow(
            table,
            controller.text("guide.moveLeft"),
            controller.keyName(
                controller.getMoveLeftKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.moveRight"),
            controller.keyName(
                controller.getMoveRightKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.lookUp"),
            controller.keyName(
                controller.getUpKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.lookDown"),
            controller.keyName(
                controller.getDownKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.jump"),
            controller.keyName(
                controller.getJumpKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.dash"),
            controller.keyName(
                controller.getDashKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.attack"),
            controller.keyName(
                controller.getAttackKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.focus"),
            controller.keyName(
                controller.getFocusKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.fireball"),
            controller.keyName(
                controller.getFireballKey()
            )
        );

        addControlRow(
            table,
            controller.text("guide.scream"),
            controller.keyName(
                controller.getScreamKey()
            )
        );
    }

    private void createAbilitiesSection(Table table) {
        addSectionTitle(
            table,
            controller.text("guide.abilitiesTitle")
        );

        addIconDescription(
            table,
            "health",
            controller.text("guide.healthTitle"),
            controller.text("guide.healthDescription")
        );

        addIconDescription(
            table,
            "soul",
            controller.text("guide.soulTitle"),
            controller.text("guide.soulDescription")
        );

        addDescription(
            table,
            controller.text("guide.nailTitle"),
            controller.text("guide.nailDescription")
        );

        addDescription(
            table,
            controller.text("guide.dashAbilityTitle"),
            controller.text("guide.dashAbilityDescription")
        );

        addDescription(
            table,
            controller.text("guide.focusAbilityTitle"),
            controller.text("guide.focusAbilityDescription")
        );

        addIconDescription(
            table,
            "vengeful",
            controller.text("guide.vengefulSpiritTitle"),
            controller.text("guide.vengefulSpiritDescription")
        );

        addIconDescription(
            table,
            "howling",
            controller.text("guide.howlingWraithsTitle"),
            controller.text("guide.howlingWraithsDescription")
        );

        addDescription(
            table,
            controller.text("guide.charmsTitle"),
            controller.text("guide.charmsDescription")
        );
    }

    private void createCheatCodesSection(Table table) {
        addSectionTitle(
            table,
            controller.text("guide.cheatsTitle")
        );

        addDescription(
            table,
            "Left Ctrl + B",
            controller.text("guide.cheatBossTeleport")
        );

        addDescription(
            table,
            "Left Ctrl + F",
            controller.text("guide.cheatNoclip")
        );

        addDescription(
            table,
            "Left Ctrl + H",
            controller.text("guide.cheatEmergencyHeal")
        );

        addDescription(
            table,
            "Left Ctrl + S",
            controller.text("guide.cheatSoul")
        );

        addDescription(
            table,
            "Left Ctrl + G",
            controller.text("guide.cheatGodMode")
        );

        addDescription(
            table,
            "Left Ctrl + K",
            controller.text("guide.cheatInstaKill")
        );
    }

    private void addSectionTitle(
        Table table,
        String text
    ) {
        Label sectionTitle = menuTheme.createSectionLabel(text);

        table.add(sectionTitle)
            .colspan(3)
            .padTop(22f)
            .padBottom(10f)
            .row();
    }

    private void addControlRow(
        Table table,
        String action,
        String key
    ) {
        Label actionLabel = menuTheme.createBodyLabel(action);
        Label keyLabel = menuTheme.createSectionLabel(key);

        table.add(menuTheme.createMagicOrbIcon(18f))
            .size(18f)
            .padLeft(8f)
            .padRight(10f);

        table.add(actionLabel)
            .width(300f)
            .left();

        table.add(keyLabel)
            .width(170f)
            .left()
            .row();
    }

    private void addIconDescription(
        Table table,
        String iconType,
        String heading,
        String description
    ) {
        Actor icon;

        if ("health".equals(iconType)) {
            icon = menuTheme.createHealthIcon(34f);
        } else if ("soul".equals(iconType)) {
            icon = menuTheme.createSoulOrbIcon(34f);
        } else {
            icon = menuTheme.createSpellIcon(iconType);
        }

        table.add(icon)
            .size(38f)
            .padLeft(8f)
            .padRight(10f)
            .top();

        addDescriptionCells(
            table,
            heading,
            description
        );
    }

    private void addDescription(
        Table table,
        String heading,
        String description
    ) {
        table.add(menuTheme.createMagicOrbIcon(18f))
            .size(18f)
            .padLeft(8f)
            .padRight(10f)
            .top();

        addDescriptionCells(
            table,
            heading,
            description
        );
    }

    private void addDescriptionCells(
        Table table,
        String heading,
        String description
    ) {
        Label headingLabel = menuTheme.createSectionLabel(
            heading
        );

        headingLabel.setAlignment(Align.left);

        Label descriptionLabel = menuTheme.createBodyLabel(
            description
        );

        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(
            Align.left,
            Align.top
        );

        table.add(headingLabel)
            .width(200f)
            .left()
            .top();

        table.add(descriptionLabel)
            .width(380f)
            .left()
            .top()
            .row();
    }

    private void createBackButton(Table table) {
        TextButton backButton = menuTheme.createMenuButton(
            controller.text("common.back")
        );

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.backToMainMenu();
            }
        });

        table.add(backButton)
            .colspan(3)
            .width(240f)
            .height(52f)
            .padTop(30f)
            .padBottom(25f)
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
