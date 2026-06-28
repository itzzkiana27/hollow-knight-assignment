package com.hollowknight.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

public class LocalizationManager {

    private static final String BUNDLE_PATH = "i18n/messages";

    private I18NBundle bundle;
    private String language;

    public LocalizationManager(String language) {
        setLanguage(language);
    }

    public void setLanguage(String language) {
        if ("fr".equals(language)) {
            this.language = "fr";
        } else {
            this.language = "en";
        }

        Locale locale = Locale.forLanguageTag(this.language);

        bundle = I18NBundle.createBundle(
            Gdx.files.internal(BUNDLE_PATH),
            locale
        );
    }

    public String get(String key) {
        return bundle.get(key);
    }

    public String format(String key, Object... arguments) {
        return bundle.format(key, arguments);
    }

    public String getLanguage() {
        return language;
    }
}
