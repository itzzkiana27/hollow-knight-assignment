package com.hollowknight.model.input;

/**
 * A frame-by-frame snapshot of player input.
 *
 * Keeping LibGDX input access out of the movement model makes the movement
 * rules easier to test and keeps the controller responsible for translating
 * keyboard input into model data.
 */
public final class PlayerInput {

    private final boolean moveLeftHeld;
    private final boolean moveRightHeld;

    private final boolean jumpPressed;
    private final boolean dashPressed;
    private final boolean attackPressed;
    private final boolean alternateAttackPressed;

    private final boolean upHeld;
    private final boolean downHeld;

    private final boolean focusHeld;
    private final boolean focusPressed;

    private final boolean hurtPressed;
    private final boolean soulGainPressed;
    private final boolean fireballPressed;
    private final boolean screamPressed;
    private final boolean deathPressed;
    private final boolean revivePressed;

    public PlayerInput(
        boolean moveLeftHeld,
        boolean moveRightHeld,
        boolean jumpPressed,
        boolean dashPressed,
        boolean attackPressed,
        boolean alternateAttackPressed,
        boolean upHeld,
        boolean downHeld,
        boolean focusHeld,
        boolean focusPressed,
        boolean hurtPressed,
        boolean soulGainPressed,
        boolean fireballPressed,
        boolean screamPressed,
        boolean deathPressed,
        boolean revivePressed
    ) {
        this.moveLeftHeld = moveLeftHeld;
        this.moveRightHeld = moveRightHeld;
        this.jumpPressed = jumpPressed;
        this.dashPressed = dashPressed;
        this.attackPressed = attackPressed;
        this.alternateAttackPressed = alternateAttackPressed;
        this.upHeld = upHeld;
        this.downHeld = downHeld;
        this.focusHeld = focusHeld;
        this.focusPressed = focusPressed;
        this.hurtPressed = hurtPressed;
        this.soulGainPressed = soulGainPressed;
        this.fireballPressed = fireballPressed;
        this.screamPressed = screamPressed;
        this.deathPressed = deathPressed;
        this.revivePressed = revivePressed;
    }

    public static PlayerInput empty() {
        return new PlayerInput(
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );
    }

    public boolean isMoveLeftHeld() {
        return moveLeftHeld;
    }

    public boolean isMoveRightHeld() {
        return moveRightHeld;
    }

    public int getHorizontalDirection() {
        int direction = 0;

        if (moveLeftHeld) {
            direction--;
        }

        if (moveRightHeld) {
            direction++;
        }

        return direction;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    public boolean isDashPressed() {
        return dashPressed;
    }

    public boolean isAttackPressed() {
        return attackPressed;
    }

    public boolean isAlternateAttackPressed() {
        return alternateAttackPressed;
    }

    public boolean isUpHeld() {
        return upHeld;
    }

    public boolean isDownHeld() {
        return downHeld;
    }

    public boolean isFocusHeld() {
        return focusHeld;
    }

    public boolean isFocusPressed() {
        return focusPressed;
    }

    public boolean isHurtPressed() {
        return hurtPressed;
    }

    public boolean isSoulGainPressed() {
        return soulGainPressed;
    }

    public boolean isFireballPressed() {
        return fireballPressed;
    }

    public boolean isScreamPressed() {
        return screamPressed;
    }

    public boolean isDeathPressed() {
        return deathPressed;
    }

    public boolean isRevivePressed() {
        return revivePressed;
    }
}
