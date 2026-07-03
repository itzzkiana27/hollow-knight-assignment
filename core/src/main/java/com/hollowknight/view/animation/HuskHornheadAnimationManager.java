package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.enemy.HuskHornheadAnimationType;

import java.util.EnumMap;

/**
 * Loads all Husk Hornhead animations as individual
 * frame files, matching the Knight animation approach.
 */
public final class
HuskHornheadAnimationManager {

    private static final String BASE_PATH =
        "sprites/enemies/husk_hornhead/";

    private final EnumMap<
        HuskHornheadAnimationType,
        Animation<TextureRegion>
        > animations;

    private final Array<Texture> textures;

    public HuskHornheadAnimationManager() {
        animations = new EnumMap<>(
            HuskHornheadAnimationType.class
        );

        textures = new Array<>();

        animations.put(
            HuskHornheadAnimationType.IDLE,
            loadAnimation(
                "idle",
                6,
                0.12f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            HuskHornheadAnimationType.WALK,
            loadAnimation(
                "walk",
                7,
                0.09f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            HuskHornheadAnimationType.TURN,
            loadAnimation(
                "turn",
                2,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            HuskHornheadAnimationType
                .ATTACK_ANTICIPATE,
            loadAnimation(
                "attack_anticipate",
                5,
                0.09f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            HuskHornheadAnimationType
                .ATTACK_LUNGE,
            loadAnimation(
                "attack_lunge",
                12,
                0.06f,
                Animation.PlayMode.LOOP
            )
        );

        animations.put(
            HuskHornheadAnimationType
                .ATTACK_COOLDOWN,
            loadAnimation(
                "attack_cooldown",
                1,
                0.65f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            HuskHornheadAnimationType
                .DEATH_AIR,
            loadAnimation(
                "death_air",
                1,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );

        animations.put(
            HuskHornheadAnimationType
                .DEATH_LAND,
            loadAnimation(
                "death_land",
                8,
                0.10f,
                Animation.PlayMode.NORMAL
            )
        );
    }

    private Animation<TextureRegion>
    loadAnimation(
        String actionName,
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
                    + actionName
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
        HuskHornheadAnimationType type,
        float animationTime
    ) {
        Animation<TextureRegion> animation =
            animations.get(type);

        if (animation == null) {
            throw new IllegalArgumentException(
                "Missing Husk Hornhead animation: "
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
