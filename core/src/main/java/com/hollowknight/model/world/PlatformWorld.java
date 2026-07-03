package com.hollowknight.model.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerBody;

public final class PlatformWorld {

    public enum VerticalCollision {
        NONE,
        LANDED,
        HIT_CEILING
    }

    private static final float FLOOR_HEIGHT = 100f;
    private static final float COLLISION_TOLERANCE = 3f;

    private final Array<Platform> platforms;

    private final Rectangle groundProbe;
    private final Rectangle leftWallProbe;
    private final Rectangle rightWallProbe;

    private float worldWidth;

    public PlatformWorld() {
        platforms = new Array<>();

        groundProbe = new Rectangle();
        leftWallProbe = new Rectangle();
        rightWallProbe = new Rectangle();

        worldWidth = 0f;
    }

    /**
     * Creates a temporary platform layout that adjusts
     * to the current window width.
     *
     * Later this class will receive platforms from the
     * actual map instead.
     */
    public void updateLayout(float newWorldWidth) {
        if (newWorldWidth <= 0f) {
            return;
        }

        if (
            Math.abs(newWorldWidth - worldWidth)
                < 0.5f
                && platforms.size > 0
        ) {
            return;
        }

        worldWidth = newWorldWidth;

        platforms.clear();

        /*
         * Main floor.
         */
        platforms.add(
            new Platform(
                0f,
                0f,
                worldWidth,
                FLOOR_HEIGHT
            )
        );

        /*
         * Temporary raised platforms.
         */
        platforms.add(
            new Platform(
                worldWidth * 0.18f,
                210f,
                worldWidth * 0.18f,
                30f
            )
        );

        platforms.add(
            new Platform(
                worldWidth * 0.43f,
                340f,
                worldWidth * 0.16f,
                30f
            )
        );

        platforms.add(
            new Platform(
                worldWidth * 0.70f,
                235f,
                worldWidth * 0.20f,
                30f
            )
        );

        /*
         * Two temporary vertical blocks for testing
         * wall sliding and wall jumping.
         */
        platforms.add(
            new Platform(
                worldWidth * 0.37f,
                FLOOR_HEIGHT,
                38f,
                175f
            )
        );

        platforms.add(
            new Platform(
                worldWidth * 0.65f,
                FLOOR_HEIGHT,
                38f,
                225f
            )
        );
    }

    public boolean resolveHorizontal(
        Player player,
        PlayerBody playerBody,
        float previousPlayerX,
        int direction,
        float drawWidth,
        float drawHeight
    ) {
        if (direction == 0) {
            return false;
        }

        Rectangle body =
            playerBody.getBounds();

        float bodyOffsetX =
            body.x - player.getPosition().x;

        boolean collided = false;

        for (Platform platform : platforms) {
            Rectangle platformBounds =
                platform.getBounds();

            if (!body.overlaps(platformBounds)) {
                continue;
            }

            if (direction > 0) {
                player.getPosition().x =
                    platformBounds.x
                        - body.width
                        - bodyOffsetX;
            } else {
                player.getPosition().x =
                    platformBounds.x
                        + platformBounds.width
                        - bodyOffsetX;
            }

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );

            body = playerBody.getBounds();
            collided = true;
        }

        clampPlayerInsideWorld(
            player,
            playerBody,
            drawWidth,
            drawHeight
        );

        /*
         * Prevent rare cases where resolving one
         * platform pushes the Knight through another.
         */
        if (
            Math.abs(
                player.getPosition().x
                    - previousPlayerX
            ) > worldWidth
        ) {
            player.getPosition().x =
                previousPlayerX;

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );

