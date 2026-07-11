package com.hollowknight.model.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerBody;
import com.hollowknight.model.player.PlayerMovementState;

public final class PlatformWorld {

    public enum VerticalCollision {
        NONE,
        LANDED,
        HIT_CEILING
    }

    private static final float COLLISION_TOLERANCE = 3f;

    /*
     * Treat the Knight's side profile like a softly bevelled capsule rather
     * than a perfectly square box. Small contacts at the feet or head slide
     * past platform corners instead of stopping horizontal movement.
     */
    private static final float SIDE_COLLISION_BOTTOM_INSET = 15f;
    private static final float SIDE_COLLISION_TOP_INSET = 9f;

    /*
     * A landing or ceiling hit needs more than a one-pixel horizontal
     * graze. This prevents edge touches from stealing the jump arc.
     */
    private static final float MIN_VERTICAL_BLOCK_OVERLAP = 4f;

    /*
     * Small map seams and shallow lips should be walked over instead of
     * behaving like full-height walls.
     */
    private static final float MAX_STEP_HEIGHT = 14f;

    /*
     * Keep a grounded Knight attached across tiny downward seams, while
     * still allowing real platform edges to be walked off normally.
     */
    private static final float GROUND_SNAP_DISTANCE = 10f;

    /*
     * A tiny horizontal extension bridges narrow seams between neighbouring
     * collision rectangles. On a real ledge it only adds a few pixels of
     * forgiveness, so the Knight still leaves the platform naturally.
     */
    private static final float GROUND_EDGE_GRACE = 4f;

    /*
     * Small downward floor changes are followed over two or three frames
     * instead of producing a visible one-frame drop. Upward corrections stay
     * immediate so the physical body never enters a platform.
     */
    private static final float GROUND_FOLLOW_SPEED = 180f;

    /*
     * When the Knight clips the very corner of a ceiling, shift sideways
     * by a few pixels rather than cancelling all upward momentum.
     */
    private static final float CEILING_CORNER_CORRECTION = 10f;

    private static final float COLLISION_SKIN = 0.25f;

    private final Array<Platform> platforms;

    private final Rectangle groundProbe;
    private final Rectangle leftWallProbe;
    private final Rectangle rightWallProbe;
    private final Rectangle candidateBody;

    private float worldMinX;
    private float worldMaxX;
    private float worldWidth;

