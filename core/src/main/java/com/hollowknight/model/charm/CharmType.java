package com.hollowknight.model.charm;

public enum CharmType {
    SOUL_CATCHER(
        "Soul Catcher",
        "Gain more Soul from each successful Nail hit.",
        "sprites/ui/charms/soul_catcher.png",
        1
    ),

    DASHMASTER(
        "Dashmaster",
        "Dash more often by reducing dash cooldown.",
        "sprites/ui/charms/dashmaster.png",
        1
    ),

    UNBREAKABLE_STRENGTH(
        "Unbreakable Strength",
        "Increase normal Nail damage.",
        "sprites/ui/charms/unbreakable_strength.png",
        1
    ),

    QUICK_SLASH(
        "Quick Slash",
        "Attack faster by reducing Nail cooldown.",
        "sprites/ui/charms/quick_slash.png",
        1
    ),

    QUICK_FOCUS(
        "Quick Focus",
        "Focus faster when healing.",
        "sprites/ui/charms/quick_focus.png",
        1
    ),

    HEAVY_BLOW(
        "Heavy Blow",
        "Increase enemy knockback distance.",
        "sprites/ui/charms/heavy_blow.png",
        1
    ),

    SHARP_SHADOW(
        "Sharp Shadow",
        "Dash through enemies, damage them, avoid contact damage, and increase dash length.",
        "sprites/ui/charms/sharp_shadow.png",
        1
    ),

    VOID_HEART(
        "Void Heart",
        "Increase ability damage and use upgraded black ability animations.",
        "sprites/ui/charms/void_heart.png",
        1
    );

    private final String displayName;
    private final String description;
    private final String iconPath;
    private final int notchCost;

    CharmType(
        String displayName,
        String description,
        String iconPath,
        int notchCost
    ) {
        this.displayName = displayName;
        this.description = description;
        this.iconPath = iconPath;
        this.notchCost = notchCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }

    public int getNotchCost() {
        return notchCost;
    }
}
