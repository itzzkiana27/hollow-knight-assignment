package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.SettingsController;
import com.hollowknight.model.GameSettings;
import com.hollowknight.view.theme.MenuThemeSkin;

import java.util.EnumMap;
import java.util.Map;

public class SettingsScreen extends ScreenAdapter {

    private final SettingsController controller;

    private Stage stage;
    private Skin skin;
    private MenuThemeSkin menuTheme;
    private InputMultiplexer inputMultiplexer;

    private CheckBox musicCheckBox;
    private CheckBox soundEffectsCheckBox;

    private Slider musicVolumeSlider;
    private Slider soundEffectsVolumeSlider;
    private Slider brightnessSlider;

    private SelectBox<String> musicStyleSelectBox;
    private SelectBox<String> languageSelectBox;
    private SelectBox<String> themeSelectBox;

    private final Map<ControlAction, TextButton>
        controlButtons = new EnumMap<>(ControlAction.class);

    private ControlAction waitingForControl;

    public SettingsScreen(SettingsController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        menuTheme = MenuThemeSkin.fromThemeId(
            controller.getMenuTheme()
        );
        skin = menuTheme.getSkin();

        createMenu();
        createInputProcessor();
    }

    private void createInputProcessor() {
        inputMultiplexer = new InputMultiplexer();

        inputMultiplexer.addProcessor(
            new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (waitingForControl == null) {
                        return false;
                    }

                    setControlKey(
                        waitingForControl,
                        keycode
                    );

                    controller.saveSettings();
                    waitingForControl = null;
                    refreshControlButtons();

                    return true;
                }
            }
        );

        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void createMenu() {
        Table contentTable = new Table();

        contentTable.top();
        contentTable.pad(32f);
        contentTable.defaults().pad(7f);
        contentTable.setBackground(
            menuTheme.panelDrawable(0.58f)
        );

        Label title = menuTheme.createTitleLabel(
            controller.text("settings.title")
        );
        title.setAlignment(Align.center);

        contentTable.add(title)
            .colspan(2)
            .padBottom(8f)
            .row();

        contentTable.add(menuTheme.createOrnament(230f))
            .colspan(2)
            .width(230f)
            .height(32f)
            .padBottom(18f)
            .row();

        createMusicControls(contentTable);
        createSoundEffectsControls(contentTable);
        createBrightnessControl(contentTable);
        createMusicStyleControl(contentTable);
        createThemeControl(contentTable);
        createLanguageControl(contentTable);
        createControlsSection(contentTable);
        createButtons(contentTable);

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
            .width(Math.min(760f, Gdx.graphics.getWidth() * 0.86f))
            .height(Math.min(680f, Gdx.graphics.getHeight() * 0.88f));

        stage.addActor(rootTable);
    }

    private void createMusicControls(Table table) {
        musicCheckBox = new CheckBox(
            " " + controller.text("settings.musicEnabled"),
            skin
        );

        musicCheckBox.setChecked(
            controller.isMusicEnabled()
        );

        musicCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.setMusicEnabled(
                    musicCheckBox.isChecked()
                );
            }
        });

        table.add(musicCheckBox)
            .left();

        musicVolumeSlider = new Slider(
            0f,
            1f,
            0.05f,
            false,
            skin
        );

        musicVolumeSlider.setValue(
            controller.getMusicVolume()
        );

        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                controller.setMusicVolume(
                    musicVolumeSlider.getValue()
                );
            }
        });

        table.add(musicVolumeSlider)
            .width(260f)
            .row();
    }

    private void createSoundEffectsControls(Table table) {
        soundEffectsCheckBox = new CheckBox(
            " " + controller.text(
                "settings.soundEffectsEnabled"
            ),
            skin
        );

        soundEffectsCheckBox.setChecked(
            controller.isSoundEffectsEnabled()
        );

        soundEffectsCheckBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.setSoundEffectsEnabled(
                        soundEffectsCheckBox.isChecked()
                    );
                }
            }
        );

        table.add(soundEffectsCheckBox)
            .left();

        soundEffectsVolumeSlider = new Slider(
            0f,
            1f,
            0.05f,
            false,
            skin
        );

        soundEffectsVolumeSlider.setValue(
            controller.getSoundEffectsVolume()
        );

        soundEffectsVolumeSlider.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.setSoundEffectsVolume(
                        soundEffectsVolumeSlider.getValue()
                    );
                }
            }
        );

        table.add(soundEffectsVolumeSlider)
            .width(260f)
            .row();
    }

    private void createBrightnessControl(Table table) {
        Label brightnessLabel = menuTheme.createBodyLabel(
            controller.text("settings.brightness")
        );

        table.add(brightnessLabel)
            .left();

        brightnessSlider = new Slider(
            GameSettings.MIN_BRIGHTNESS,
            GameSettings.MAX_BRIGHTNESS,
            0.05f,
            false,
            skin
        );

        brightnessSlider.setValue(
            controller.getBrightness()
        );

        brightnessSlider.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.setBrightness(
                        brightnessSlider.getValue()
                    );
                }
            }
        );

        table.add(brightnessSlider)
            .width(260f)
            .row();
    }

    private void createMusicStyleControl(Table table) {
        Label musicStyleLabel = menuTheme.createBodyLabel(
            controller.text("settings.musicStyle")
        );

        table.add(musicStyleLabel)
            .left();

        musicStyleSelectBox = new SelectBox<>(skin);

        musicStyleSelectBox.setItems(
            controller.text(
                "settings.musicStyle.original"
            ),
            controller.text(
                "settings.musicStyle.ambient"
            ),
            controller.text(
                "settings.musicStyle.boss"
            )
        );

        switch (controller.getMusicStyle()) {
            case "Ambient":
                musicStyleSelectBox.setSelectedIndex(1);
                break;

            case "Boss":
                musicStyleSelectBox.setSelectedIndex(2);
                break;

            default:
                musicStyleSelectBox.setSelectedIndex(0);
                break;
        }

        musicStyleSelectBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    switch (
                        musicStyleSelectBox.getSelectedIndex()
                    ) {
                        case 1:
                            controller.setMusicStyle("Ambient");
                            break;

                        case 2:
                            controller.setMusicStyle("Boss");
                            break;

                        default:
                            controller.setMusicStyle("Original");
                            break;
                    }
                }
            }
        );

        table.add(musicStyleSelectBox)
            .width(260f)
            .row();
    }

    private void createThemeControl(Table table) {
        Label themeLabel = menuTheme.createSectionLabel(
            "Menu Theme"
        );

        table.add(themeLabel)
            .left();

        themeSelectBox = new SelectBox<>(skin);
        themeSelectBox.setItems(
            controller.getMenuThemeDisplayNames()
        );
        themeSelectBox.setSelected(
            controller.getCurrentMenuThemeDisplayName()
        );

        themeSelectBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.setMenuTheme(
                        controller.getMenuThemeIdFromDisplayName(
                            themeSelectBox.getSelected()
                        )
                    );
                }
            }
        );

        table.add(themeSelectBox)
            .width(260f)
            .row();
    }

    private void createLanguageControl(Table table) {
        Label languageLabel = menuTheme.createBodyLabel(
            controller.text("settings.language")
        );

        table.add(languageLabel)
            .left();

        languageSelectBox = new SelectBox<>(skin);

        languageSelectBox.setItems(
            controller.text(
                "settings.language.english"
            ),
            controller.text(
                "settings.language.french"
            )
        );

        if ("fr".equals(controller.getLanguage())) {
            languageSelectBox.setSelectedIndex(1);
        } else {
            languageSelectBox.setSelectedIndex(0);
        }

        languageSelectBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    if (
                        languageSelectBox.getSelectedIndex() == 1
                    ) {
                        controller.setLanguage("fr");
                    } else {
                        controller.setLanguage("en");
                    }
                }
            }
        );

        table.add(languageSelectBox)
            .width(260f)
            .row();
    }

    private void createControlsSection(Table table) {
        Label controlsTitle = menuTheme.createSectionLabel(
            controller.text("settings.controls")
        );

        table.add(controlsTitle)
            .colspan(2)
            .padTop(18f)
            .padBottom(8f)
            .row();

        addControlRow(
            table,
            ControlAction.MOVE_LEFT,
            "settings.moveLeft"
        );

        addControlRow(
            table,
            ControlAction.MOVE_RIGHT,
            "settings.moveRight"
        );

        addControlRow(
            table,
            ControlAction.JUMP,
            "settings.jump"
        );

        addControlRow(
            table,
            ControlAction.DASH,
            "settings.dash"
        );

        addControlRow(
            table,
            ControlAction.ATTACK,
            "settings.attack"
        );

        TextButton resetControlsButton = menuTheme.createMenuButton(
            controller.text("settings.resetControls")
        );

        resetControlsButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    waitingForControl = null;
                    controller.resetControls();
                    refreshControlButtons();
                }
            }
        );

        table.add(resetControlsButton)
            .colspan(2)
            .width(260f)
            .height(46f)
            .padTop(8f)
            .row();
    }

    private void addControlRow(
        Table table,
        ControlAction action,
        String labelKey
    ) {
        Label actionLabel = menuTheme.createBodyLabel(
            controller.text(labelKey)
        );

        TextButton keyButton = menuTheme.createMenuButton(
            controller.keyName(getControlKey(action))
        );

        keyButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    waitingForControl = action;
                    refreshControlButtons();
                }
            }
        );

        controlButtons.put(action, keyButton);

        table.add(actionLabel)
            .left();

        table.add(keyButton)
            .width(260f)
            .height(42f)
            .row();
    }

    private int getControlKey(ControlAction action) {
        switch (action) {
            case MOVE_LEFT:
                return controller.getMoveLeftKey();

            case MOVE_RIGHT:
                return controller.getMoveRightKey();

            case JUMP:
                return controller.getJumpKey();

            case DASH:
                return controller.getDashKey();

            case ATTACK:
                return controller.getAttackKey();

            default:
                throw new IllegalArgumentException(
                    "Unknown control action: " + action
                );
        }
    }

    private void setControlKey(
        ControlAction action,
        int keycode
    ) {
        switch (action) {
            case MOVE_LEFT:
                controller.setMoveLeftKey(keycode);
                break;

            case MOVE_RIGHT:
                controller.setMoveRightKey(keycode);
                break;

            case JUMP:
                controller.setJumpKey(keycode);
                break;

            case DASH:
                controller.setDashKey(keycode);
                break;

            case ATTACK:
                controller.setAttackKey(keycode);
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown control action: " + action
                );
        }
    }

    private void refreshControlButtons() {
        for (
            Map.Entry<ControlAction, TextButton> entry
                : controlButtons.entrySet()
        ) {
            if (entry.getKey() == waitingForControl) {
                entry.getValue().setText(
                    controller.text("settings.pressKey")
                );
            } else {
                entry.getValue().setText(
                    controller.keyName(
                        getControlKey(entry.getKey())
                    )
                );
            }
        }
    }

    private void createButtons(Table table) {
        TextButton resetAudioButton = menuTheme.createMenuButton(
            controller.text("settings.resetAudio")
        );

        resetAudioButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.resetAudio();
                    refreshAudioControls();
                }
            }
        );

        table.add(resetAudioButton)
            .width(220f)
            .height(50f)
            .padTop(20f);

        TextButton resetAllButton = menuTheme.createMenuButton(
            controller.text("settings.resetAll")
        );

        resetAllButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.resetAllSettings();
                }
            }
        );

        table.add(resetAllButton)
            .width(220f)
            .height(50f)
            .padTop(20f)
            .row();

        TextButton backButton = menuTheme.createMenuButton(
            controller.text("common.back")
        );

        backButton.getLabel().setFontScale(1.22f);

        backButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.goBack();
                }
            }
        );

        table.add(backButton)
            .colspan(2)
            .width(220f)
            .height(52f)
            .padTop(12f)
            .padBottom(20f)
            .row();
    }

    private void refreshAudioControls() {
        musicCheckBox.setChecked(
            controller.isMusicEnabled()
        );

        soundEffectsCheckBox.setChecked(
            controller.isSoundEffectsEnabled()
        );

        musicVolumeSlider.setValue(
            controller.getMusicVolume()
        );

        soundEffectsVolumeSlider.setValue(
            controller.getSoundEffectsVolume()
        );

        switch (controller.getMusicStyle()) {
            case "Ambient":
                musicStyleSelectBox.setSelectedIndex(1);
                break;

            case "Boss":
                musicStyleSelectBox.setSelectedIndex(2);
                break;

            default:
                musicStyleSelectBox.setSelectedIndex(0);
                break;
        }
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

        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
