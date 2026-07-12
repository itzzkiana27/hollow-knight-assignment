package com.hollowknight.model.charm;

public enum CharmType {
    SOUL_CATCHER(
        "charm.soulCatcher.name",
        "charm.soulCatcher.description",
        "sprites/ui/charms/soul_catcher.png",
        1
    ),

    DASHMASTER(
        "charm.dashmaster.name",
        "charm.dashmaster.description",
        "sprites/ui/charms/dashmaster.png",
        1
    ),

    UNBREAKABLE_STRENGTH(
        "charm.unbreakableStrength.name",
        "charm.unbreakableStrength.description",
        "sprites/ui/charms/unbreakable_strength.png",
        1
    ),

    QUICK_SLASH(
        "charm.quickSlash.name",
        "charm.quickSlash.description",
        "sprites/ui/charms/quick_slash.png",
        1
    ),

    QUICK_FOCUS(
        "charm.quickFocus.name",
        "charm.quickFocus.description",
        "sprites/ui/charms/quick_focus.png",
        1
    ),

    HEAVY_BLOW(
        "charm.heavyBlow.name",
        "charm.heavyBlow.description",
        "sprites/ui/charms/heavy_blow.png",
        1
    ),

    SHARP_SHADOW(
        "charm.sharpShadow.name",
        "charm.sharpShadow.description",
        "sprites/ui/charms/sharp_shadow.png",
        1
    ),

    VOID_HEART(
        "charm.voidHeart.name",
        "charm.voidHeart.description",
        "sprites/ui/charms/void_heart.png",
        1
    );

    private final String nameKey;
    private final String descriptionKey;
    private final String iconPath;
    private final int notchCost;

    CharmType(
        String nameKey,
        String descriptionKey,
        String iconPath,
        int notchCost
    ) {
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.iconPath = iconPath;
        this.notchCost = notchCost;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getIconPath() {
        return iconPath;
    }

    public int getNotchCost() {
        return notchCost;
    }
}
