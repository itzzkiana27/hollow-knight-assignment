package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

public final class SpikeHazard implements Pogoable {

    private static final float FLASH_DURATION = 0.10f;

    private final String id;

    private final Rectangle bounds;

    private final int damage;

    private final boolean pogoable;

    private float flashTimeRemaining;

    private int pogoCount;

    public SpikeHazard(String id, Rectangle bounds, int damage, boolean pogoable) {
        this.id = id;
        this.bounds = new Rectangle(bounds);
        this.damage = Math.max(0, damage);
        this.pogoable = pogoable;

        flashTimeRemaining = 0f;
        pogoCount = 0;
    }

    public void update(float delta) {
        if (flashTimeRemaining <= 0f) {
            return;
        }

        flashTimeRemaining -= Math.max(delta, 0f);

        if (flashTimeRemaining < 0f) {
            flashTimeRemaining = 0f;
        }
    }

    @Override
    public boolean canBePogoed() {
        return pogoable;
    }

    @Override
    public void onPogo() {
        if (!pogoable) {
            return;
        }

        pogoCount++;
        flashTimeRemaining = FLASH_DURATION;
    }

    @Override
    public Rectangle getPogoBounds() {
        return bounds;
    }

    public String getId() {
        return id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isFlashing() {
        return flashTimeRemaining > 0f;
    }

    public int getPogoCount() {
        return pogoCount;
    }
}
