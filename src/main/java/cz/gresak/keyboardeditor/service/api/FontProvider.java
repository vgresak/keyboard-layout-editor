package cz.gresak.keyboardeditor.service.api;

import javafx.scene.text.Font;

/**
 * Provides utility methods concerning font. This is the center place that should be used to work with fonts.
 */
public interface FontProvider {

    int MIN_SIZE = 8;
    int MAX_SIZE = 40;
    int SIZE_INCREMENT = 4;

    /**
     * Retrieves name of the font that is capable of displaying given text.
     * If selected default font cannot display the provided text then the method searches through all available fonts.
     *
     * @param text text which the returned font should be able to display
     * @return name of the font that is capable of displaying given text or default font if no available font can display the text
     */
    String getFontName(String text);

    /**
     * Calculated size of a font according to a displayed text, used font and dimensions in which the text should be displayed.
     * <p>
     * In order to display font sizes more uniformly (i.e. each key should not be displayed in different font size), returned font size is selected in increments of {@value #SIZE_INCREMENT}.
     * Minimum size of font is {@value #MIN_SIZE} and maximum {@value #MAX_SIZE}.
     *
     * @param text        displayed text
     * @param font        font used to display text
     * @param widthToFit  width in which the displayed text using the selected font should fit
     * @param heightToFit height in which the displayed text using the selected font should fit
     * @return font size that fits width and height
     */
    double getFontSize(String text, Font font, double widthToFit, double heightToFit);

    /**
     * Retrieves currently set default font.
     *
     * @return currently set default font
     */
    Font getDefaultFont();

    /**
     * Sets default font.
     *
     * @param defaultFont new default font to use
     */
    void setDefaultFont(Font defaultFont);

    /**
     * Adds new listener that is notified whenever {@link #setDefaultFont(Font)} is invoked.
     *
     * @param listener listener to add
     */
    void addDefaultFontChangedListener(DefaultFontChangedListener listener);

    /**
     * Interface of a listener that listens for default font change.
     */
    interface DefaultFontChangedListener {
        /**
         * Callback with a new default font.
         *
         * @param newFont newly set default font
         */
        void action(Font newFont);
    }
}
