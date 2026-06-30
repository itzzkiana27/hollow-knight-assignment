package com.hollowknight.model.input;

import com.badlogic.gdx.Input;

/**
 * Central definition of the gameplay keys.
 *
 * The five primary controls are stored in GameSettings and can be remapped
 * from the Settings screen. Directional attack modifiers and temporary test
 * keys remain fixed until their related gameplay systems are implemented.
 */
public final class KeyBindings {

    public static final int DEFAULT_MOVE_LEFT = Input.Keys.LEFT;
    public static final int DEFAULT_MOVE_RIGHT = Input.Keys.RIGHT;
    public static final int DEFAULT_JUMP = Input.Keys.Z;
    public static final int DEFAULT_DASH = Input.Keys.C;
    public static final int DEFAULT_ATTACK = Input.Keys.X;

    public static final int DEFAULT_FOCUS = Input.Keys.A;
    public static final int DEFAULT_UP = Input.Keys.UP;
    public static final int DEFAULT_DOWN = Input.Keys.DOWN;
    public static final int DEFAULT_ALTERNATE_ATTACK = Input.Keys.K;

    public static final int DEFAULT_HURT_TEST = Input.Keys.H;
    public static final int DEFAULT_SOUL_GAIN_TEST = Input.Keys.G;
    public static final int DEFAULT_FIREBALL_TEST = Input.Keys.E;
    public static final int DEFAULT_SCREAM_TEST = Input.Keys.R;
    public static final int DEFAULT_DEATH_TEST = Input.Keys.F10;
    public static final int DEFAULT_REVIVE = Input.Keys.ENTER;

    private final int moveLeft;
    private final int moveRight;
    private final int jump;
    private final int dash;
    private final int attack;

    private final int focus;
    private final int up;
    private final int down;
    private final int alternateAttack;

    private final int hurtTest;
    private final int soulGainTest;
    private final int fireballTest;
    private final int screamTest;
    private final int deathTest;
    private final int revive;

    public KeyBindings(
        int moveLeft,
        int moveRight,
        int jump,
        int dash,
        int attack
    ) {
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.jump = jump;
        this.dash = dash;
        this.attack = attack;

        focus = DEFAULT_FOCUS;
        up = DEFAULT_UP;
        down = DEFAULT_DOWN;
        alternateAttack = DEFAULT_ALTERNATE_ATTACK;

        hurtTest = DEFAULT_HURT_TEST;
        soulGainTest = DEFAULT_SOUL_GAIN_TEST;
        fireballTest = DEFAULT_FIREBALL_TEST;
        screamTest = DEFAULT_SCREAM_TEST;
        deathTest = DEFAULT_DEATH_TEST;
        revive = DEFAULT_REVIVE;
    }

    public int getMoveLeft() {
        return moveLeft;
    }

    public int getMoveRight() {
        return moveRight;
    }

    public int getJump() {
        return jump;
    }

    public int getDash() {
        return dash;
    }

    public int getAttack() {
        return attack;
    }

    public int getFocus() {
        return focus;
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public int getAlternateAttack() {
        return alternateAttack;
    }

    public int getHurtTest() {
        return hurtTest;
    }

    public int getSoulGainTest() {
        return soulGainTest;
    }

    public int getFireballTest() {
        return fireballTest;
    }

    public int getScreamTest() {
        return screamTest;
    }

    public int getDeathTest() {
        return deathTest;
    }

    public int getRevive() {
        return revive;
    }
}
