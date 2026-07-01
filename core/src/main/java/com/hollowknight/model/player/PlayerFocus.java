package com.hollowknight.model.player;

/**
 * Manages uninterrupted Focus healing.
 *
 * This class contains no LibGDX input code.
 * The controller provides the current conditions.
 */
public final class PlayerFocus {

    public enum UpdateResult {
        NONE,
        CANCELLED,
        HEALED
    }

    private static final float FOCUS_DURATION =
        1.5f;

    private float focusTime;
    private boolean active;

    public PlayerFocus() {
        focusTime = 0f;
        active = false;
    }

    public boolean tryStart(
        boolean onGround,
        boolean stationary,
        PlayerHealth health,
        PlayerSoul soul
    ) {
        if (active) {
            return false;
        }

        if (!onGround || !stationary) {
            return false;
        }

        if (health.isFullHealth()) {
            return false;
        }

        if (
            !soul.canSpend(
                PlayerSoul.FOCUS_COST
            )
        ) {
            return false;
        }

        focusTime = 0f;
        active = true;

        return true;
    }

    public UpdateResult update(
        float delta,
        boolean focusHeld,
        boolean cancelRequested,
        PlayerHealth health,
        PlayerSoul soul
    ) {
        if (!active) {
            return UpdateResult.NONE;
        }

        if (
            !focusHeld
                || cancelRequested
                || health.isFullHealth()
                || !soul.canSpend(
                PlayerSoul.FOCUS_COST
            )
        ) {
            cancel();

            return UpdateResult.CANCELLED;
        }

        focusTime += delta;

        if (focusTime < FOCUS_DURATION) {
            return UpdateResult.NONE;
        }

        /*
         * Recheck both conditions before modifying
         * either resource.
         */
        if (
            health.isFullHealth()
                || !soul.canSpend(
                PlayerSoul.FOCUS_COST
            )
        ) {
            cancel();

            return UpdateResult.CANCELLED;
        }

        soul.spend(
            PlayerSoul.FOCUS_COST
        );

        health.heal(1);

        focusTime = 0f;
        active = false;

        return UpdateResult.HEALED;
    }

    public void cancel() {
        focusTime = 0f;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public float getProgress() {
        if (!active) {
            return 0f;
        }

        return Math.min(
            1f,
            focusTime / FOCUS_DURATION
        );
    }
}
