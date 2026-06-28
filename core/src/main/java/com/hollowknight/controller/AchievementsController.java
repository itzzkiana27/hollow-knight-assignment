package com.hollowknight.controller;

import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.model.achievement.AchievementManager;
import com.hollowknight.model.achievement.AchievementObserver;

import java.util.List;

public class AchievementsController {

    private final HollowKnightGame game;
    private final AchievementManager achievementManager;

    public AchievementsController(
        HollowKnightGame game
    ) {
        this.game = game;

        achievementManager =
            game.getAchievementManager();
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public List<Achievement> getAchievements() {
        return achievementManager.getAchievements();
    }

    public void addObserver(
        AchievementObserver observer
    ) {
        achievementManager.addObserver(observer);
    }

    public void removeObserver(
        AchievementObserver observer
    ) {
        achievementManager.removeObserver(observer);
    }

    public void backToMainMenu() {
        game.showMainMenu();
    }
}
