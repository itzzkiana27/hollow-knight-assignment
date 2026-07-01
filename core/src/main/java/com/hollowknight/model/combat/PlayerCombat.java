package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.model.player.PlayerBody;

public final class PlayerCombat {

    private static final int NAIL_DAMAGE = 1;

    private static final float
        ACTIVE_START_TIME = 0.055f;

    private static final float
        ACTIVE_END_TIME = 0.220f;

    private final Rectangle attackHitbox;

    private AttackDirection attackDirection;

    private float attackTime;

    private boolean attacking;
    private boolean hitRegistered;

    public PlayerCombat() {
        attackHitbox = new Rectangle();

        attackDirection =
            AttackDirection.RIGHT;

        attackTime = 0f;

        attacking = false;
        hitRegistered = false;
    }

    public boolean tryStartAttack(
        PlayerInput input,
        Player player
    ) {
        if (attacking) {
            return false;
        }

        if (
            !input.isAttackPressed()
                && !input
                .isAlternateAttackPressed()
        ) {
            return false;
        }

        if (input.isUpHeld()) {
            attackDirection =
                AttackDirection.UP;

            player.setAnimation(
                PlayerAnimationType.UP_SLASH
            );
        } else if (input.isDownHeld()) {
            attackDirection =
                AttackDirection.DOWN;

            player.setAnimation(
                PlayerAnimationType.DOWN_SLASH
            );
        } else {
            attackDirection =
                player.isFacingRight()
                    ? AttackDirection.RIGHT
                    : AttackDirection.LEFT;

            if (
                input.isAlternateAttackPressed()
            ) {
                player.setAnimation(
                    PlayerAnimationType.SLASH_ALT
                );
            } else {
                player.setAnimation(
                    PlayerAnimationType.SLASH
                );
            }
        }

        attackTime = 0f;
        attacking = true;
        hitRegistered = false;

        attackHitbox.set(
            0f,
            0f,
            0f,
            0f
        );

        return true;
    }

    public void update(
        float delta,
        PlayerBody playerBody
    ) {
        if (!attacking) {
            return;
        }

        attackTime += delta;

        updateHitbox(
            playerBody.getBounds()
        );
    }

    private void updateHitbox(
        Rectangle body
    ) {
        float bodyCenterX =
            body.x + body.width / 2f;

        float horizontalReach =
            body.width * 1.28f;

        float horizontalHeight =
            body.height * 0.43f;

        float verticalWidth =
            body.width * 1.04f;

        float verticalReach =
            body.height * 0.74f;

        switch (attackDirection) {
            case RIGHT -> attackHitbox.set(
                body.x + body.width - 4f,
                body.y
                    + body.height * 0.31f,
                horizontalReach,
                horizontalHeight
            );

            case LEFT -> attackHitbox.set(
                body.x
                    - horizontalReach
                    + 4f,
                body.y
                    + body.height * 0.31f,
                horizontalReach,
                horizontalHeight
            );

            case UP -> attackHitbox.set(
                bodyCenterX
                    - verticalWidth / 2f,
                body.y + body.height - 4f,
                verticalWidth,
                verticalReach
            );

            case DOWN -> attackHitbox.set(
                bodyCenterX
                    - verticalWidth / 2f,
                body.y
                    - verticalReach
                    + 8f,
                verticalWidth,
                verticalReach
            );
        }
    }

    public boolean isHitboxActive() {
        return attacking
            && attackTime
            >= ACTIVE_START_TIME
            && attackTime
            <= ACTIVE_END_TIME;
    }

    public boolean canRegisterHit() {
        return isHitboxActive()
            && !hitRegistered;
    }

    public void registerHit() {
        hitRegistered = true;
    }

    public void finishAttack() {
        attacking = false;
        attackTime = 0f;
        hitRegistered = false;

        attackHitbox.set(
            0f,
            0f,
            0f,
            0f
        );
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean isDownwardAttack() {
        return attackDirection
            == AttackDirection.DOWN;
    }

    public int getDamage() {
        return NAIL_DAMAGE;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }
}
