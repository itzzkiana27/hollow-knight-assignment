package com.hollowknight.model.input;

import com.badlogic.gdx.Input;

/**
 * Central definition of gameplay keys. Normal controls live in GameSettings and can be remapped;
 * debug controls intentionally stay fixed.
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
    public static final int DEFAULT_FIREBALL = Input.Keys.E;
    public static final int DEFAULT_SCREAM = Input.Keys.R;
    public static final int DEFAULT_INTERACT = Input.Keys.E;
    public static final int DEFAULT_DIALOGUE_ADVANCE = Input.Keys.ENTER;
    public static final int DEFAULT_INVENTORY = Input.Keys.I;
    public static final int DEFAULT_PAUSE = Input.Keys.ESCAPE;

    public static final int DEFAULT_FIREBALL_TEST = DEFAULT_FIREBALL;
    public static final int DEFAULT_SCREAM_TEST = DEFAULT_SCREAM;

    public static final int DEFAULT_HURT_TEST = Input.Keys.H;
    public static final int DEFAULT_SOUL_GAIN_TEST = Input.Keys.G;
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
    private final int fireball;
    private final int scream;
    private final int interact;
    private final int dialogueAdvance;
    private final int inventory;
    private final int pause;

    private final int hurtTest;
    private final int soulGainTest;
    private final int deathTest;
    private final int revive;

    public KeyBindings(
            int moveLeft,
            int moveRight,
            int jump,
            int dash,
            int attack,
            int up,
            int down,
            int alternateAttack,
            int focus,
            int fireball,
            int scream,
            int interact,
            int dialogueAdvance,
            int inventory,
            int pause) {
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.jump = jump;
        this.dash = dash;
        this.attack = attack;
        this.up = up;
        this.down = down;
        this.alternateAttack = alternateAttack;
        this.focus = focus;
        this.fireball = fireball;
        this.scream = scream;
        this.interact = interact;
        this.dialogueAdvance = dialogueAdvance;
        this.inventory = inventory;
        this.pause = pause;

        hurtTest = DEFAULT_HURT_TEST;
        soulGainTest = DEFAULT_SOUL_GAIN_TEST;
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

    public int getFireball() {
        return fireball;
    }

    public int getScream() {
        return scream;
    }

    public int getInteract() {
        return interact;
    }

    public int getDialogueAdvance() {
        return dialogueAdvance;
    }

    public int getInventory() {
        return inventory;
    }

    public int getPause() {
        return pause;
    }

    public int getHurtTest() {
        return hurtTest;
    }

    public int getSoulGainTest() {
        return soulGainTest;
    }

    public int getFireballTest() {
        return fireball;
    }

    public int getScreamTest() {
        return scream;
    }

    public int getDeathTest() {
        return deathTest;
    }

    public int getRevive() {
        return revive;
    }
}
