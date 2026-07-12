package com.hollowknight.controller;

import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.EndGameStats;

public final class EndGameController {

    private final HollowKnightGame game;

    private final EndGameStats stats;

    public EndGameController(HollowKnightGame game, EndGameStats stats) {
        this.game = game;
        this.stats = stats;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public String format(String key, Object... arguments) {
        return game.getLocalization().format(key, arguments);
    }

    public boolean shouldPlayVictoryMusic() {
        return game.getSettings().isMusicEnabled();
    }

    public String getFormattedTime() {
        int totalSeconds = (int) stats.elapsedSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void restartGame() {
        game.showGameScreenForSlot(game.getActiveSaveSlot());
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }

    public EndGameStats getStats() {
        return stats;
    }

    public float getVictoryMusicVolume() {
        return game.getSettings().getMusicVolume();
    }
}
