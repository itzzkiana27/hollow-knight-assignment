package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.enemy.WingedSentryAnimationType;

import java.util.EnumMap;

public final class
WingedSentryAnimationManager {

    private static final String BASE_PATH =
        "sprites/enemies/winged_sentry/";

    private final EnumMap<
        WingedSentryAnimationType,
        Animation<TextureRegion>
        > animations;

    private final Array<Texture> textures;

    public WingedSentryAnimationManager() {
        animations = new EnumMap<>(
            WingedSentryAnimationType.class
        );

        textures = new Array<>();

        animations.put(
            WingedSentryAnimationType.IDLE,
            loadAnimation(
                "idle",
                7,
                0.10f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            WingedSentryAnimationType
                .TURN_TO_FLY,
            loadAnimation(
                "turn_to_fly",
                3,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            WingedSentryAnimationType
                .CHARGE_ANTICIPATE,
            loadAnimation(
                "charge_anticipate",
                4,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            WingedSentryAnimationType.CHARGE,
            loadAnimation(
                "charge",
                3,
                0.07f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            WingedSentryAnimationType.DEATH_AIR,
            loadAnimation(
                "death_air",
                2,
                0.10f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            WingedSentryAnimationType
                .DEATH_LAND,
            loadAnimation(
                "death_land",
                4,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );
    }

    private Animation<TextureRegion>
    loadAnimation(
        String action,
        int frameCount,
        float frameDuration,
        Animation.PlayMode playMode
    ) {
        Array<TextureRegion> frames =
            new Array<>(frameCount);

        for (
            int index = 0;
            index < frameCount;
            index++
        ) {
            String frameNumber =
                String.format(
                    "%03d",
                    index
                );

            String path =
                BASE_PATH
                    + action
                    + "_"
                    + frameNumber
                    + ".png";

            Texture texture =
                new Texture(
                    Gdx.files.internal(path)
                );

            texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            textures.add(texture);

            frames.add(
                new TextureRegion(texture)
            );
        }

        Animation<TextureRegion> animation =
            new Animation<>(
                frameDuration,
                frames
            );

        animation.setPlayMode(playMode);

        return animation;
    }

    public TextureRegion getFrame(
        WingedSentryAnimationType type,
        float animationTime
    ) {
        Animation<TextureRegion> animation =
            animations.get(type);

        if (animation == null) {
            throw new IllegalArgumentException(
                "Missing Winged Sentry animation: "
                    + type
            );
        }

        return animation.getKeyFrame(
            Math.max(0f, animationTime)
        );
    }

    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }

        textures.clear();
        animations.clear();
    }
}
