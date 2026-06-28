package com.hollowknight.view.actors;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.model.achievement.AchievementObserver;

import java.util.function.Function;

public class AchievementPopupObserver
    implements AchievementObserver {

    private final Stage stage;
    private final Skin skin;
    private final Function<String, String> translator;

    public AchievementPopupObserver(
        Stage stage,
        Skin skin,
        Function<String, String> translator
    ) {
        this.stage = stage;
        this.skin = skin;
        this.translator = translator;
    }

    @Override
    public void onAchievementUnlocked(
        Achievement achievement
    ) {
        Window popup = new Window(
            translator.apply(
                "achievements.popupTitle"
            ),
            skin
        );

        popup.setMovable(false);
        popup.setResizable(false);
        popup.setKeepWithinStage(false);
        popup.pad(18f);

        Label titleLabel = new Label(
            translator.apply(
                achievement.getTitleKey()
            ),
            skin
        );

        titleLabel.setFontScale(1.2f);

        Label descriptionLabel = new Label(
            translator.apply(
                achievement.getDescriptionKey()
            ),
            skin
        );

        descriptionLabel.setWrap(true);

        popup.add(titleLabel)
            .width(320f)
            .left()
            .row();

        popup.add(descriptionLabel)
            .width(320f)
            .left()
            .padTop(8f)
            .row();

        popup.pack();

        float targetX =
            stage.getViewport().getWorldWidth()
                - popup.getWidth()
                - 20f;

        float targetY =
            stage.getViewport().getWorldHeight()
                - popup.getHeight()
                - 20f;

        float hiddenX =
            stage.getViewport().getWorldWidth()
                + 20f;

        popup.setPosition(hiddenX, targetY);
        popup.getColor().a = 0f;

        stage.addActor(popup);

        popup.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(
                        targetX,
                        targetY,
                        0.35f
                    ),
                    Actions.fadeIn(0.35f)
                ),

                Actions.delay(3f),

                Actions.parallel(
                    Actions.moveTo(
                        hiddenX,
                        targetY,
                        0.35f
                    ),
                    Actions.fadeOut(0.35f)
                ),

                Actions.removeActor()
            )
        );
    }
}
