package com.hollowknight.view.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.hollowknight.model.player.PlayerAnimationType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class KnightAnimationManager implements Disposable {

    private static final String BASE_PATH = "sprites/knight/";

    private final Map<PlayerAnimationType, Animation<TextureRegion>> animations;

    private final EnumSet<PlayerAnimationType> loopingAnimations;

    private final Array<Texture> loadedTextures;

    public KnightAnimationManager() {
        animations = new EnumMap<>(PlayerAnimationType.class);

        loopingAnimations = EnumSet.noneOf(PlayerAnimationType.class);

        loadedTextures = new Array<>();

        loadAllAnimations();
    }

    public TextureRegion getFrame(PlayerAnimationType type, float stateTime) {
        Animation<TextureRegion> animation = animations.get(type);

        if (animation == null) {
            throw new GdxRuntimeException("No Knight animation registered for: " + type);
        }

        return animation.getKeyFrame(stateTime);
    }

    public boolean isFinished(PlayerAnimationType type, float stateTime) {
        Animation<TextureRegion> animation = animations.get(type);

        if (animation == null) {
            throw new GdxRuntimeException("No Knight animation registered for: " + type);
        }

        if (loopingAnimations.contains(type)) {
            return false;
        }

        return animation.isAnimationFinished(stateTime);
    }

    public float getAnimationDuration(PlayerAnimationType type) {
        Animation<TextureRegion> animation = animations.get(type);

        if (animation == null) {
            throw new GdxRuntimeException("No Knight animation registered for: " + type);
        }

        return animation.getAnimationDuration();
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }

        loadedTextures.clear();
        animations.clear();
        loopingAnimations.clear();
    }

    public boolean isLooping(PlayerAnimationType type) {
        return loopingAnimations.contains(type);
    }

    private void loadAllAnimations() {

        registerLoop(PlayerAnimationType.IDLE, "idle", 9, 0.10f, "idle");

        registerLoop(PlayerAnimationType.WALK, "walk", 3, 0.11f, "walk");

        registerLoop(PlayerAnimationType.RUN, "run", 13, 0.065f, "run");

        registerOnce(PlayerAnimationType.RUN_TO_IDLE, "run_to_idle", 6, 0.07f, "run_to_idle");

        registerOnce(PlayerAnimationType.AIRBORNE, "airborne", 12, 0.065f, "airborne");

        registerLoop(PlayerAnimationType.FALL, "fall", 6, 0.08f, "fall");

        registerOnce(PlayerAnimationType.LANDING, "landing", 4, 0.065f, "landing");

        registerOnce(PlayerAnimationType.DOUBLE_JUMP, "double_jump", 8, 0.06f, "double_jump");

        registerLoop(PlayerAnimationType.WALL_SLIDE, "wall_slide", 4, 0.10f, "wall_slide");

        registerOnce(PlayerAnimationType.WALL_JUMP, "wall_jump", 9, 0.06f, "wall_jump");

        registerOnce(PlayerAnimationType.DASH, "dash", 12, 0.045f, "dash");

        registerOnce(PlayerAnimationType.SLASH, "slash", 5, 0.055f, "slash");

        registerOnce(PlayerAnimationType.SLASH_ALT, "slash_alt", 5, 0.055f, "slash_alt");

        registerOnce(PlayerAnimationType.UP_SLASH, "up_slash", 5, 0.055f, "up_slash");

        registerOnce(PlayerAnimationType.DOWN_SLASH, "down_slash", 5, 0.055f, "down_slash");

        registerOnce(PlayerAnimationType.FOCUS_START, "focus_start", 3, 0.08f, "focus_start");

        registerLoop(PlayerAnimationType.FOCUS, "focus", 7, 0.10f, "focus", "focus_loop");

        registerOnce(PlayerAnimationType.FOCUS_END, "focus_end", 3, 0.08f, "focus_end");

        registerOnce(PlayerAnimationType.FOCUS_GET, "focus_get", 6, 0.08f, "focus_get");

        registerOnce(PlayerAnimationType.FIREBALL_CAST, "fireball_cast", 9, 0.06f, "fireball_cast");

        registerOnce(PlayerAnimationType.SCREAM, "scream", 7, 0.07f, "scream");

        registerOnce(PlayerAnimationType.IDLE_HURT, "idle_hurt", 12, 0.06f, "idle_hurt", "hurt");

        registerOnce(PlayerAnimationType.DEATH, "death", 18, 0.08f, "death");

        registerOnce(PlayerAnimationType.LOOK_UP, "look_up", 6, 0.10f, "look_up");

        registerOnce(PlayerAnimationType.LOOK_DOWN, "look_down", 6, 0.10f, "look_down");
    }

    private void registerLoop(
            PlayerAnimationType type,
            String folder,
            int frameCount,
            float frameDuration,
            String... possiblePrefixes) {
        Animation<TextureRegion> animation =
                createAnimation(
                        folder,
                        frameCount,
                        frameDuration,
                        Animation.PlayMode.LOOP,
                        possiblePrefixes);

        animations.put(type, animation);
        loopingAnimations.add(type);
    }

    private void registerOnce(
            PlayerAnimationType type,
            String folder,
            int frameCount,
            float frameDuration,
            String... possiblePrefixes) {
        Animation<TextureRegion> animation =
                createAnimation(
                        folder,
                        frameCount,
                        frameDuration,
                        Animation.PlayMode.NORMAL,
                        possiblePrefixes);

        animations.put(type, animation);
    }

    private Animation<TextureRegion> createAnimation(
            String folder,
            int frameCount,
            float frameDuration,
            Animation.PlayMode playMode,
            String... possiblePrefixes) {
        Array<TextureRegion> frames = loadFrames(folder, frameCount, possiblePrefixes);

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);

        animation.setPlayMode(playMode);

        return animation;
    }

    private Array<TextureRegion> loadFrames(
            String folder, int frameCount, String... possiblePrefixes) {
        Array<TextureRegion> frames = new Array<>();

        for (int index = 0; index < frameCount; index++) {

            FileHandle frameFile = findFrameFile(folder, index, possiblePrefixes);

            Texture texture = new Texture(frameFile);

            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            loadedTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }

        return frames;
    }

    private FileHandle findFrameFile(String folder, int index, String... possiblePrefixes) {
        for (String prefix : possiblePrefixes) {
            String path = String.format("%s%s/%s_%03d.png", BASE_PATH, folder, prefix, index);

            FileHandle file = Gdx.files.internal(path);

            if (file.exists()) {
                return file;
            }
        }

        throw new GdxRuntimeException(
                "Knight animation frame was not found. "
                        + "Folder: "
                        + folder
                        + ", frame: "
                        + index
                        + ", tested prefixes: "
                        + String.join(", ", possiblePrefixes));
    }
}
