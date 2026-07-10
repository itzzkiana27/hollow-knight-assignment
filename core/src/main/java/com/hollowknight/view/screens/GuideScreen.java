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

public class GuideScreen extends ScreenAdapter {

    private final GuideController controller;

    private Stage stage;
    private Skin skin;

    public GuideScreen(GuideController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(
            Gdx.files.internal("ui/uiskin.json")
        );

        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    private void createMenu() {
        Table contentTable = new Table();

        contentTable.top();
        contentTable.pad(35f);
        contentTable.defaults().pad(7f);

        Label title = new Label(
            controller.text("guide.title"),
            skin
        );

        title.setFontScale(1.7f);

        contentTable.add(title)
            .colspan(2)
            .padBottom(25f)
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

        rootTable.add(scrollPane)
            .grow()
            .pad(10f);

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

        addDescription(
            table,
            controller.text("guide.healthTitle"),
            controller.text("guide.healthDescription")
        );

        addDescription(
            table,
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

        addDescription(
            table,
            controller.text("guide.vengefulSpiritTitle"),
            controller.text("guide.vengefulSpiritDescription")
        );

        addDescription(
            table,
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
        Label sectionTitle = new Label(text, skin);

        sectionTitle.setFontScale(1.3f);

        table.add(sectionTitle)
            .colspan(2)
            .padTop(22f)
            .padBottom(10f)
            .row();
    }

    private void addControlRow(
        Table table,
        String action,
        String key
    ) {
        Label actionLabel = new Label(action, skin);
        Label keyLabel = new Label(key, skin);

        table.add(actionLabel)
            .width(320f)
            .left();

        table.add(keyLabel)
            .width(260f)
            .left()
            .row();
    }

    private void addDescription(
        Table table,
        String heading,
        String description
    ) {
        Label headingLabel = new Label(
            heading,
            skin
        );

        headingLabel.setAlignment(Align.left);

        Label descriptionLabel = new Label(
            description,
            skin
        );

        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(
            Align.left,
            Align.top
        );

        table.add(headingLabel)
            .width(220f)
            .left()
            .top();

        table.add(descriptionLabel)
            .width(540f)
            .left()
            .top()
            .row();
    }

    private void createBackButton(Table table) {
        TextButton backButton = new TextButton(
            controller.text("common.back"),
            skin
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
            .colspan(2)
            .width(220f)
            .height(52f)
            .padTop(30f)
            .padBottom(25f)
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
        if (Gdx.input.getInputProcessor() == stage) {
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
