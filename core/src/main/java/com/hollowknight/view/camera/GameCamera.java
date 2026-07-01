package com.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Controls the camera used to render the game world.
 *
 * The UI uses a different camera, so HUD elements
 * remain fixed on the screen.
 */
public final class GameCamera {

    private static final float FOLLOW_SPEED = 7f;

    /*
     * If the player suddenly moves a large distance,
     * such as during respawning, the camera snaps
     * instead of slowly showing an empty area.
     */
    private static final float
        SNAP_DISTANCE_RATIO = 0.70f;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final float worldWidth;
    private final float worldHeight;

    public GameCamera(
        float visibleWidth,
        float visibleHeight,
        float worldWidth,
        float worldHeight
    ) {
        if (
            visibleWidth <= 0f
                || visibleHeight <= 0f
                || worldWidth <= 0f
                || worldHeight <= 0f
        ) {
            throw new IllegalArgumentException(
                "Camera and world dimensions must be positive."
            );
        }

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        camera = new OrthographicCamera();

        viewport = new FitViewport(
            visibleWidth,
            visibleHeight,
            camera
        );

        camera.position.set(
            visibleWidth / 2f,
            visibleHeight / 2f,
            0f
        );

        camera.update();
    }

    public void update(
        float delta,
        float targetX,
        float targetY
    ) {
        float desiredX =
            clampHorizontalPosition(
                targetX
            );

        float desiredY =
            clampVerticalPosition(
                targetY
            );

        if (
            shouldSnap(
                desiredX,
                desiredY
            )
        ) {
            camera.position.set(
                desiredX,
                desiredY,
                0f
            );

            camera.update();

            return;
        }

        /*
         * Exponential interpolation produces nearly
         * the same camera behavior at different frame
         * rates.
         */
        float safeDelta =
            Math.max(0f, delta);

        float interpolation =
            1f
                - (float) Math.exp(
                -FOLLOW_SPEED * safeDelta
            );

        camera.position.x =
            MathUtils.lerp(
                camera.position.x,
                desiredX,
                interpolation
            );

        camera.position.y =
            MathUtils.lerp(
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
            clampHorizontalPosition(
                targetX
            ),
            clampVerticalPosition(
                targetY
            ),
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
            worldWidth
                <= halfVisibleWidth * 2f
        ) {
            return worldWidth / 2f;
        }

        return MathUtils.clamp(
            targetX,
            halfVisibleWidth,
            worldWidth - halfVisibleWidth
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
            worldHeight
                <= halfVisibleHeight * 2f
        ) {
            return worldHeight / 2f;
        }

        return MathUtils.clamp(
            targetY,
            halfVisibleHeight,
            worldHeight - halfVisibleHeight
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

    public Matrix4 getCombined() {
        return camera.combined;
    }
}
