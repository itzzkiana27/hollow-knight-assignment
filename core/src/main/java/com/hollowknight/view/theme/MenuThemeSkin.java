package com.hollowknight.view.theme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class MenuThemeSkin implements Disposable {

    private static final String PREFERENCES_NAME = "hollow-knight-settings";

    private static final String THEME_KEY = "menuTheme";

    private static final String BASE_PATH = "ui/menu/";

    private static final String FLEUR_PATH = "ui/fleurs/";

    private static final String TRAJAN_FONT_PATH = "ui/fonts/TrajanPro-Regular.ttf";

    private static final String SCROLLBAR_PATH = "ui/scrollbar/";

    private static final int MENU_BODY_FONT_SIZE = 24;

    private static final int MENU_TITLE_FONT_SIZE = 34;

    private static final int MENU_SMALL_FONT_SIZE = 20;

    private static Cursor sharedCursor;

    private static boolean cursorLoaded;

    private final MenuThemeType theme;

    private final Skin skin;

    private final SpriteBatch backgroundBatch;

    private final Array<Texture> ownedTextures;

    private final Texture backgroundTexture;

    private final Texture saveBackgroundTexture;

    private final Texture titleLogoTexture;

    private final Texture titleOrnamentTexture;

    private final Texture borderTexture;

    private final Texture controllerPromptTexture;

    private final Texture mainBeamTexture;

    private final Texture voidBeamTexture;

    private final Texture soulOrbTexture;

    private final Texture soulGlowTexture;

    private final Texture magicOrbTexture;

    private final Texture healthMaskTexture;

    private final Texture vengefulSpiritTexture;

    private final Texture shadeSoulTexture;

    private final Texture howlingWraithsTexture;

    private final Texture abyssShriekTexture;

    private final Texture slotForgottenCrossroadsTexture;

    private final Texture slotCityOfTearsTexture;

    private final Texture slotAbyssTexture;

    private final Texture slotWhitePalaceTexture;

    private final Texture slotDirtmouthTexture;

    private final Texture verticalScrollTrackTexture;

    private final Texture verticalScrollKnobTexture;

    private final Texture menuHeaderFleurTexture;

    private final Texture menuFooterFleurTexture;

    private final Texture pauseHeaderFleurTexture;

    private final Texture dialogueDividerTexture;

    private float particleTime;

    private MenuThemeSkin(MenuThemeType theme) {
        this.theme = theme;
        this.ownedTextures = new Array<>();
        this.backgroundBatch = new SpriteBatch();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        backgroundTexture = load(BASE_PATH + "backgrounds/voidheart_menu_bg.png");

        saveBackgroundTexture = load(BASE_PATH + "backgrounds/save_background.png");

        titleLogoTexture = load(BASE_PATH + "common/vheart_title.png");

        titleOrnamentTexture = load(BASE_PATH + "common/title_ornament_large.png");

        borderTexture = load(BASE_PATH + "common/menu_border_black.png");

        controllerPromptTexture = load(BASE_PATH + "common/controller_prompt_bg.png");

        mainBeamTexture = load(BASE_PATH + "effects/main_menu_beam.png");

        voidBeamTexture = load(BASE_PATH + "effects/vheart_beam.png");

        soulOrbTexture = load(BASE_PATH + "icons/soul_orb_full.png");

        soulGlowTexture = load(BASE_PATH + "icons/soul_orb_glow.png");

        magicOrbTexture = load(BASE_PATH + "icons/magic_orb_small.png");

        healthMaskTexture = load(BASE_PATH + "icons/health_mask.png");

        vengefulSpiritTexture = load(BASE_PATH + "icons/spell_vengeful_spirit.png");

        shadeSoulTexture = load(BASE_PATH + "icons/spell_shade_soul.png");

        howlingWraithsTexture = load(BASE_PATH + "icons/spell_howling_wraiths.png");

        abyssShriekTexture = load(BASE_PATH + "icons/spell_abyss_shriek.png");

        slotForgottenCrossroadsTexture = load(BASE_PATH + "slots/area_forgotten_crossroads.png");

        slotCityOfTearsTexture = load(BASE_PATH + "slots/area_city_of_tears.png");

        slotAbyssTexture = load(BASE_PATH + "slots/area_abyss.png");

        slotWhitePalaceTexture = load(BASE_PATH + "slots/area_white_palace.png");

        slotDirtmouthTexture = load(BASE_PATH + "slots/area_dirtmouth.png");

        menuHeaderFleurTexture = load(FLEUR_PATH + "menu_header_fleur.png");

        menuFooterFleurTexture = load(FLEUR_PATH + "menu_footer_fleur.png");

        pauseHeaderFleurTexture = load(FLEUR_PATH + "pause_header_fleur.png");

        dialogueDividerTexture = load(FLEUR_PATH + "dialogue_divider.png");

        verticalScrollTrackTexture = load(SCROLLBAR_PATH + "vertical_scroll_track.png");

        verticalScrollKnobTexture = load(SCROLLBAR_PATH + "vertical_scroll_knob.png");

        customizeSkin();
        applyCustomCursor();
    }

    public static MenuThemeSkin fromSettings() {
        String id =
                Gdx.app
                        .getPreferences(PREFERENCES_NAME)
                        .getString(THEME_KEY, MenuThemeType.VOIDHEART.getId());

        return new MenuThemeSkin(MenuThemeType.fromId(id));
    }

    public static MenuThemeSkin fromThemeId(String themeId) {
        return new MenuThemeSkin(MenuThemeType.fromId(themeId));
    }

    public Color titleColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.95f, 0.70f, 0.22f, 1f);

            case CLASSIC_HOLLOW:
                return new Color(0.88f, 0.96f, 1f, 1f);

            case VOIDHEART:
            default:
                return new Color(0.92f, 0.94f, 0.98f, 1f);
        }
    }

    public Color highlightColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(1f, 0.70f, 0.18f, 1f);

            case CLASSIC_HOLLOW:
                return new Color(0.45f, 0.82f, 1f, 1f);

            case VOIDHEART:
            default:
                return new Color(0.85f, 0.90f, 1f, 1f);
        }
    }

    public Color bodyColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.82f, 0.75f, 0.64f, 1f);

            case CLASSIC_HOLLOW:
                return new Color(0.77f, 0.88f, 0.94f, 1f);

            case VOIDHEART:
            default:
                return new Color(0.78f, 0.80f, 0.86f, 1f);
        }
    }

    public void drawBackground(float delta, boolean saveScreen) {
        drawBackground(delta, saveScreen, 1f, 1f);
    }

    public void drawBackground(
            float delta, boolean saveScreen, float backgroundAlpha, float overlayAlphaMultiplier) {
        particleTime += delta;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        float clampedBackgroundAlpha = Math.max(0f, Math.min(1f, backgroundAlpha));
        float clampedOverlayMultiplier = Math.max(0f, Math.min(1f, overlayAlphaMultiplier));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        backgroundBatch.begin();

        drawCovered(
                saveScreen ? saveBackgroundTexture : backgroundTexture,
                width,
                height,
                themeTint(clampedBackgroundAlpha));

        drawParticles(width, height);

        backgroundBatch.setColor(
                0f,
                0f,
                0f,
                (theme == MenuThemeType.ROYAL_GOLD ? 0.45f : 0.28f) * clampedOverlayMultiplier);
        backgroundBatch.draw(backgroundTexture, 0f, 0f, width, height);

        backgroundBatch.setColor(Color.WHITE);
        backgroundBatch.end();
    }

    public Image createTitleLogo(float width) {
        Image image = new Image(drawable(titleLogoTexture));

        image.setColor(titleColor());
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        image.setSize(width, width * 0.32f);

        return image;
    }

    public Image createOrnament(float width) {
        Image image = new Image(drawable(titleOrnamentTexture));

        image.setColor(highlightColor());
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        image.setSize(width, width * 0.09f);

        return image;
    }

    public Image createMenuHeaderFleur(float width) {
        return createFleurImage(menuHeaderFleurTexture, width, 0.92f);
    }

    public Image createMenuFooterFleur(float width) {
        return createFleurImage(menuFooterFleurTexture, width, 0.78f);
    }

    public Image createPauseHeaderFleur(float width) {
        return createFleurImage(pauseHeaderFleurTexture, width, 0.90f);
    }

    public Image createPanelBorder(float width, float height) {
        Image image = new Image(drawable(borderTexture));
        image.setColor(1f, 1f, 1f, 0.65f);
        image.setSize(width, height);
        return image;
    }

    public Image createSlotPreview(int slotNumber, boolean saved) {
        Texture texture;

        switch (slotNumber) {
            case 2:
                texture = slotCityOfTearsTexture;
                break;

            case 3:
                texture = slotAbyssTexture;
                break;

            case 4:
                texture = slotWhitePalaceTexture;
                break;

            case 1:
            default:
                texture = saved ? slotForgottenCrossroadsTexture : slotDirtmouthTexture;
                break;
        }

        Image image = new Image(drawable(texture));
        image.setScaling(com.badlogic.gdx.utils.Scaling.stretch);

        if (!saved) {
            image.setColor(0.45f, 0.50f, 0.58f, 0.72f);
        }

        return image;
    }

    public Image createSoulOrbIcon(float size) {
        Image image = new Image(drawable(soulOrbTexture));
        image.setSize(size, size);
        return image;
    }

    public Image createHealthIcon(float size) {
        Image image = new Image(drawable(healthMaskTexture));
        image.setSize(size, size);
        return image;
    }

    public Image createMagicOrbIcon(float size) {
        Image image = new Image(drawable(magicOrbTexture));
        image.setSize(size, size);
        return image;
    }

    public Image createSpellIcon(String spellKey) {
        Texture texture;

        if ("shade".equals(spellKey)) {
            texture = shadeSoulTexture;
        } else if ("howling".equals(spellKey)) {
            texture = howlingWraithsTexture;
        } else if ("abyss".equals(spellKey)) {
            texture = abyssShriekTexture;
        } else {
            texture = vengefulSpiritTexture;
        }

        Image image = new Image(drawable(texture));
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        return image;
    }

    public Drawable panelDrawable(float alpha) {
        return solidDrawable(0.02f, 0.025f, 0.035f, alpha);
    }

    public Drawable lineDrawable() {
        Color color = highlightColor();
        return solidDrawable(color.r, color.g, color.b, 0.80f);
    }

    public TextButton createMenuButton(String text) {
        TextButton button = new TextButton(text, skin);

        button.getLabel().setFontScale(1.08f);
        button.getLabel().setColor(bodyColor());
        return button;
    }

    public Label createTitleLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(titleColor());
        label.setFontScale(2.15f);
        return label;
    }

    public Label createSectionLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(highlightColor());
        label.setFontScale(1.26f);
        return label;
    }

    public Label createBodyLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(bodyColor());
        label.setFontScale(1.08f);
        return label;
    }

    @Override
    public void dispose() {
        backgroundBatch.dispose();

        if (skin != null) {
            skin.dispose();
        }

        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
    }

    public Skin getSkin() {
        return skin;
    }

    public MenuThemeType getTheme() {
        return theme;
    }

    public Texture getDialogueDividerTexture() {
        return dialogueDividerTexture;
    }

    public Texture getSoulOrbTexture() {
        return soulOrbTexture;
    }

    public Texture getSoulGlowTexture() {
        return soulGlowTexture;
    }

    private Image createFleurImage(Texture texture, float width, float alpha) {
        Image image = new Image(drawable(texture));

        Color tint = highlightColor();

        image.setColor(tint.r, tint.g, tint.b, alpha);

        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        float safeWidth = Math.max(1f, width);
        float height = safeWidth * texture.getHeight() / Math.max(1f, texture.getWidth());

        image.setSize(safeWidth, height);

        return image;
    }

    private static void applyCustomCursor() {
        if (cursorLoaded) {
            if (sharedCursor != null) {
                Gdx.graphics.setCursor(sharedCursor);
            }
            return;
        }

        FileHandle cursorFile = findCursorFile();

        if (cursorFile == null) {
            return;
        }

        Pixmap sourcePixmap = null;
        Pixmap cursorPixmap = null;

        try {
            sourcePixmap = new Pixmap(cursorFile);
            cursorPixmap = makeCursorPixmap(sourcePixmap);

            sharedCursor = Gdx.graphics.newCursor(cursorPixmap, 2, 0);

            cursorLoaded = true;
            Gdx.graphics.setCursor(sharedCursor);
        } catch (GdxRuntimeException exception) {
            sharedCursor = null;
            cursorLoaded = false;
            Gdx.app.error(
                    "MenuThemeSkin",
                    "Could not load custom menu cursor from " + cursorFile.path(),
                    exception);
        } finally {
            if (cursorPixmap != null) {
                cursorPixmap.dispose();
            }

            if (sourcePixmap != null) {
                sourcePixmap.dispose();
            }
        }
    }

    private static FileHandle findCursorFile() {
        String[] candidates = {
            BASE_PATH + "common/cursor_runtime.png",
            BASE_PATH + "common/cursor.png",
            BASE_PATH + "common/Cursor.png",
            "ui/menu/Cursor.png",
            "ui/Cursor.png",
            "Cursor.png"
        };

        for (String candidate : candidates) {
            FileHandle file = Gdx.files.internal(candidate);

            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    private static Pixmap makeCursorPixmap(Pixmap sourcePixmap) {
        final int cursorCanvasSize = 64;
        final int cursorVisibleSize = 24;

        Pixmap cursorPixmap =
                new Pixmap(cursorCanvasSize, cursorCanvasSize, Pixmap.Format.RGBA8888);

        cursorPixmap.setColor(0f, 0f, 0f, 0f);
        cursorPixmap.fill();
        cursorPixmap.setBlending(Pixmap.Blending.SourceOver);
        cursorPixmap.drawPixmap(
                sourcePixmap,
                0,
                0,
                sourcePixmap.getWidth(),
                sourcePixmap.getHeight(),
                0,
                0,
                cursorVisibleSize,
                cursorVisibleSize);

        return cursorPixmap;
    }

    private Texture load(String path) {
        FileHandle file = Gdx.files.internal(path);

        if (!file.exists()) {
            Texture fallback = createFallbackTexture();
            ownedTextures.add(fallback);
            return fallback;
        }

        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
    }

    private BitmapFont createTrajanFont(
            int size, float borderWidth, int shadowOffsetX, int shadowOffsetY) {
        FileHandle fontFile = Gdx.files.internal(TRAJAN_FONT_PATH);

        if (!fontFile.exists()) {
            return null;
        }

        FreeTypeFontGenerator generator = null;

        try {
            generator = new FreeTypeFontGenerator(fontFile);

            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = size;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.borderWidth = borderWidth;
            parameter.borderColor = new Color(0f, 0f, 0f, 0.72f);
            parameter.shadowOffsetX = shadowOffsetX;
            parameter.shadowOffsetY = shadowOffsetY;
            parameter.shadowColor = new Color(0f, 0f, 0f, 0.62f);
            parameter.characters =
                    FreeTypeFontGenerator.DEFAULT_CHARS
                            + "–—’‘“”…•◦؛،؟آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیءئۀة";

            BitmapFont font = generator.generateFont(parameter);
            font.getData().markupEnabled = true;

            return font;
        } catch (GdxRuntimeException exception) {
            return null;
        } finally {
            if (generator != null) {
                generator.dispose();
            }
        }
    }

    private BitmapFont createOrGetFont(String skinFontName, int size, float borderWidth) {
        BitmapFont trajanFont = createTrajanFont(size, borderWidth, 1, -1);

        if (trajanFont != null) {
            skin.add(skinFontName, trajanFont, BitmapFont.class);
            return trajanFont;
        }

        return skin.getFont(skinFontName);
    }

    private <T> T findStyle(String name, Class<T> styleType) {
        try {
            return skin.get(name, styleType);
        } catch (GdxRuntimeException exception) {
            return null;
        }
    }

    private void customizeSkin() {
        BitmapFont defaultFont = createOrGetFont("default", MENU_BODY_FONT_SIZE, 0.55f);

        BitmapFont bodyFont = createOrGetFont("font", MENU_BODY_FONT_SIZE, 0.55f);

        BitmapFont subtitleFont = createOrGetFont("subtitle", MENU_BODY_FONT_SIZE, 0.55f);

        BitmapFont smallFont = createOrGetFont("list", MENU_SMALL_FONT_SIZE, 0.45f);

        BitmapFont titleFont = createOrGetFont("window", MENU_TITLE_FONT_SIZE, 0.70f);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = bodyFont;
        labelStyle.fontColor = bodyColor();

        Label.LabelStyle windowLabelStyle = findStyle("window", Label.LabelStyle.class);

        if (windowLabelStyle != null) {
            windowLabelStyle.font = titleFont;
            windowLabelStyle.fontColor = titleColor();
        }

        Label.LabelStyle listLabelStyle = findStyle("list", Label.LabelStyle.class);

        if (listLabelStyle != null) {
            listLabelStyle.font = smallFont;
            listLabelStyle.fontColor = bodyColor();
        }

        Label.LabelStyle subtitleLabelStyle = findStyle("subtitle", Label.LabelStyle.class);

        if (subtitleLabelStyle != null) {
            subtitleLabelStyle.font = subtitleFont;
            subtitleLabelStyle.fontColor = bodyColor();
        }

        TextButton.TextButtonStyle buttonStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle.font = bodyFont;
        buttonStyle.fontColor = bodyColor();
        buttonStyle.overFontColor = highlightColor();
        buttonStyle.downFontColor = Color.WHITE;
        buttonStyle.checkedFontColor = highlightColor();
        buttonStyle.up = solidDrawable(0f, 0f, 0f, 0f);
        buttonStyle.down = solidDrawable(1f, 1f, 1f, 0.06f);
        buttonStyle.over = solidDrawable(1f, 1f, 1f, 0.035f);

        SelectBox.SelectBoxStyle selectStyle = skin.get(SelectBox.SelectBoxStyle.class);
        selectStyle.font = bodyFont;
        selectStyle.fontColor = bodyColor();

        if (selectStyle.listStyle != null) {
            selectStyle.listStyle.font = smallFont;
            selectStyle.listStyle.fontColorSelected = highlightColor();
            selectStyle.listStyle.fontColorUnselected = bodyColor();
        }

        List.ListStyle listStyle = skin.get(List.ListStyle.class);
        listStyle.font = smallFont;
        listStyle.fontColorSelected = highlightColor();
        listStyle.fontColorUnselected = bodyColor();

        TextField.TextFieldStyle textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = bodyFont;
        textFieldStyle.messageFont = bodyFont;
        textFieldStyle.fontColor = bodyColor();
        textFieldStyle.messageFontColor = bodyColor();

        Window.WindowStyle windowStyle = skin.get(Window.WindowStyle.class);
        windowStyle.titleFont = titleFont;
        windowStyle.titleFontColor = titleColor();

        Slider.SliderStyle sliderStyle = skin.get("default-horizontal", Slider.SliderStyle.class);
        sliderStyle.background =
                new TextureRegionDrawable(
                                new TextureRegion(
                                        load(BASE_PATH + "settings/horizontal_slider.png")))
                        .tint(bodyColor());
        sliderStyle.knob = drawable(load(BASE_PATH + "settings/slider_thumb.png"));
        sliderStyle.knobOver = drawable(load(BASE_PATH + "settings/slider_thumb_active.png"));
        sliderStyle.knobDown = sliderStyle.knobOver;

        ScrollPane.ScrollPaneStyle scrollPaneStyle = skin.get(ScrollPane.ScrollPaneStyle.class);

        TextureRegionDrawable verticalTrack =
                new TextureRegionDrawable(new TextureRegion(verticalScrollTrackTexture));

        verticalTrack.setMinWidth(18f);
        verticalTrack.setMinHeight(96f);

        TextureRegionDrawable verticalKnob =
                new TextureRegionDrawable(new TextureRegion(verticalScrollKnobTexture));

        verticalKnob.setMinWidth(30f);
        verticalKnob.setMinHeight(60f);

        scrollPaneStyle.vScroll = verticalTrack;
        scrollPaneStyle.vScrollKnob = verticalKnob;
        scrollPaneStyle.hScroll = null;
        scrollPaneStyle.hScrollKnob = null;

        CheckBox.CheckBoxStyle checkBoxStyle = skin.get(CheckBox.CheckBoxStyle.class);
        checkBoxStyle.font = bodyFont;
        checkBoxStyle.fontColor = bodyColor();
        checkBoxStyle.checkboxOn = drawable(load(BASE_PATH + "settings/toggle_on.png"));
        checkBoxStyle.checkboxOff = drawable(load(BASE_PATH + "settings/toggle_hover.png"));
        checkBoxStyle.checkboxOver = drawable(load(BASE_PATH + "settings/toggle_active.png"));
        checkBoxStyle.checkboxOnOver = checkBoxStyle.checkboxOver;
    }

    private void drawCovered(Texture texture, float width, float height, Color color) {
        float textureRatio = (float) texture.getWidth() / Math.max(1f, texture.getHeight());

        float screenRatio = width / Math.max(1f, height);

        float drawWidth = width;
        float drawHeight = height;

        if (textureRatio > screenRatio) {
            drawWidth = height * textureRatio;
        } else {
            drawHeight = width / textureRatio;
        }

        float x = (width - drawWidth) / 2f;
        float y = (height - drawHeight) / 2f;

        backgroundBatch.setColor(color);
        backgroundBatch.draw(texture, x, y, drawWidth, drawHeight);
    }

    private void drawBeam(float width, float height) {
        Texture beam = theme == MenuThemeType.VOIDHEART ? voidBeamTexture : mainBeamTexture;

        Color color = highlightColor();
        backgroundBatch.setColor(color.r, color.g, color.b, 0.18f);

        float beamHeight = height * 0.82f;
        float beamWidth = beamHeight * beam.getWidth() / Math.max(1f, beam.getHeight());

        backgroundBatch.draw(
                beam, width * 0.5f - beamWidth * 0.5f, height * 0.18f, beamWidth, beamHeight);
    }

    private void drawParticles(float width, float height) {
        Color color = highlightColor();

        for (int index = 0; index < 18; index++) {
            float seed = index * 37.17f;
            float x = (seed * 53f) % width;
            float y = ((seed * 29f) + particleTime * (12f + index)) % height;
            float size = 5f + (index % 4) * 2.3f;
            float alpha = 0.10f + (index % 5) * 0.028f;

            backgroundBatch.setColor(color.r, color.g, color.b, alpha);
            backgroundBatch.draw(soulGlowTexture, x, y, size, size);
        }
    }

    private Color themeTint(float alpha) {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.72f, 0.55f, 0.34f, alpha);

            case CLASSIC_HOLLOW:
                return new Color(0.55f, 0.80f, 1f, alpha);

            case VOIDHEART:
            default:
                return new Color(0.86f, 0.90f, 1f, alpha);
        }
    }

    private TextureRegionDrawable drawable(Texture texture) {
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Drawable solidDrawable(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(r, g, b, a);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        ownedTextures.add(texture);

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.CLEAR);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
