package com.hollowknight.model.input;

public record PlayerInput(
        boolean moveLeftHeld,
        boolean moveRightHeld,
        boolean jumpPressed,
        boolean jumpHeld,
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
        boolean revivePressed) {

    public static PlayerInput empty() {
        return new PlayerInput(
                false, false, false, false, false, false, false, false, false, false, false, false,
                false, false, false, false, false);
    }

    public int getHorizontalDirection() {
        if (moveLeftHeld == moveRightHeld) {
            return 0;
        }

        return moveRightHeld ? 1 : -1;
    }
}
