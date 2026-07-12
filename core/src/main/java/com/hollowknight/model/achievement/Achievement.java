package com.hollowknight.model.achievement;

public class Achievement {

    private final AchievementType type;
    private final String titleKey;
    private final String descriptionKey;

    private boolean unlocked;

    public Achievement(AchievementType type, String titleKey, String descriptionKey) {
        this.type = type;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
    }

    public AchievementType getType() {
        return type;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
