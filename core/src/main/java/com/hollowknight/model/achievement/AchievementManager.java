package com.hollowknight.model.achievement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AchievementManager {

    private static final String PREFERENCES_NAME =
        "hollow-knight-achievements";

    private final Map<AchievementType, Achievement> achievements;
    private final List<AchievementObserver> observers;
    private final Preferences preferences;

    public AchievementManager() {
        achievements = new EnumMap<>(AchievementType.class);
        observers = new ArrayList<>();

        preferences = Gdx.app.getPreferences(
            PREFERENCES_NAME
        );

        createAchievements();
        loadAchievements();
    }

    private void createAchievements() {
        register(new Achievement(
            AchievementType.COMPLETION,
            "achievements.completion.title",
            "achievements.completion.description"
        ));

        register(new Achievement(
            AchievementType.SPEEDRUN,
            "achievements.speedrun.title",
            "achievements.speedrun.description"
        ));

        register(new Achievement(
            AchievementType.TRUE_HUNTER,
            "achievements.trueHunter.title",
            "achievements.trueHunter.description"
        ));

        register(new Achievement(
            AchievementType.DEFEAT_FALSE_KNIGHT,
            "achievements.falseKnight.title",
            "achievements.falseKnight.description"
        ));

        register(new Achievement(
            AchievementType.SOUL_MASTER,
            "achievements.soulMaster.title",
            "achievements.soulMaster.description"
        ));
    }

    private void register(Achievement achievement) {
        achievements.put(
            achievement.getType(),
            achievement
        );
    }

    private void loadAchievements() {
        for (Achievement achievement : achievements.values()) {
            boolean unlocked = preferences.getBoolean(
                achievement.getType().name(),
                false
            );

            achievement.setUnlocked(unlocked);
        }
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(
            new ArrayList<>(achievements.values())
        );
    }

    public Achievement getAchievement(
        AchievementType type
    ) {
        return achievements.get(type);
    }

    public boolean unlock(AchievementType type) {
        Achievement achievement = achievements.get(type);

        if (
            achievement == null
                || achievement.isUnlocked()
        ) {
            return false;
        }

        achievement.setUnlocked(true);

        preferences.putBoolean(
            type.name(),
            true
        );

        preferences.flush();

        notifyObservers(achievement);

        return true;
    }

    public void addObserver(
        AchievementObserver observer
    ) {
        if (
            observer != null
                && !observers.contains(observer)
        ) {
            observers.add(observer);
        }
    }

    public void removeObserver(
        AchievementObserver observer
    ) {
        observers.remove(observer);
    }

    private void notifyObservers(
        Achievement achievement
    ) {
        List<AchievementObserver> observerCopy =
            new ArrayList<>(observers);

        for (AchievementObserver observer : observerCopy) {
            observer.onAchievementUnlocked(achievement);
        }
    }
}
