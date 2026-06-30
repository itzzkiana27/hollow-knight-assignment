package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;

public final class PlayerCombat {

    /*
     * The nail only causes damage during this part
     * of the slash animation.
     */
    private static final float ACTIVE_START_TIME = 0.055f;
    private static final float ACTIVE_END_TIME = 0.220f;

    /*
     * Approximate location of the Knight's body
     * inside the supplied 349 × 186 animation frames.
     */
    private static final float BODY_X_RATIO =
        131f / 349f;

    private static final float BODY_Y_RATIO =
        4f / 186f;

    private static final float BODY_WIDTH_RATIO =
        74f / 349f;

    private static final float BODY_HEIGHT_RATIO =
        120f / 186f;

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
                && !input.isAlternateAttackPressed()
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

            if (input.isAlternateAttackPressed()) {
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
        Player player,
        float playerDrawWidth,
        float playerDrawHeight
    ) {
        if (!attacking) {
            return;
        }

        attackTime += delta;

        updateHitbox(
            player,
            playerDrawWidth,
            playerDrawHeight
        );
    }

    private void updateHitbox(
        Player player,
        float drawWidth,
        float drawHeight
    ) {
        float bodyX =
            player.getPosition().x
                + drawWidth * BODY_X_RATIO;

        float bodyY =
            player.getPosition().y
                + drawHeight * BODY_Y_RATIO;

        float bodyWidth =
            drawWidth * BODY_WIDTH_RATIO;

        float bodyHeight =
            drawHeight * BODY_HEIGHT_RATIO;

        float bodyCenterX =
            bodyX + bodyWidth / 2f;

        float horizontalReach =
            drawWidth * 0.27f;

        float horizontalHeight =
            drawHeight * 0.34f;

        float verticalWidth =
            drawWidth * 0.22f;

        float verticalReach =
            drawHeight * 0.48f;

        switch (attackDirection) {
            case RIGHT -> attackHitbox.set(
                bodyX + bodyWidth - 4f,
                bodyY + bodyHeight * 0.31f,
                horizontalReach,
                horizontalHeight
            );

            case LEFT -> attackHitbox.set(
                bodyX - horizontalReach + 4f,
                bodyY + bodyHeight * 0.31f,
                horizontalReach,
                horizontalHeight
            );

            case UP -> attackHitbox.set(
                bodyCenterX - verticalWidth / 2f,
                bodyY + bodyHeight - 4f,
                verticalWidth,
                verticalReach
            );

            case DOWN -> attackHitbox.set(
                bodyCenterX - verticalWidth / 2f,
                bodyY - verticalReach + 8f,
                verticalWidth,
                verticalReach
            );
        }
    }

    public boolean isHitboxActive() {
        return attacking
            && attackTime >= ACTIVE_START_TIME
            && attackTime <= ACTIVE_END_TIME;
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

    public AttackDirection getAttackDirection() {
        return attackDirection;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }
}
