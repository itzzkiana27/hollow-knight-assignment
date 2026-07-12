package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hollowknight.model.boss.FalseKnight;

import java.util.EnumMap;

public final class FalseKnightAnimationManager implements Disposable {

    private static final String BASE_PATH = "sprites/bosses/false_knight/";

    private final EnumMap<FalseKnight.State, Animation<TextureRegion>> animations;

    private final Animation<TextureRegion> shockwaveAnimation;

    private final Animation<TextureRegion> deathFallAnimation;

    private final Array<Texture> textures;

    public FalseKnightAnimationManager() {
        animations = new EnumMap<>(FalseKnight.State.class);

        textures = new Array<>();

        animations.put(
                FalseKnight.State.IDLE, loadAnimation("idle", 5, 0.14f, Animation.PlayMode.LOOP));

        animations.put(
                FalseKnight.State.MACE_SLAM_ANTIC,
                loadAnimation("attack_antic", 6, 0.085f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.MACE_SLAM,
                loadAnimation("attack", 3, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.MACE_SLAM_RECOVER,
                loadAnimation("attack_recover", 5, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.CHARGE_RUN_ANTIC,
                loadAnimation("run_antic", 2, 0.12f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.CHARGE_RUN,
                loadAnimation("run", 5, 0.08f, Animation.PlayMode.LOOP));

        animations.put(
                FalseKnight.State.OFFENSIVE_LEAP,
                loadAnimation("jump", 4, 0.09f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.DEFENSIVE_LEAP,
                loadAnimation("jump", 4, 0.09f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.POWER_JUMP,
                loadAnimation("jump_attack", 4, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.POWER_SLAM,
                loadAnimation(
                        new String[] {
                            BASE_PATH + "jump_attack_004.png",
                            BASE_PATH + "jump_attack_005.png",
                            BASE_PATH + "jump_attack_006.png",
                            BASE_PATH + "jump_attack_007.png"
                        },
                        0.12f,
                        Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.STUNNED,
                loadAnimation("body", 5, 0.14f, Animation.PlayMode.LOOP));

        animations.put(
                FalseKnight.State.STUN_RECOVER,
                loadAnimation("stun_recover", 6, 0.09f, Animation.PlayMode.NORMAL));

        animations.put(
                FalseKnight.State.DEAD,
                loadAnimation("death_land", 11, 0.10f, Animation.PlayMode.NORMAL));

        deathFallAnimation = loadAnimation("death_fall", 3, 0.10f, Animation.PlayMode.NORMAL);

        shockwaveAnimation =
                loadAnimation(
                        "sprites/effects/wave/", "shock_wave", 8, 0.06f, Animation.PlayMode.LOOP);
    }

    public TextureRegion getFrame(FalseKnight falseKnight) {
        Animation<TextureRegion> animation;

        if (falseKnight.getState() == FalseKnight.State.DEAD && !falseKnight.isCorpseGrounded()) {
            animation = deathFallAnimation;
        } else {
            animation = animations.get(falseKnight.getState());
        }

        if (animation == null) {
            animation = animations.get(FalseKnight.State.IDLE);
        }

        float animationTime = falseKnight.getAnimationTime();

        if (falseKnight.isPhaseTwo() && falseKnight.getState() != FalseKnight.State.DEAD) {
            animationTime *= 1.25f;
        }

        return animation.getKeyFrame(Math.max(0f, animationTime));
    }

    public TextureRegion getShockwaveFrame(float shockwaveTime) {
        return shockwaveAnimation.getKeyFrame(Math.max(0f, shockwaveTime));
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }

        textures.clear();
        animations.clear();
    }

    private Animation<TextureRegion> loadAnimation(
            String actionName, int frameCount, float frameDuration, Animation.PlayMode playMode) {
        return loadAnimation(BASE_PATH, actionName, frameCount, frameDuration, playMode);
    }

    private Animation<TextureRegion> loadAnimation(
            String basePath,
            String actionName,
            int frameCount,
            float frameDuration,
            Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>(frameCount);

        for (int index = 0; index < frameCount; index++) {
            String frameNumber = String.format("%03d", index);

            String path = basePath + actionName + "_" + frameNumber + ".png";

            Texture texture = new Texture(Gdx.files.internal(path));

            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            textures.add(texture);

            frames.add(new TextureRegion(texture));
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);

        animation.setPlayMode(playMode);

        return animation;
    }

    private Animation<TextureRegion> loadAnimation(
            String[] framePaths, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>(framePaths.length);

        for (String path : framePaths) {
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
