package com.hollowknight.model.npc;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Zote {

    public enum State {
        IDLE,
        TALKING,
        ANGRY,
        ATTACKING
    }

    private final Rectangle bounds;
    private final String roomId;
    private final float interactionRadius;

    private State state = State.IDLE;

    private boolean facingRight;
    private float animationTime;

    private float angryTimer;
    private float attackTimer;

    private static final float ANGRY_DURATION = 4f;
    private static final float ATTACK_SPEED = 200f;
    private static final float PLAYER_STOP_GAP = 24f;
    private final float moveMinX;
    private final float moveMaxX;

    public Zote(
        float x,
        float y,
        float width,
        float height,
        String roomId,
        boolean facingRight,
        float interactionRadius,
        float moveMinX,
        float moveMaxX
    ) {
        this.bounds = new Rectangle(
            x,
            y,
            width,
            height
        );

        this.roomId = roomId;
        this.facingRight = facingRight;
        this.interactionRadius = interactionRadius;
        this.moveMinX = moveMinX;
        this.moveMaxX = moveMaxX;
    }

    public void update(
        float delta,
        Rectangle playerBounds
    ) {
        animationTime += delta;

        if (
            state != State.ANGRY
                && state != State.ATTACKING
        ) {
            return;
        }

        angryTimer -= delta;

        if (angryTimer <= 0f) {
            state = State.IDLE;
            return;
        }

        state = State.ATTACKING;

        float zoteCenterX =
            bounds.x + bounds.width / 2f;

        float playerCenterX =
            playerBounds.x + playerBounds.width / 2f;

        boolean zoteIsLeftOfPlayer =
            zoteCenterX < playerCenterX;

        float targetX;

        if (zoteIsLeftOfPlayer) {
            targetX =
                playerBounds.x
                    - bounds.width
                    - PLAYER_STOP_GAP;
        } else {
            targetX =
                playerBounds.x
                    + playerBounds.width
                    + PLAYER_STOP_GAP;
        }

        float minX = moveMinX;
        float maxX = moveMaxX - bounds.width;

        if (targetX < minX) {
            targetX = minX;
        }

        if (targetX > maxX) {
            targetX = maxX;
        }

        float distanceX =
            targetX - bounds.x;

        if (Math.abs(distanceX) <= 2f) {
            return;
        }

        facingRight = distanceX > 0f;

        float moveAmount =
            ATTACK_SPEED * delta;

        if (Math.abs(distanceX) < moveAmount) {
            bounds.x = targetX;
        } else {
            bounds.x += Math.signum(distanceX)
                * moveAmount;
        }

        if (bounds.x < minX) {
            bounds.x = minX;
        }

        if (bounds.x > maxX) {
            bounds.x = maxX;
        }
    }


    public void startTalking() {
        if (state != State.ANGRY && state != State.ATTACKING) {
            state = State.TALKING;
            animationTime = 0f;
        }
    }

    public void stopTalking() {
        if (state == State.TALKING) {
            state = State.IDLE;
            animationTime = 0f;
        }
    }

    public void hitByPlayer() {
        state = State.ANGRY;
        angryTimer = ANGRY_DURATION;
        attackTimer = ANGRY_DURATION;
        animationTime = 0f;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getRoomId() {
        return roomId;
    }

    public State getState() {
        return state;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public boolean isAngry() {
        return state == State.ANGRY
            || state == State.ATTACKING;
    }
    public float getInteractionRadius() {
        return interactionRadius;
    }

    public boolean isPlayerInInteractionRange(
        Rectangle playerBounds
    ) {
        float zoteCenterX =
            bounds.x + bounds.width / 2f;

        float zoteCenterY =
            bounds.y + bounds.height / 2f;

        float playerCenterX =
            playerBounds.x + playerBounds.width / 2f;

        float playerCenterY =
            playerBounds.y + playerBounds.height / 2f;

        float dx = zoteCenterX - playerCenterX;
        float dy = zoteCenterY - playerCenterY;

        return dx * dx + dy * dy
            <= interactionRadius * interactionRadius;
    }
}
