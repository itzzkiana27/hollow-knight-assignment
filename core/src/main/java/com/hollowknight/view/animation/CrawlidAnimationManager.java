package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.enemy.CrawlidAnimationType;

import java.util.EnumMap;

public final class CrawlidAnimationManager {

    private static final String BASE_PATH = "sprites/enemies/crawlid/";

    private final EnumMap<CrawlidAnimationType, Animation<TextureRegion>> animations;

    private final Array<Texture> textures;

    public CrawlidAnimationManager() {
        animations = new EnumMap<>(CrawlidAnimationType.class);

        textures = new Array<>();

        animations.put(
                CrawlidAnimationType.WALK,
                loadAnimation("walk", 4, 0.12f, Animation.PlayMode.LOOP));

        animations.put(
                CrawlidAnimationType.TURN,
                loadAnimation("turn", 2, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                CrawlidAnimationType.DEATH_AIR,
                loadAnimation("death_air", 3, 0.10f, Animation.PlayMode.NORMAL));

        animations.put(
                CrawlidAnimationType.DEATH_LAND,
                loadAnimation("death_land", 2, 0.12f, Animation.PlayMode.NORMAL));
    }

    public TextureRegion getFrame(CrawlidAnimationType type, float animationTime) {
        Animation<TextureRegion> animation = animations.get(type);

        if (animation == null) {
            throw new IllegalArgumentException("Missing Crawlid animation: " + type);
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
            String actionName, int frameCount, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>(frameCount);

        for (int index = 0; index < frameCount; index++) {
            String frameNumber = String.format("%03d", index);

            String path = BASE_PATH + actionName + "_" + frameNumber + ".png";

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
