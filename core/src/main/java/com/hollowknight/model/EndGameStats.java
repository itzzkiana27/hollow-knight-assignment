package com.hollowknight.model;

public record EndGameStats(int deathCount, int totalEnemiesKilled, float elapsedSeconds) {

    public EndGameStats(int deathCount, int totalEnemiesKilled, float elapsedSeconds) {
        this.deathCount = Math.max(0, deathCount);
        this.totalEnemiesKilled = Math.max(0, totalEnemiesKilled);
        this.elapsedSeconds = Math.max(0f, elapsedSeconds);
    }
}
