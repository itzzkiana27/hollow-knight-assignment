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

    private static final float SETTINGS_TITLE_SCALE = 1.80f;
    private static final float SETTINGS_SECTION_SCALE = 1.00f;
    private static final float SETTINGS_LABEL_SCALE = 0.82f;
    private static final float SETTINGS_CHECKBOX_SCALE = 0.82f;
    private static final float SETTINGS_BUTTON_SCALE = 0.88f;

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
        contentTable.pad(24f);
        contentTable.defaults().pad(5f);
        contentTable.columnDefaults(0)
            .width(360f)
            .left()
            .padRight(18f);
        contentTable.columnDefaults(1)
            .width(320f)
            .right();
        contentTable.setBackground(
            menuTheme.panelDrawable(0.58f)
        );

        Label title = menuTheme.createTitleLabel(
            controller.text("settings.title")
        );
        title.setAlignment(Align.center);
        title.setFontScale(SETTINGS_TITLE_SCALE);

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
            .width(Math.min(860f, Gdx.graphics.getWidth() * 0.94f))
            .height(Math.min(700f, Gdx.graphics.getHeight() * 0.92f));

        stage.addActor(rootTable);
    }

    private void createMusicControls(Table table) {
        musicCheckBox = new CheckBox(
            " " + controller.text("settings.musicEnabled"),
            skin
        );

        musicCheckBox.getLabel().setFontScale(SETTINGS_CHECKBOX_SCALE);

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
            .width(300f)
            .row();
    }

    private void createSoundEffectsControls(Table table) {
        soundEffectsCheckBox = new CheckBox(
            " " + controller.text(
                "settings.soundEffectsEnabled"
            ),
            skin
        );

        soundEffectsCheckBox.getLabel().setFontScale(SETTINGS_CHECKBOX_SCALE);

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
            .width(300f)
            .row();
    }

    private void createBrightnessControl(Table table) {
        Label brightnessLabel = createSettingsLabel(
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
            .width(300f)
            .row();
    }

    private void createMusicStyleControl(Table table) {
        Label musicStyleLabel = createSettingsLabel(
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
            .width(300f)
            .row();
    }

    private void createThemeControl(Table table) {
        Label themeLabel = createSettingsSectionLabel(
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
            .width(300f)
            .row();
    }

    private void createLanguageControl(Table table) {
        Label languageLabel = createSettingsLabel(
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
            .width(300f)
            .row();
    }

    private void createControlsSection(Table table) {
        Label controlsTitle = createSettingsSectionLabel(
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
            ControlAction.LOOK_UP,
            "settings.lookUp"
        );

        addControlRow(
            table,
            ControlAction.LOOK_DOWN,
            "settings.lookDown"
        );

        addControlRow(
            table,
            ControlAction.ATTACK,
            "settings.attack"
        );

        addControlRow(
            table,
            ControlAction.ALTERNATE_ATTACK,
            "settings.alternateAttack"
        );

        addControlRow(
            table,
            ControlAction.FOCUS,
            "settings.focus"
        );

        addControlRow(
            table,
            ControlAction.FIREBALL,
            "settings.fireball"
        );

        addControlRow(
            table,
            ControlAction.SCREAM,
            "settings.scream"
        );

        addControlRow(
            table,
            ControlAction.INTERACT,
            "settings.interact"
        );

        addControlRow(
            table,
            ControlAction.DIALOGUE_ADVANCE,
            "settings.dialogueAdvance"
        );

        addControlRow(
            table,
            ControlAction.INVENTORY,
            "settings.inventory"
        );

        addControlRow(
            table,
            ControlAction.PAUSE,
            "settings.pause"
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

        resetControlsButton.getLabel().setFontScale(SETTINGS_BUTTON_SCALE);

        table.add(resetControlsButton)
            .colspan(2)
            .width(300f)
            .height(42f)
            .center()
            .padTop(8f)
            .row();
    }

    private void addControlRow(
        Table table,
        ControlAction action,
        String labelKey
    ) {
        Label actionLabel = createSettingsLabel(
            controller.text(labelKey)
        );

        TextButton keyButton = menuTheme.createMenuButton(
            controller.keyName(getControlKey(action))
        );

        keyButton.getLabel().setFontScale(SETTINGS_BUTTON_SCALE);

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
            .width(300f)
            .height(38f)
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

            case LOOK_UP:
                return controller.getUpKey();

            case LOOK_DOWN:
                return controller.getDownKey();

            case ATTACK:
                return controller.getAttackKey();

            case ALTERNATE_ATTACK:
                return controller.getAlternateAttackKey();

            case FOCUS:
                return controller.getFocusKey();

            case FIREBALL:
                return controller.getFireballKey();

            case SCREAM:
                return controller.getScreamKey();

            case INTERACT:
                return controller.getInteractKey();

            case DIALOGUE_ADVANCE:
                return controller.getDialogueAdvanceKey();

            case INVENTORY:
                return controller.getInventoryKey();

            case PAUSE:
                return controller.getPauseKey();

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

            case LOOK_UP:
                controller.setUpKey(keycode);
                break;

            case LOOK_DOWN:
                controller.setDownKey(keycode);
                break;

            case ATTACK:
                controller.setAttackKey(keycode);
                break;

            case ALTERNATE_ATTACK:
                controller.setAlternateAttackKey(keycode);
                break;

            case FOCUS:
                controller.setFocusKey(keycode);
                break;

            case FIREBALL:
                controller.setFireballKey(keycode);
                break;

            case SCREAM:
                controller.setScreamKey(keycode);
                break;

            case INTERACT:
                controller.setInteractKey(keycode);
                break;

            case DIALOGUE_ADVANCE:
                controller.setDialogueAdvanceKey(keycode);
                break;

            case INVENTORY:
                controller.setInventoryKey(keycode);
                break;

            case PAUSE:
                controller.setPauseKey(keycode);
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

        resetAudioButton.getLabel().setFontScale(SETTINGS_BUTTON_SCALE);

        table.add(resetAudioButton)
            .width(240f)
            .height(44f)
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

        resetAllButton.getLabel().setFontScale(SETTINGS_BUTTON_SCALE);

        table.add(resetAllButton)
            .width(240f)
            .height(44f)
            .padTop(20f)
            .row();

        TextButton backButton = menuTheme.createMenuButton(
            controller.text("common.back")
        );

        backButton.getLabel().setFontScale(0.94f);

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
            .width(240f)
            .height(46f)
            .center()
            .padTop(12f)
            .padBottom(20f)
            .row();
    }

    private Label createSettingsLabel(String text) {
        Label label = menuTheme.createBodyLabel(text);
        label.setFontScale(SETTINGS_LABEL_SCALE);
        return label;
    }

    private Label createSettingsSectionLabel(String text) {
        Label label = menuTheme.createSectionLabel(text);
        label.setFontScale(SETTINGS_SECTION_SCALE);
        return label;
    }

    private void refreshAudioControls() {
        musicCheckBox.getLabel().setFontScale(SETTINGS_CHECKBOX_SCALE);

        musicCheckBox.setChecked(
            controller.isMusicEnabled()
        );

        soundEffectsCheckBox.getLabel().setFontScale(SETTINGS_CHECKBOX_SCALE);

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