    public PlatformWorld() {
        platforms = new Array<>();

        groundProbe = new Rectangle();
        leftWallProbe = new Rectangle();
        rightWallProbe = new Rectangle();
        candidateBody = new Rectangle();

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

        /*
         * First try to walk onto a tiny lip. This is deliberately limited
         * to a few pixels, so normal walls and tall ledges still block.
         */
        if (
            canUseStepCorrection(
                player,
                body
            )
                && tryStepUp(
                    player,
                    playerBody,
                    previousBodyLeft,
                    previousBodyRight,
                    direction,
                    drawWidth,
                    drawHeight
                )
        ) {
            body = playerBody.getBounds();
        }

        float currentBodyLeft = body.x;
        float currentBodyRight =
            body.x + body.width;

        float bottomInset = Math.min(
            SIDE_COLLISION_BOTTOM_INSET,
            body.height * 0.28f
        );

        float topInset = Math.min(
            SIDE_COLLISION_TOP_INSET,
            body.height * 0.20f
        );

        float sideProfileBottom =
            body.y + bottomInset;

        float sideProfileTop =
            body.y + body.height - topInset;

        Rectangle blockingBounds = null;

        float nearestFace =
            direction > 0
                ? Float.POSITIVE_INFINITY
                : Float.NEGATIVE_INFINITY;

        for (Platform platform : platforms) {
            Rectangle platformBounds =
                platform.getBounds();

            float verticalOverlap =
                overlapAmount(
                    sideProfileBottom,
                    sideProfileTop,
                    platformBounds.y,
                    platformBounds.y
                        + platformBounds.height
                );

            if (verticalOverlap <= COLLISION_SKIN) {
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
         * Prevent invalid map geometry from moving the Knight across an
         * entire room during one collision correction.
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
            float highestSurface =
                Float.NEGATIVE_INFINITY;

            for (Platform platform : platforms) {
                Rectangle platformBounds =
                    platform.getBounds();

                float horizontalOverlap =
                    overlapAmount(
                        body.x,
                        body.x + body.width,
                        platformBounds.x,
                        platformBounds.x
                            + platformBounds.width
                    );

                if (
                    horizontalOverlap
                        < MIN_VERTICAL_BLOCK_OVERLAP
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
                        && body.y
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
            for (int attempt = 0; attempt < 2; attempt++) {
                body = playerBody.getBounds();

                Rectangle ceilingBounds = null;
                float lowestCeiling =
                    Float.POSITIVE_INFINITY;

                for (Platform platform : platforms) {
                    Rectangle platformBounds =
                        platform.getBounds();

                    float horizontalOverlap =
                        overlapAmount(
                            body.x,
                            body.x + body.width,
                            platformBounds.x,
                            platformBounds.x
                                + platformBounds.width
                        );

                    if (
                        horizontalOverlap
                            < MIN_VERTICAL_BLOCK_OVERLAP
                    ) {
                        continue;
                    }

                    float platformBottom =
                        platformBounds.y;

                    boolean crossedSurface =
                        previousTop
                            <= platformBottom
                            + COLLISION_TOLERANCE
                            && body.y + body.height
                            >= platformBottom
                            - COLLISION_TOLERANCE;

                    if (
                        crossedSurface
                            && platformBottom
                            < lowestCeiling
                    ) {
                        lowestCeiling =
                            platformBottom;
                        ceilingBounds =
                            platformBounds;
                    }
                }

                if (ceilingBounds == null) {
                    return VerticalCollision.NONE;
                }

                if (
                    attempt == 0
                        && tryCeilingCornerCorrection(
                            player,
                            playerBody,
                            ceilingBounds,
                            drawWidth,
                            drawHeight
                        )
                ) {
                    /*
                     * The body moved sideways out from under the corner.
                     * Recheck once in case another ceiling is adjacent.
                     */
                    continue;
                }

                body = playerBody.getBounds();
                bodyOffsetY =
                    body.y - player.getPosition().y;

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
        return findGroundSurface(
            body,
            GROUND_SNAP_DISTANCE
        ) != Float.NEGATIVE_INFINITY;
    }

    /**
     * Keeps an already-grounded Knight attached across tiny platform seams
     * and shallow downward steps. Real ledges remain unaffected because the
     * search distance is intentionally small.
     */
    public boolean snapToGround(
        Player player,
        PlayerBody playerBody,
        float delta,
        float drawWidth,
        float drawHeight
    ) {
        Rectangle body =
            playerBody.getBounds();

        float surface =
            findGroundSurface(
                body,
                GROUND_SNAP_DISTANCE
            );

        if (
            surface
                == Float.NEGATIVE_INFINITY
        ) {
            return false;
        }

        float correction =
            surface - body.y;

        if (
            Math.abs(correction)
                > GROUND_SNAP_DISTANCE
                    + COLLISION_TOLERANCE
        ) {
            return false;
        }

        if (correction < 0f) {
            float maximumFollow =
                GROUND_FOLLOW_SPEED
                    * Math.max(0f, delta);

            if (maximumFollow > 0f) {
                correction = Math.max(
                    correction,
                    -maximumFollow
                );
            }
        }

        player.getPosition().y +=
            correction;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        return true;
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

            float horizontalOverlap =
                overlapAmount(
                    bodyLeft,
                    bodyRight,
                    solidLeft,
                    solidRight
                );

            if (
                horizontalOverlap
                    < MIN_VERTICAL_BLOCK_OVERLAP
            ) {
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

    private boolean canUseStepCorrection(
        Player player,
        Rectangle body
    ) {
        PlayerMovementState state =
            player.getMovementState();

        if (
            state == PlayerMovementState.GROUNDED
                || state
                == PlayerMovementState.FALLING
        ) {
            return true;
        }

        /*
         * A ground dash may cross a tiny seam too, but an airborne dash
         * should never gain height merely by touching a platform side.
         */
        return state
            == PlayerMovementState.DASHING
            && hasGroundSupport(body);
    }

    private boolean tryStepUp(
        Player player,
        PlayerBody playerBody,
        float previousBodyLeft,
        float previousBodyRight,
        int direction,
        float drawWidth,
        float drawHeight
    ) {
        Rectangle body =
            playerBody.getBounds();

        Rectangle bestPlatform = null;
        float bestCorrection =
            Float.POSITIVE_INFINITY;

        /*
         * LibGDX Array reuses iterator instances and rejects nested
         * enhanced-for loops. This method calls overlapsAnyPlatform()
         * while scanning the same array, so use an index loop here.
         */
        for (
            int platformIndex = 0;
            platformIndex < platforms.size;
            platformIndex++
        ) {
            Platform platform =
                platforms.get(platformIndex);

            Rectangle solid =
                platform.getBounds();

            float solidTop =
                solid.y + solid.height;

            float correction =
                solidTop - body.y;

            if (
                correction <= COLLISION_SKIN
                    || correction
                    > MAX_STEP_HEIGHT
            ) {
                continue;
            }

            boolean crossedSide;

            if (direction > 0) {
                crossedSide =
                    previousBodyRight
                        <= solid.x
                        + COLLISION_TOLERANCE
                        && body.x + body.width
                        > solid.x;
            } else {
                float solidRight =
                    solid.x + solid.width;

                crossedSide =
                    previousBodyLeft
                        >= solidRight
                        - COLLISION_TOLERANCE
                        && body.x < solidRight;
            }

            if (!crossedSide) {
                continue;
            }

            candidateBody.set(body);
            candidateBody.y +=
                correction + COLLISION_SKIN;

            if (
                candidateBody.x < worldMinX
                    || candidateBody.x
                        + candidateBody.width
                        > worldMaxX
                    || overlapsAnyPlatform(
                        candidateBody
                    )
            ) {
                continue;
            }

            if (correction < bestCorrection) {
                bestCorrection = correction;
                bestPlatform = solid;
            }
        }

        if (bestPlatform == null) {
            return false;
        }

        player.getPosition().y +=
            bestCorrection
                + COLLISION_SKIN;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        return true;
    }

    private boolean tryCeilingCornerCorrection(
        Player player,
        PlayerBody playerBody,
        Rectangle ceiling,
        float drawWidth,
        float drawHeight
    ) {
        Rectangle body =
            playerBody.getBounds();

        float leftCornerOverlap =
            body.x + body.width
                - ceiling.x;

        float rightCornerOverlap =
            ceiling.x + ceiling.width
                - body.x;

        float correction = 0f;

        if (
            leftCornerOverlap > 0f
                && leftCornerOverlap
                <= CEILING_CORNER_CORRECTION
        ) {
            correction =
                -leftCornerOverlap
                    - COLLISION_SKIN;
        }

        if (
            rightCornerOverlap > 0f
                && rightCornerOverlap
                <= CEILING_CORNER_CORRECTION
                && (
                    correction == 0f
                        || rightCornerOverlap
                        < Math.abs(correction)
                )
        ) {
            correction =
                rightCornerOverlap
                    + COLLISION_SKIN;
        }

        if (correction == 0f) {
            return false;
        }

        candidateBody.set(body);
        candidateBody.x += correction;

        if (
            candidateBody.x < worldMinX
                || candidateBody.x
                    + candidateBody.width
                    > worldMaxX
                || overlapsAnyPlatform(
                    candidateBody
                )
        ) {
            return false;
        }

        player.getPosition().x +=
            correction;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        return true;
    }

    private float findGroundSurface(
        Rectangle body,
        float maximumDistance
    ) {
        float highestSurface =
            Float.NEGATIVE_INFINITY;

        for (Platform platform : platforms) {
            Rectangle solid =
                platform.getBounds();

            float horizontalOverlap =
                overlapAmount(
                    body.x - GROUND_EDGE_GRACE,
                    body.x + body.width
                        + GROUND_EDGE_GRACE,
                    solid.x,
                    solid.x + solid.width
                );

            if (
                horizontalOverlap
                    < MIN_VERTICAL_BLOCK_OVERLAP
            ) {
                continue;
            }

            float surface =
                solid.y + solid.height;

            float distance =
                body.y - surface;

            if (
                distance
                    >= -COLLISION_TOLERANCE
                    && distance
                    <= maximumDistance
                    && surface > highestSurface
            ) {
                highestSurface = surface;
            }
        }

        return highestSurface;
    }

    private float overlapAmount(
        float firstMin,
        float firstMax,
        float secondMin,
        float secondMax
    ) {
        return Math.max(
            0f,
            Math.min(firstMax, secondMax)
                - Math.max(firstMin, secondMin)
        );
    }

    private boolean overlapsAnyPlatform(
        Rectangle rectangle
    ) {
        /*
         * Keep this index-based as well because it can be called from
         * another platform scan. LibGDX Array iterators are not nestable.
         */
        for (
            int platformIndex = 0;
            platformIndex < platforms.size;
            platformIndex++
        ) {
            Platform platform =
                platforms.get(platformIndex);

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