            collided = true;
        }

        return collided;
    }

    public VerticalCollision resolveVertical(
        Player player,
        PlayerBody playerBody,
        float previousBottom,
        float previousTop,
        float verticalVelocity,
        float drawWidth,
        float drawHeight
    ) {
        Rectangle body =
            playerBody.getBounds();

        float bodyOffsetY =
            body.y - player.getPosition().y;

        if (verticalVelocity <= 0f) {
            Platform landingPlatform = null;
            float highestSurface =
                Float.NEGATIVE_INFINITY;

            for (Platform platform : platforms) {
                Rectangle platformBounds =
                    platform.getBounds();

                if (!body.overlaps(platformBounds)) {
                    continue;
                }

                float platformTop =
                    platformBounds.y
                        + platformBounds.height;

                if (
                    previousBottom
                        >= platformTop
                        - COLLISION_TOLERANCE
                        && platformTop
                        > highestSurface
                ) {
                    landingPlatform = platform;
                    highestSurface = platformTop;
                }
            }

            if (landingPlatform != null) {
                player.getPosition().y =
                    highestSurface
                        - bodyOffsetY;

                playerBody.update(
                    player,
                    drawWidth,
                    drawHeight
                );

                return VerticalCollision.LANDED;
            }
        } else {
            Platform ceilingPlatform = null;
            float lowestCeiling =
                Float.POSITIVE_INFINITY;

            for (Platform platform : platforms) {
                Rectangle platformBounds =
                    platform.getBounds();

                if (!body.overlaps(platformBounds)) {
                    continue;
                }

                float platformBottom =
                    platformBounds.y;

                if (
                    previousTop
                        <= platformBottom
                        + COLLISION_TOLERANCE
                        && platformBottom
                        < lowestCeiling
                ) {
                    ceilingPlatform = platform;
                    lowestCeiling = platformBottom;
                }
            }

            if (ceilingPlatform != null) {
                player.getPosition().y =
                    lowestCeiling
                        - body.height
                        - bodyOffsetY;

                playerBody.update(
                    player,
                    drawWidth,
                    drawHeight
                );

                return VerticalCollision.HIT_CEILING;
            }
        }

        return VerticalCollision.NONE;
    }

    public boolean hasGroundSupport(
        Rectangle body
    ) {
        float probeWidth =
            Math.max(
                1f,
                body.width - 6f
            );

        groundProbe.set(
            body.x + 3f,
            body.y - 4f,
            probeWidth,
            5f
        );

        return overlapsAnyPlatform(
            groundProbe
        );
    }

    public int getHeldWallSide(
        Rectangle body,
        PlayerInput input
    ) {
        int touchingSide =
            getTouchingWallSide(body);

        if (
            touchingSide == -1
                && input.isMoveLeftHeld()
        ) {
            return -1;
        }

        if (
            touchingSide == 1
                && input.isMoveRightHeld()
        ) {
            return 1;
        }

        return 0;
    }

    private int getTouchingWallSide(
        Rectangle body
    ) {
        float probeHeight =
            Math.max(
                1f,
                body.height - 12f
            );

        leftWallProbe.set(
            body.x - 4f,
            body.y + 6f,
            5f,
            probeHeight
        );

        if (
            overlapsAnyPlatform(
                leftWallProbe
            )
        ) {
            return -1;
        }

        rightWallProbe.set(
            body.x
                + body.width
                - 1f,
            body.y + 6f,
            5f,
            probeHeight
        );

        if (
            overlapsAnyPlatform(
                rightWallProbe
            )
        ) {
            return 1;
        }

        return 0;
    }

    public boolean overlapsSolid(
        Rectangle rectangle
    ) {
        return overlapsAnyPlatform(
            rectangle
        );
    }

    public boolean hasGroundAhead(
        Rectangle body,
        int direction,
        float lookAhead,
        float probeDepth
    ) {
        if (direction == 0) {
            return hasGroundSupport(body);
        }

        float probeWidth = 4f;

        float probeX =
            direction > 0
                ? body.x
                + body.width
                + lookAhead
                : body.x
                - lookAhead
                - probeWidth;

        groundProbe.set(
            probeX,
            body.y - probeDepth,
            probeWidth,
            probeDepth + 2f
        );

        return overlapsAnyPlatform(
            groundProbe
        );
    }

    public float getHorizontalRayLength(
        float startX,
        float rayY,
        float rayHeight,
        int direction,
        float maximumRange
    ) {
        if (
            direction == 0
                || maximumRange <= 0f
                || rayHeight <= 0f
        ) {
            return 0f;
        }

        int normalizedDirection =
            direction > 0 ? 1 : -1;

        float distanceToWorldBoundary =
            normalizedDirection > 0
                ? worldWidth - startX
                : startX;

        float result =
            Math.min(
                maximumRange,
                Math.max(
                    0f,
                    distanceToWorldBoundary
                )
            );

        float rayTop =
            rayY + rayHeight;

        for (Platform platform : platforms) {
            Rectangle solid =
                platform.getBounds();

            float solidTop =
                solid.y + solid.height;

            /*
             * Ignore platforms that do not intersect the
             * laser vertically.
             */
            if (
                solid.y >= rayTop
                    || solidTop <= rayY
            ) {
                continue;
            }

            float distance;

            if (normalizedDirection > 0) {
                if (solid.x < startX) {
                    continue;
                }

                distance =
                    solid.x - startX;
            } else {
                float solidRight =
                    solid.x + solid.width;

                if (solidRight > startX) {
                    continue;
                }

                distance =
                    startX - solidRight;
            }

            if (distance < result) {
                result = Math.max(
                    0f,
                    distance
                );
            }
        }

        return result;
    }


    private boolean overlapsAnyPlatform(
        Rectangle rectangle
    ) {
        for (Platform platform : platforms) {
            if (
                rectangle.overlaps(
                    platform.getBounds()
                )
            ) {
                return true;
            }
        }

        return false;
    }

    public void clampPlayerInsideWorld(
        Player player,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        Rectangle body =
            playerBody.getBounds();

        if (body.x < 0f) {
            player.getPosition().x -=
                body.x;

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );

            body = playerBody.getBounds();
        }

        float bodyRight =
            body.x + body.width;

        if (bodyRight > worldWidth) {
            player.getPosition().x -=
                bodyRight - worldWidth;

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );
        }

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                -drawWidth,
                worldWidth
            );
    }

    public Array<Platform> getPlatforms() {
        return platforms;
    }

    public float getFloorHeight() {
        return FLOOR_HEIGHT;
    }

    public float getWorldWidth() {
        return worldWidth;
    }
}
