package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.enemy.CrystalGuardianAnimationType;

import java.util.EnumMap;

public final class CrystalGuardianAnimationManager {

    private static final String BASE_PATH = "sprites/enemies/crystal_guardian/";

    private final EnumMap<CrystalGuardianAnimationType, Animation<TextureRegion>> animations;

    private final Array<Texture> textures;

    public CrystalGuardianAnimationManager() {
        animations = new EnumMap<>(CrystalGuardianAnimationType.class);

        textures = new Array<>();

        animations.put(
                CrystalGuardianAnimationType.IDLE,
                loadAnimation("idle", 5, 0.12f, Animation.PlayMode.LOOP));

        animations.put(
                CrystalGuardianAnimationType.RUN,
                loadAnimation("run", 6, 0.09f, Animation.PlayMode.LOOP));

        animations.put(
                CrystalGuardianAnimationType.EVADE,
                loadAnimation("evade", 7, 0.08f, Animation.PlayMode.LOOP));

        animations.put(
                CrystalGuardianAnimationType.SHOOT,
                loadAnimation("shoot", 7, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                CrystalGuardianAnimationType.TURN,
                loadAnimation("turn", 3, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                CrystalGuardianAnimationType.DEATH_AIR,
                loadAnimation("death_air", 3, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                CrystalGuardianAnimationType.DEATH_LAND,
                loadAnimation("death_land", 3, 0.10f, Animation.PlayMode.NORMAL));
    }

    public TextureRegion getFrame(CrystalGuardianAnimationType type, float animationTime) {
        Animation<TextureRegion> animation = animations.get(type);

        if (animation == null) {
            throw new IllegalArgumentException("Missing Crystal Guardian animation: " + type);
        }

        return animation.getKeyFrame(Math.max(0f, animationTime));
    }

    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }

        textures.clear();
        animations.clear();
    }

    private Animation<TextureRegion> loadAnimation(
            String action, int frameCount, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>(frameCount);

        for (int index = 0; index < frameCount; index++) {
            String frameNumber = String.format("%03d", index);

            String path = BASE_PATH + action + "_" + frameNumber + ".png";

            Texture texture = new Texture(Gdx.files.internal(path));

            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            textures.add(texture);

            frames.add(new TextureRegion(texture));
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);

        animation.setPlayMode(playMode);

        return animation;
    }
}
