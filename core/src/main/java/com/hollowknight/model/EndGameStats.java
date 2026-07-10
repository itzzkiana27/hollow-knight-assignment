package com.hollowknight.model;

public final class EndGameStats {

    private final int deathCount;
    private final int totalEnemiesKilled;
    private final float elapsedSeconds;

    public EndGameStats(
        int deathCount,
        int totalEnemiesKilled,
        float elapsedSeconds
    ) {
        this.deathCount = Math.max(0, deathCount);
        this.totalEnemiesKilled =
            Math.max(0, totalEnemiesKilled);
        this.elapsedSeconds =
            Math.max(0f, elapsedSeconds);
    }

    public int getDeathCount() {
        return deathCount;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }
}
