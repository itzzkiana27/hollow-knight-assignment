package com.hollowknight.model.player;

/**
 * Manages the Knight's masks and temporary
 * invincibility after receiving damage.
 */
public final class PlayerHealth {

    public enum DamageResult {
        IGNORED,
        DAMAGED,
        DEFEATED
    }

    private static final int MAX_MASKS = 5;

    private static final float
        INVINCIBILITY_DURATION = 1f;

    private static final float
        BLINKS_PER_SECOND = 10f;

    private int currentMasks;

    private float
        invincibilityTimeRemaining;

    public PlayerHealth() {
        currentMasks = MAX_MASKS;
        invincibilityTimeRemaining = 0f;
    }

    public void update(float delta) {
        if (
            invincibilityTimeRemaining <= 0f
        ) {
            return;
        }

        invincibilityTimeRemaining -= delta;

        if (
            invincibilityTimeRemaining < 0f
        ) {
            invincibilityTimeRemaining = 0f;
        }
    }

    public DamageResult takeDamage(
        int damage
    ) {
        if (
            damage <= 0
                || isInvincible()
        ) {
            return DamageResult.IGNORED;
        }

        currentMasks = Math.max(
            0,
            currentMasks - damage
        );

        invincibilityTimeRemaining =
            INVINCIBILITY_DURATION;

        if (currentMasks == 0) {
            return DamageResult.DEFEATED;
        }

        return DamageResult.DAMAGED;
    }

    public int heal(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int masksBeforeHealing =
            currentMasks;

        currentMasks = Math.min(
            MAX_MASKS,
            currentMasks + amount
        );

        return currentMasks
            - masksBeforeHealing;
    }

    public boolean isFullHealth() {
        return currentMasks >= MAX_MASKS;
    }

    public void restoreFullHealth() {
        currentMasks = MAX_MASKS;
    }

    public void setCurrentMasks(
        int masks
    ) {
        currentMasks = Math.max(
            0,
            Math.min(MAX_MASKS, masks)
        );
    }

    public boolean isInvincible() {
        return
            invincibilityTimeRemaining > 0f;
    }

    public boolean shouldDrawPlayer() {
        if (!isInvincible()) {
            return true;
        }

        int blinkFrame = (int) (
            invincibilityTimeRemaining
                * BLINKS_PER_SECOND
                * 2f
        );

        return blinkFrame % 2 == 0;
    }

    public int getCurrentMasks() {
        return currentMasks;
    }

    public int getMaximumMasks() {
        return MAX_MASKS;
    }

    public float
    getInvincibilityTimeRemaining() {
        return invincibilityTimeRemaining;
    }
}
