package com.hollowknight.model.player;

import com.badlogic.gdx.math.Vector2;

public class Player {

    private final Vector2 position;

    private PlayerAnimationType animationType;
    private PlayerMovementState movementState;

    private float animationTime;
    private boolean facingRight;
    private boolean dead;

    public Player(float x, float y) {
        position = new Vector2(x, y);

        animationType = PlayerAnimationType.IDLE;
        movementState = PlayerMovementState.GROUNDED;

        animationTime = 0f;
        facingRight = true;
        dead = false;
    }

    public void setAnimation(PlayerAnimationType newAnimation) {
        if (animationType != newAnimation) {
            animationType = newAnimation;
            animationTime = 0f;
        }
    }

    public void updateAnimationTime(float delta) {
        animationTime += delta;
    }

    public void reset(float x, float y) {
        position.set(x, y);

        animationType = PlayerAnimationType.IDLE;
        movementState = PlayerMovementState.GROUNDED;

        animationTime = 0f;
        facingRight = true;
        dead = false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public PlayerAnimationType getAnimationType() {
        return animationType;
    }

    public PlayerMovementState getMovementState() {
        return movementState;
    }

    public void setMovementState(PlayerMovementState movementState) {
        this.movementState = movementState;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }
}
