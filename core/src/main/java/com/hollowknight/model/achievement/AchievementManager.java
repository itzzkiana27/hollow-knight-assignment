package com.hollowknight.model.achievement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.*;

public class AchievementManager {

    private static final String PREFERENCES_NAME = "hollow-knight-achievements";

    private final Map<AchievementType, Achievement> achievements;

    private final List<AchievementObserver> observers;

    private final Preferences preferences;

    public AchievementManager() {
        achievements = new EnumMap<>(AchievementType.class);
        observers = new ArrayList<>();

        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);

        createAchievements();
        loadAchievements();
    }

    public boolean unlock(AchievementType type) {
        Achievement achievement = achievements.get(type);

        if (achievement == null || achievement.isUnlocked()) {
            return false;
        }

        achievement.setUnlocked(true);

        preferences.putBoolean(type.name(), true);

        preferences.flush();

        notifyObservers(achievement);

        return true;
    }

    public boolean isUnlocked(AchievementType type) {
        Achievement achievement = achievements.get(type);

        return achievement != null && achievement.isUnlocked();
    }

    public List<String> getUnlockedTypeNames() {
        List<String> unlockedNames = new ArrayList<>();

        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlockedNames.add(achievement.getType().name());
            }
        }

        return unlockedNames;
    }

    public void applyUnlockedTypeNames(List<String> unlockedTypeNames) {
        if (unlockedTypeNames == null) {
            return;
        }

        for (String typeName : unlockedTypeNames) {
            try {
                unlock(AchievementType.valueOf(typeName));
            } catch (IllegalArgumentException ignored) {

            }
        }
    }

    public void addObserver(AchievementObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(AchievementObserver observer) {
        observers.remove(observer);
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(new ArrayList<>(achievements.values()));
    }

    public Achievement getAchievement(AchievementType type) {
        return achievements.get(type);
    }

    private void createAchievements() {
        register(
                new Achievement(
                        AchievementType.COMPLETION,
                        "achievements.completion.title",
                        "achievements.completion.description"));

        register(
                new Achievement(
                        AchievementType.SPEEDRUN,
                        "achievements.speedrun.title",
                        "achievements.speedrun.description"));

        register(
                new Achievement(
                        AchievementType.TRUE_HUNTER,
                        "achievements.trueHunter.title",
                        "achievements.trueHunter.description"));

        register(
                new Achievement(
                        AchievementType.DEFEAT_FALSE_KNIGHT,
                        "achievements.falseKnight.title",
                        "achievements.falseKnight.description"));

        register(
                new Achievement(
                        AchievementType.KILL_TWO_ENEMIES_10_SECONDS,
                        "achievements.killTwo.title",
                        "achievements.killTwo.description"));
    }

    private void register(Achievement achievement) {
        achievements.put(achievement.getType(), achievement);
    }

    private void loadAchievements() {
        for (Achievement achievement : achievements.values()) {
            boolean unlocked = preferences.getBoolean(achievement.getType().name(), false);

            achievement.setUnlocked(unlocked);
        }
    }

    private void notifyObservers(Achievement achievement) {
        List<AchievementObserver> observerCopy = new ArrayList<>(observers);

        for (AchievementObserver observer : observerCopy) {
            observer.onAchievementUnlocked(achievement);
        }
    }
}
