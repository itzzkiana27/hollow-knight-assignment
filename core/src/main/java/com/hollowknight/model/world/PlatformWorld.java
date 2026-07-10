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

    private static final float COLLISION_TOLERANCE = 3f;

    /*
     * Ignore tiny top/bottom corner contacts while resolving sideways
     * movement. Without this inset, brushing a platform corner by one
     * pixel can be treated as a full wall and make the Knight stick.
     */
    private static final float HORIZONTAL_CORNER_INSET = 6f;

    /*
     * Likewise, require a useful amount of horizontal overlap before
     * a platform can catch the Knight vertically. This keeps jump arcs
     * smooth when only the edge of the body grazes a platform.
     */
    private static final float VERTICAL_CORNER_INSET = 3f;

    private static final float GROUND_SUPPORT_DISTANCE = 4f;

    private final Array<Platform> platforms;

    private final Rectangle groundProbe;
    private final Rectangle leftWallProbe;
    private final Rectangle rightWallProbe;

    private float worldMinX;
    private float worldMaxX;
    private float worldWidth;

    public PlatformWorld() {
        platforms = new Array<>();

        groundProbe = new Rectangle();
        leftWallProbe = new Rectangle();
        rightWallProbe = new Rectangle();

        worldMinX = 0f;
        worldMaxX = 0f;
        worldWidth = 0f;
    }

    /**
     * Replaces the temporary generated layout with collision rectangles
     * loaded from Tiled.
     */
    public void configure(
        Rectangle roomBounds,
        Array<Platform> loadedPlatforms
    ) {
        if (
            roomBounds == null
                || roomBounds.width <= 0f
        ) {
            throw new IllegalArgumentException(
                "Room bounds must have a positive width."
            );
        }

        worldMinX = roomBounds.x;
        worldMaxX =
            roomBounds.x + roomBounds.width;
        worldWidth = roomBounds.width;

        platforms.clear();

        if (loadedPlatforms != null) {
            platforms.addAll(loadedPlatforms);
        }
    }

    public void addPlatform(Platform platform) {
        if (
            platform != null
                && !platforms.contains(
                    platform,
                    true
                )
        ) {
            platforms.add(platform);
        }
    }

    public void removePlatform(Platform platform) {
        if (platform != null) {
            platforms.removeValue(
                platform,
                true
            );
        }
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

        float previousBodyLeft =
            previousPlayerX + bodyOffsetX;

        float previousBodyRight =
            previousBodyLeft + body.width;

        float currentBodyLeft = body.x;
        float currentBodyRight =
            body.x + body.width;

        float collisionBottom =
            body.y + HORIZONTAL_CORNER_INSET;

        float collisionTop =
            body.y
                + body.height
                - HORIZONTAL_CORNER_INSET;

        Rectangle blockingBounds = null;

        float nearestFace =
            direction > 0
                ? Float.POSITIVE_INFINITY
                : Float.NEGATIVE_INFINITY;

        for (Platform platform : platforms) {
            Rectangle platformBounds =
                platform.getBounds();

            if (
                !rangesOverlap(
                    collisionBottom,
                    collisionTop,
                    platformBounds.y,
                    platformBounds.y
                        + platformBounds.height
                )
            ) {
                continue;
            }

            if (direction > 0) {
                float platformFace =
                    platformBounds.x;

                boolean crossedFace =
                    previousBodyRight
                        <= platformFace
                        + COLLISION_TOLERANCE
                        && currentBodyRight
                        > platformFace;

                boolean penetratingFromLeft =
                    previousBodyLeft < platformFace
                        && currentBodyLeft < platformFace
                        && currentBodyRight
                        > platformFace;

                if (
                    (crossedFace
                        || penetratingFromLeft)
                        && platformFace < nearestFace
                ) {
                    nearestFace = platformFace;
                    blockingBounds = platformBounds;
                }
            } else {
                float platformFace =
                    platformBounds.x
                        + platformBounds.width;

                boolean crossedFace =
                    previousBodyLeft
                        >= platformFace
                        - COLLISION_TOLERANCE
                        && currentBodyLeft
                        < platformFace;

                boolean penetratingFromRight =
                    previousBodyRight > platformFace
                        && currentBodyRight > platformFace
                        && currentBodyLeft
                        < platformFace;

                if (
                    (crossedFace
                        || penetratingFromRight)
                        && platformFace > nearestFace
                ) {
                    nearestFace = platformFace;
                    blockingBounds = platformBounds;
                }
            }
        }

        boolean collided =
            blockingBounds != null;

        if (collided) {
            if (direction > 0) {
                player.getPosition().x =
                    blockingBounds.x
                        - body.width
                        - bodyOffsetX;
            } else {
                player.getPosition().x =
                    blockingBounds.x
                        + blockingBounds.width
                        - bodyOffsetX;
            }

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );
        }

        clampPlayerInsideWorld(
            player,
            playerBody,
            drawWidth,
            drawHeight
        );

        /*
         * Prevent rare cases where invalid map geometry
         * would move the Knight across the whole room.
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

        float collisionLeft =
            body.x + VERTICAL_CORNER_INSET;

        float collisionRight =
            body.x
                + body.width
                - VERTICAL_CORNER_INSET;

        float currentBottom = body.y;
        float currentTop =
            body.y + body.height;

        if (verticalVelocity <= 0f) {
            float highestSurface =
                Float.NEGATIVE_INFINITY;

            for (Platform platform : platforms) {
                Rectangle platformBounds =
                    platform.getBounds();

                if (
                    !rangesOverlap(
                        collisionLeft,
                        collisionRight,
                        platformBounds.x,
                        platformBounds.x
                            + platformBounds.width
                    )
                ) {
                    continue;
                }

                float platformTop =
                    platformBounds.y
                        + platformBounds.height;

                boolean crossedSurface =
                    previousBottom
                        >= platformTop
                        - COLLISION_TOLERANCE
                        && currentBottom
                        <= platformTop
                        + COLLISION_TOLERANCE;

                if (
                    crossedSurface
                        && platformTop
                        > highestSurface
                ) {
                    highestSurface = platformTop;
                }
            }

            if (
                highestSurface
                    != Float.NEGATIVE_INFINITY
            ) {
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
            float lowestCeiling =
                Float.POSITIVE_INFINITY;

            for (Platform platform : platforms) {
                Rectangle platformBounds =
                    platform.getBounds();

                if (
                    !rangesOverlap(
                        collisionLeft,
                        collisionRight,
                        platformBounds.x,
                        platformBounds.x
                            + platformBounds.width
                    )
                ) {
                    continue;
                }

                float platformBottom =
                    platformBounds.y;

                boolean crossedSurface =
                    previousTop
                        <= platformBottom
                        + COLLISION_TOLERANCE
                        && currentTop
                        >= platformBottom
                        - COLLISION_TOLERANCE;

                if (
                    crossedSurface
                        && platformBottom
                        < lowestCeiling
                ) {
                    lowestCeiling =
                        platformBottom;
                }
            }

            if (
                lowestCeiling
                    != Float.POSITIVE_INFINITY
            ) {
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
        float supportLeft =
            body.x + VERTICAL_CORNER_INSET;

        float supportRight =
            body.x
                + body.width
                - VERTICAL_CORNER_INSET;

        float bodyBottom = body.y;

        for (Platform platform : platforms) {
            Rectangle platformBounds =
                platform.getBounds();

            if (
                !rangesOverlap(
                    supportLeft,
                    supportRight,
                    platformBounds.x,
                    platformBounds.x
                        + platformBounds.width
                )
            ) {
                continue;
            }

            float platformTop =
                platformBounds.y
                    + platformBounds.height;

            if (
                bodyBottom
                    >= platformTop
                    - COLLISION_TOLERANCE
                    && bodyBottom
                    <= platformTop
                    + GROUND_SUPPORT_DISTANCE
            ) {
                return true;
            }
        }

        return false;
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
                ? worldMaxX - startX
                : startX - worldMinX;

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

    public float findLandingSurfaceY(
        Rectangle body,
        float previousBottom,
        float nextBottom
    ) {
        float highestSurface =
            Float.NEGATIVE_INFINITY;

        float bodyLeft = body.x;
        float bodyRight =
            body.x + body.width;

        for (Platform platform : platforms) {
            Rectangle solid =
                platform.getBounds();

            float solidLeft = solid.x;

            float solidRight =
                solid.x + solid.width;

            boolean horizontalOverlap =
                bodyRight > solidLeft
                    && bodyLeft < solidRight;

            if (!horizontalOverlap) {
                continue;
            }

            float platformTop =
                solid.y + solid.height;

            boolean crossedSurface =
                previousBottom
                    >= platformTop
                    - COLLISION_TOLERANCE
                    && nextBottom
                    <= platformTop
                    + COLLISION_TOLERANCE;

            if (
                crossedSurface
                    && platformTop > highestSurface
            ) {
                highestSurface = platformTop;
            }
        }

        if (
            highestSurface
                == Float.NEGATIVE_INFINITY
        ) {
            return Float.NaN;
        }

        return highestSurface;
    }

    private boolean rangesOverlap(
        float firstMin,
        float firstMax,
        float secondMin,
        float secondMax
    ) {
        return firstMax > secondMin
            && firstMin < secondMax;
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

        if (body.x < worldMinX) {
            player.getPosition().x +=
                worldMinX - body.x;

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );

            body = playerBody.getBounds();
        }

        float bodyRight =
            body.x + body.width;

        if (bodyRight > worldMaxX) {
            player.getPosition().x -=
                bodyRight - worldMaxX;

            playerBody.update(
                player,
                drawWidth,
                drawHeight
            );
        }

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                worldMinX - drawWidth,
                worldMaxX
            );
    }

    public Array<Platform> getPlatforms() {
        return platforms;
    }

    public float getWorldMinX() {
        return worldMinX;
    }

    public float getWorldMaxX() {
        return worldMaxX;
    }

    /**
     * Kept for the current enemy classes, whose Crossroads room starts at x=0.
     */
    public float getWorldWidth() {
        return worldWidth;
    }
}
