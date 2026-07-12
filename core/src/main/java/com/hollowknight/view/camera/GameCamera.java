package com.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Follows the player and clamps the view to the current Tiled room. Camera shake keeps the
 * strongest active request and eases out quadratically.
 */
public final class GameCamera {

    private static final float FOLLOW_SPEED = 7f;

    private static final float SNAP_DISTANCE_RATIO = 0.70f;

    private static final float MAX_SHAKE_FRACTION = 0.048f;

    private static final float MIN_SHAKE_DURATION = 0.24f;

    private static final float SHAKE_DURATION_SCALE = 0.55f;

    private static final float MAX_SHAKE_INTENSITY = 1.7f;

    private static final float NORMAL_SHAKE_MAGNITUDE_SCALE = 0.55f;

    private static final float BOSS_ARENA_SHAKE_MAGNITUDE_SCALE = 0.72f;

    private final OrthographicCamera camera;

    private final Viewport viewport;

    private final Rectangle worldBounds;

    private float baseX;

    private float baseY;

    private float shakeTimeRemaining;

    private float shakeDuration;

    private float shakeMagnitude;

    public GameCamera(float visibleWidth, float visibleHeight, Rectangle initialWorldBounds) {
        if (visibleWidth <= 0f
                || visibleHeight <= 0f
                || initialWorldBounds == null
                || initialWorldBounds.width <= 0f
                || initialWorldBounds.height <= 0f) {
            throw new IllegalArgumentException(
                    "Camera dimensions and world bounds must be positive.");
        }

        worldBounds = new Rectangle(initialWorldBounds);

        camera = new OrthographicCamera();

        viewport = new FitViewport(visibleWidth, visibleHeight, camera);

        baseX = worldBounds.x + visibleWidth / 2f;
        baseY = worldBounds.y + visibleHeight / 2f;

        camera.position.set(baseX, baseY, 0f);

        camera.update();
    }

    public void shake(float intensity) {
        shakeWithMagnitudeScale(intensity, NORMAL_SHAKE_MAGNITUDE_SCALE);
    }

    public void shakeBossArena(float intensity) {
        shakeWithMagnitudeScale(intensity, BOSS_ARENA_SHAKE_MAGNITUDE_SCALE);
    }

    public void setWorldBounds(Rectangle bounds) {
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            throw new IllegalArgumentException("World bounds must be positive.");
        }

        worldBounds.set(bounds);

        baseX = clampHorizontalPosition(baseX);

        baseY = clampVerticalPosition(baseY);

        applyRenderPosition(0f);
    }

    public void update(float delta, float targetX, float targetY) {
        float desiredX = clampHorizontalPosition(targetX);

        float desiredY = clampVerticalPosition(targetY);

        float safeDelta = Math.max(0f, delta);

        if (shouldSnap(desiredX, desiredY)) {
            baseX = desiredX;
            baseY = desiredY;
        } else {
            float interpolation = 1f - (float) Math.exp(-FOLLOW_SPEED * safeDelta);

            baseX = MathUtils.lerp(baseX, desiredX, interpolation);

            baseY = MathUtils.lerp(baseY, desiredY, interpolation);
        }

        applyRenderPosition(safeDelta);
    }

    public void reset(float targetX, float targetY) {
        baseX = clampHorizontalPosition(targetX);
        baseY = clampVerticalPosition(targetY);

        shakeTimeRemaining = 0f;
        shakeMagnitude = 0f;

        applyRenderPosition(0f);
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);

        baseX = clampHorizontalPosition(baseX);

        baseY = clampVerticalPosition(baseY);

        applyRenderPosition(0f);
    }

    public void apply() {
        viewport.apply();
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Matrix4 getCombined() {
        return camera.combined;
    }

    private void shakeWithMagnitudeScale(float intensity, float magnitudeScale) {
        if (intensity <= 0f) {
            return;
        }

        float clamped = MathUtils.clamp(intensity, 0f, MAX_SHAKE_INTENSITY);

        float newMagnitude =
                clamped * camera.viewportWidth * camera.zoom * MAX_SHAKE_FRACTION * magnitudeScale;

        float newDuration = MIN_SHAKE_DURATION + clamped * SHAKE_DURATION_SCALE;

        if (shakeTimeRemaining <= 0f || newMagnitude >= shakeMagnitude) {
            shakeMagnitude = newMagnitude;
            shakeDuration = newDuration;
            shakeTimeRemaining = newDuration;
        }
    }

    private void applyRenderPosition(float delta) {
        float offsetX = 0f;
        float offsetY = 0f;

        if (shakeTimeRemaining > 0f) {
            shakeTimeRemaining -= delta;

            if (shakeTimeRemaining < 0f) {
                shakeTimeRemaining = 0f;
            }

            float progress = shakeDuration > 0f ? shakeTimeRemaining / shakeDuration : 0f;

            float currentMagnitude = shakeMagnitude * progress;

            offsetX = MathUtils.random(-1f, 1f) * currentMagnitude;

            offsetY = MathUtils.random(-1f, 1f) * currentMagnitude;
        }

        camera.position.set(baseX + offsetX, baseY + offsetY, 0f);

        camera.update();
    }

    private boolean shouldSnap(float desiredX, float desiredY) {
        float horizontalSnapDistance = camera.viewportWidth * camera.zoom * SNAP_DISTANCE_RATIO;

        float verticalSnapDistance = camera.viewportHeight * camera.zoom * SNAP_DISTANCE_RATIO;

        return Math.abs(desiredX - baseX) > horizontalSnapDistance
                || Math.abs(desiredY - baseY) > verticalSnapDistance;
    }

    private float clampHorizontalPosition(float targetX) {
        float halfVisibleWidth = camera.viewportWidth * camera.zoom / 2f;

        if (worldBounds.width <= halfVisibleWidth * 2f) {
            return worldBounds.x + worldBounds.width / 2f;
        }

        return MathUtils.clamp(
                targetX,
                worldBounds.x + halfVisibleWidth,
                worldBounds.x + worldBounds.width - halfVisibleWidth);
    }

    private float clampVerticalPosition(float targetY) {
        float halfVisibleHeight = camera.viewportHeight * camera.zoom / 2f;

        if (worldBounds.height <= halfVisibleHeight * 2f) {
            return worldBounds.y + worldBounds.height / 2f;
        }

        return MathUtils.clamp(
                targetY,
                worldBounds.y + halfVisibleHeight,
                worldBounds.y + worldBounds.height - halfVisibleHeight);
    }
}
