package com.hollowknight.view.theme;

public enum MenuThemeType {
    CLASSIC_HOLLOW(
        "classic_hollow",
        "settings.theme.classicHollow"
    ),
    VOIDHEART(
        "voidheart",
        "settings.theme.voidheart"
    ),
    ROYAL_GOLD(
        "royal_gold",
        "settings.theme.royalGold"
    );

    private final String id;
    private final String localizationKey;

    MenuThemeType(
        String id,
        String localizationKey
    ) {
        this.id = id;
        this.localizationKey = localizationKey;
    }

    public String getId() {
        return id;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    public MenuThemeType next() {
        MenuThemeType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static MenuThemeType fromId(String id) {
        if (id == null) {
            return VOIDHEART;
        }

        for (MenuThemeType theme : values()) {
            if (theme.id.equals(id)) {
                return theme;
            }
        }

        return VOIDHEART;
    }

}
