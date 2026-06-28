package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.SettingsController;

public class SettingsScreen extends ScreenAdapter {

    private final SettingsController controller;

    private Stage stage;
    private Skin skin;

    private CheckBox musicCheckBox;
    private CheckBox soundEffectsCheckBox;

    private Slider musicVolumeSlider;
    private Slider soundEffectsVolumeSlider;
    private Slider brightnessSlider;

    private SelectBox<String> musicStyleSelectBox;
    private SelectBox<String> languageSelectBox;

    public SettingsScreen(SettingsController controller) {
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
        Table table = new Table();

        table.setFillParent(true);
        table.center();
        table.defaults().pad(8f);

        Label title = new Label(
            controller.text("settings.title"),
            skin
        );

        title.setFontScale(1.6f);

        table.add(title)
            .colspan(2)
            .padBottom(25f)
            .row();

        createMusicControls(table);
        createSoundEffectsControls(table);
        createBrightnessControl(table);
        createMusicStyleControl(table);
        createLanguageControl(table);
        createButtons(table);

        stage.addActor(table);
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
            .width(250f)
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
            .width(250f)
            .row();
    }

    private void createBrightnessControl(Table table) {
        Label brightnessLabel = new Label(
            controller.text("settings.brightness"),
            skin
        );

        table.add(brightnessLabel)
            .left();

        brightnessSlider = new Slider(
            0.3f,
            1f,
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
            .width(250f)
            .row();
    }

    private void createMusicStyleControl(Table table) {
        Label musicStyleLabel = new Label(
            controller.text("settings.musicStyle"),
            skin
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
            .width(250f)
            .row();
    }

    private void createLanguageControl(Table table) {
        Label languageLabel = new Label(
            controller.text("settings.language"),
            skin
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
            .width(250f)
            .row();
    }

    private void createButtons(Table table) {
        TextButton resetAudioButton = new TextButton(
            controller.text("settings.resetAudio"),
            skin
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
            .width(210f)
            .height(50f)
            .padTop(20f);

        TextButton resetAllButton = new TextButton(
            controller.text("settings.resetAll"),
            skin
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
            .width(210f)
            .height(50f)
            .padTop(20f)
            .row();

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

        table.add(backButton)
            .colspan(2)
            .width(210f)
            .height(50f)
            .padTop(10f)
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
