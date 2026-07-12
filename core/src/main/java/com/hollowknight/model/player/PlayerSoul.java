package com.hollowknight.model.player;

public final class PlayerSoul {

    public static final int MAX_SOUL = 99;
    public static final int NAIL_HIT_GAIN = 11;
    public static final int FOCUS_COST = 33;

    private int currentSoul;

    public PlayerSoul() {
        currentSoul = 0;
    }

    public void gainFromNailHit() {
        gain(NAIL_HIT_GAIN);
    }

    public void gain(int amount) {
        if (amount <= 0) {
            return;
        }

        currentSoul = Math.min(MAX_SOUL, currentSoul + amount);
    }

    public boolean canSpend(int amount) {
        return amount >= 0 && currentSoul >= amount;
    }

    public boolean spend(int amount) {
        if (!canSpend(amount)) {
            return false;
        }

        currentSoul -= amount;
        return true;
    }

    public void refill() {
        currentSoul = MAX_SOUL;
    }

    public void setCurrentSoul(int soul) {
        currentSoul = Math.max(0, Math.min(MAX_SOUL, soul));
    }

    public int getCurrentSoul() {
        return currentSoul;
    }

    public int getMaximumSoul() {
        return MAX_SOUL;
    }

    public float getFillRatio() {
        return (float) currentSoul / MAX_SOUL;
    }
}
