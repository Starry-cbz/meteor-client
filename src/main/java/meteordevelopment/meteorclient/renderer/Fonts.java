/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.renderer;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.CustomFontChangedEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.render.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Fonts {
    public static final String[] BUILTIN_FONTS = { "JetBrains Mono", "Comfortaa", "Tw Cen MT", "Pixelation" };

    /** CJK font families to search for on the system when the game language is CJK. */
    private static final Set<String> CJK_FONT_FAMILIES = Set.of(
        "Microsoft YaHei", "SimHei", "SimSun", "DengXian", "FangSong", "KaiTi", "Microsoft JhengHei",
        "PingFang SC", "Heiti SC", "STHeiti",
        "Noto Sans CJK SC", "WenQuanYi Micro Hei", "WenQuanYi Zen Hei",
        "Noto Sans CJK JP", "Noto Sans CJK KR"
    );

    /** CJK language codes that need CJK font support. */
    private static final Set<String> CJK_LANGS = Set.of("zh_cn", "zh_tw", "zh_hk", "ja_jp", "ko_kr");

    public static String DEFAULT_FONT_FAMILY;
    public static FontFace DEFAULT_FONT;

    public static final List<FontFamily> FONT_FAMILIES = new ArrayList<>();
    public static CustomTextRenderer RENDERER;

    private Fonts() {
    }

    @PreInit
    public static void refresh() {
        FONT_FAMILIES.clear();

        for (String builtinFont : BUILTIN_FONTS) {
            FontUtils.loadBuiltin(FONT_FAMILIES, builtinFont);
        }

        for (String fontPath : FontUtils.getSearchPaths()) {
            FontUtils.loadSystem(FONT_FAMILIES, new File(fontPath));
        }

        FONT_FAMILIES.sort(Comparator.comparing(FontFamily::getName));

        MeteorClient.LOG.info("Found {} font families.", FONT_FAMILIES.size());

        // Select default font - prefer CJK-capable font when game language is CJK
        String lang = mc.options.language;
        if (lang != null && CJK_LANGS.contains(lang)) {
            DEFAULT_FONT = findCjkFont();
            if (DEFAULT_FONT != null) {
                DEFAULT_FONT_FAMILY = DEFAULT_FONT.info.family();
                MeteorClient.LOG.info("CJK locale detected ({}), using CJK font: {}", lang, DEFAULT_FONT_FAMILY);
            }
        }

        if (DEFAULT_FONT == null) {
            DEFAULT_FONT_FAMILY = FontUtils.getBuiltinFontInfo(BUILTIN_FONTS[1]).family();
            DEFAULT_FONT = getFamily(DEFAULT_FONT_FAMILY).get(FontInfo.Type.Regular);
        }

        Config config = Config.get();
        load(config != null ? config.font.get() : DEFAULT_FONT);
    }

    public static void load(FontFace fontFace) {
        if (RENDERER != null) {
            if (RENDERER.fontFace.equals(fontFace)) return;
            else RENDERER.destroy();
        }

        try {
            RENDERER = new CustomTextRenderer(fontFace);
            MeteorClient.EVENT_BUS.post(CustomFontChangedEvent.get());
        }
        catch (Exception e) {
            if (fontFace.equals(DEFAULT_FONT)) {
                throw new RuntimeException("Failed to load default font: " + fontFace, e);
            }

            MeteorClient.LOG.error("Failed to load font: {}", fontFace, e);
            load(Fonts.DEFAULT_FONT);
        }

        if (mc.currentScreen instanceof WidgetScreen && Config.get().customFont.get()) {
            ((WidgetScreen) mc.currentScreen).invalidate();
        }
    }

    public static FontFamily getFamily(String name) {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (fontFamily.getName().equalsIgnoreCase(name)) {
                return fontFamily;
            }
        }

        return null;
    }

    /** Tries to find a CJK-capable system font, returning null if none found. */
    private static FontFace findCjkFont() {
        for (FontFamily family : FONT_FAMILIES) {
            if (CJK_FONT_FAMILIES.contains(family.getName())) {
                FontFace face = family.get(FontInfo.Type.Regular);
                if (face != null) return face;
            }
        }
        return null;
    }
}
