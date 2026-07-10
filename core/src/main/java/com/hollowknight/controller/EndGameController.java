package com.hollowknight.controller;

import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.EndGameStats;

public final class EndGameController {

    private final HollowKnightGame game;
    private final EndGameStats stats;

    public EndGameController(
        HollowKnightGame game,
        EndGameStats stats
    ) {
        this.game = game;
        this.stats = stats;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public EndGameStats getStats() {
        return stats;
    }

    public boolean shouldPlayVictoryMusic() {
        return game.getSettings().isMusicEnabled();
    }

    public float getVictoryMusicVolume() {
        return game.getSettings().getMusicVolume();
    }

    public String getFormattedTime() {
        int totalSeconds = (int) stats.getElapsedSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        );
    }

    public void restartGame() {
        game.showGameScreenForSlot(
            game.getActiveSaveSlot()
        );
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }
}
