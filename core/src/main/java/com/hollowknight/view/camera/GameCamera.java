package com.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Controls the world camera and clamps it to the current Tiled room.
 */
public final class GameCamera {

    private static final float FOLLOW_SPEED = 7f;
    private static final float SNAP_DISTANCE_RATIO = 0.70f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Rectangle worldBounds;

    public GameCamera(
        float visibleWidth,
        float visibleHeight,
        Rectangle initialWorldBounds
    ) {
        if (
            visibleWidth <= 0f
                || visibleHeight <= 0f
                || initialWorldBounds == null
                || initialWorldBounds.width <= 0f
                || initialWorldBounds.height <= 0f
        ) {
            throw new IllegalArgumentException(
                "Camera dimensions and world bounds must be positive."
            );
        }

        worldBounds = new Rectangle(
            initialWorldBounds
        );

        camera = new OrthographicCamera();

        viewport = new FitViewport(
            visibleWidth,
            visibleHeight,
            camera
        );

        camera.position.set(
            worldBounds.x + visibleWidth / 2f,
            worldBounds.y + visibleHeight / 2f,
            0f
        );

        camera.update();
    }

    public void setWorldBounds(Rectangle bounds) {
        if (
            bounds == null
                || bounds.width <= 0f
                || bounds.height <= 0f
        ) {
            throw new IllegalArgumentException(
                "World bounds must be positive."
            );
        }

        worldBounds.set(bounds);

        camera.position.x =
            clampHorizontalPosition(
                camera.position.x
            );

        camera.position.y =
            clampVerticalPosition(
                camera.position.y
            );

        camera.update();
    }

    public void update(
        float delta,
        float targetX,
        float targetY
    ) {
        float desiredX =
            clampHorizontalPosition(targetX);

        float desiredY =
            clampVerticalPosition(targetY);

        if (shouldSnap(desiredX, desiredY)) {
            camera.position.set(
                desiredX,
                desiredY,
                0f
            );

            camera.update();
            return;
        }

        float safeDelta = Math.max(0f, delta);

        float interpolation =
            1f
                - (float) Math.exp(
                -FOLLOW_SPEED * safeDelta
            );

        camera.position.x = MathUtils.lerp(
            camera.position.x,
            desiredX,
            interpolation
        );

        camera.position.y = MathUtils.lerp(
            camera.position.y,
            desiredY,
            interpolation
        );

        camera.position.z = 0f;
        camera.update();
    }

    public void reset(
        float targetX,
        float targetY
    ) {
        camera.position.set(
            clampHorizontalPosition(targetX),
            clampVerticalPosition(targetY),
            0f
        );

        camera.update();
    }

    private boolean shouldSnap(
        float desiredX,
        float desiredY
    ) {
        float horizontalSnapDistance =
            camera.viewportWidth
                * camera.zoom
                * SNAP_DISTANCE_RATIO;

        float verticalSnapDistance =
            camera.viewportHeight
                * camera.zoom
                * SNAP_DISTANCE_RATIO;

        return Math.abs(
            desiredX - camera.position.x
        ) > horizontalSnapDistance
            || Math.abs(
            desiredY - camera.position.y
        ) > verticalSnapDistance;
    }

    private float clampHorizontalPosition(
        float targetX
    ) {
        float halfVisibleWidth =
            camera.viewportWidth
                * camera.zoom
                / 2f;

        if (
            worldBounds.width
                <= halfVisibleWidth * 2f
        ) {
            return worldBounds.x
                + worldBounds.width / 2f;
        }

        return MathUtils.clamp(
            targetX,
            worldBounds.x + halfVisibleWidth,
            worldBounds.x
                + worldBounds.width
                - halfVisibleWidth
        );
    }

    private float clampVerticalPosition(
        float targetY
    ) {
        float halfVisibleHeight =
            camera.viewportHeight
                * camera.zoom
                / 2f;

        if (
            worldBounds.height
                <= halfVisibleHeight * 2f
        ) {
            return worldBounds.y
                + worldBounds.height / 2f;
        }

        return MathUtils.clamp(
            targetY,
            worldBounds.y + halfVisibleHeight,
            worldBounds.y
                + worldBounds.height
                - halfVisibleHeight
        );
    }

    public void resize(
        int screenWidth,
        int screenHeight
    ) {
        viewport.update(
            screenWidth,
            screenHeight,
            false
        );

        camera.position.x =
            clampHorizontalPosition(
                camera.position.x
            );

        camera.position.y =
            clampVerticalPosition(
                camera.position.y
            );

        camera.update();
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
}
