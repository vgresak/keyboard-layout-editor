package cz.gresak.keyboardeditor.service.impl;

import com.sun.javafx.font.CharToGlyphMapper;
import com.sun.javafx.font.FontFactory;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import com.sun.prism.GraphicsPipeline;
import cz.gresak.keyboardeditor.service.api.FontProvider;
import javafx.scene.text.Font;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class FontProviderImpl implements FontProvider {
    private static final Deque<Font> cache = new ArrayDeque<>();
    private static final Preferences preferences = Preferences.userRoot().node(FontProviderImpl.class.getName());
    private static FontProvider instance;
    private final List<DefaultFontChangedListener> listeners;
    private final FontFactory fontFactory;
    private Font defaultFont;

    public FontProviderImpl() {
        listeners = new ArrayList<>();
        fontFactory = GraphicsPipeline.getPipeline().getFontFactory();
        String fontName = preferences.get("font", Font.getDefault().getName());
        defaultFont = new Font(fontName, Font.getDefault().getSize());
    }

    public static FontProvider getInstance() {
        if (instance == null) {
            instance = new FontProviderImpl();
        }
        return instance;
    }

    @Override
    public String getFontName(String text) {
        //try default font
        if (canDisplay(text, defaultFont)) {
            return defaultFont.getName();
        }
        //look for compatible font in cache
        Optional<Font> cachedFont = cache.stream().filter(font -> canDisplay(text, font)).findFirst();
        if (cachedFont.isPresent()) {
            return cachedFont.get().getName();
        }
        //search through all available fonts
        for (String fontFullName : fontFactory.getFontFullNames()) {
            Font candidate = new Font(fontFullName, 13);
            if (canDisplay(text, candidate)) {
                cache.push(candidate);
                return candidate.getName();
            }
        }
        //Failed to find font that is able to display text
        return defaultFont.getName();
    }

    private boolean canDisplay(String text, Font font) {
        PGFont pgFont = fontFactory.createFont(font.getName(), 13);
        if (pgFont == null) {
            return false;
        }
        CharToGlyphMapper glyphMapper = pgFont.getFontResource().getGlyphMapper();
        char[] chars = text.toCharArray();
        for (char character : chars) {
            if (!glyphMapper.canDisplay(character)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getFontSize(String text, Font font, double widthToFit, double heightToFit) {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double sampleWidth = fontLoader.computeStringWidth(text, font);
        double fontSizeWidth = (widthToFit / sampleWidth) * font.getSize();

        FontMetrics fontMetrics = fontLoader.getFontMetrics(font);
        double fontSizeHeight = (heightToFit / fontMetrics.getLineHeight()) * font.getSize();
        // this size fits perfectly to defined area
        double fontSize = Math.min(fontSizeWidth, fontSizeHeight);
        // select size in larger increments so keys have uniform font sizes
        for (int i = MAX_SIZE; i > MIN_SIZE - 1; i -= SIZE_INCREMENT) {
            if (i <= fontSize) {
                return i;
            }
        }
        return MIN_SIZE;
    }

    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    @Override
    public void setDefaultFont(Font defaultFont) {
        this.defaultFont = defaultFont;
        listeners.forEach(listener -> listener.action(this.defaultFont));
        preferences.put("font", defaultFont.getName());
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDefaultFontChangedListener(DefaultFontChangedListener listener) {
        listeners.add(listener);
    }

}
