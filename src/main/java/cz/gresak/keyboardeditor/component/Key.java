package cz.gresak.keyboardeditor.component;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import cz.gresak.keyboardeditor.model.ModelKey;
import cz.gresak.keyboardeditor.service.ServiceLoader;
import cz.gresak.keyboardeditor.service.api.FontProvider;
import cz.gresak.keyboardeditor.service.api.GroupState;
import cz.gresak.keyboardeditor.service.api.KeysymMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Key extends Pane {

    private static final int PADDING = 2;
    private static final int KEY_COLUMN_SPACE = 6;
    private final ModelKey key;

    @FXML
    private Text topLeftChar;
    @FXML
    private Text bottomLeftChar;
    @FXML
    private Text topRightChar;
    @FXML
    private Text bottomRightChar;
    private KeyLayout keyLayout;
    private KeyConfig keyConfig;
    private FontProvider fontProvider = ServiceLoader.lookup(FontProvider.class);
    private GroupState groupState = ServiceLoader.lookup(GroupState.class);
    private KeysymMapper mapper = ServiceLoader.lookup(KeysymMapper.class);

    public Key(ModelKey key) {
        this.key = key;
        this.keyConfig = new KeyConfig(true, true, true, true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/component/key.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(getClass().getResource("/component/key.css").toString());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setOnMouseClicked(event -> System.out.println("Clicked " + this.key));
        updateContent();
        widthProperty().addListener((observable, oldValue, newValue) -> updateSize());
        heightProperty().addListener((observable, oldValue, newValue) -> updateSize());
        fontProvider.addDefaultFontChangedListener(newFont -> setFontFamily());
        groupState.addListener(newGroup -> updateContent());
        mapper.addShowNoSymbolChangedListener(newValue -> updateContent());
    }

    public void select() {
        getStyleClass().add("selected");
    }

    public void unselect() {
        getStyleClass().remove("selected");
    }

    private void updateContent() {
        setChars();
        setFontFamily();
        updateSize();
    }

    /**
     * Sets character values.
     */
    private void setChars() {
        int group = groupState.getGroup();
        List<String> groupValues = key.getGroup(group);
        setChars(groupValues);
    }

    /**
     * Sets font family for characters. This should be called before setting font size.
     */
    private void setFontFamily() {
        setFontFamily(topLeftChar);
        setFontFamily(bottomLeftChar);
        setFontFamily(topRightChar);
        setFontFamily(bottomRightChar);
    }

    private void setFontFamily(Text text) {
        text.setFont(new Font(fontProvider.getFontName(text.getText()), text.getFont().getSize()));
    }

    private void updateSize() {
        setClip(getShape()); // avoid overflowing
        updateFontSizes();
        layoutChars();
    }

    /**
     * Sets font sizes according to current key size.
     */
    private void updateFontSizes() {
        Font font = topLeftChar.getFont();
        String sample = Stream.of(topLeftChar.getText(), bottomLeftChar.getText(), topRightChar.getText(), bottomRightChar.getText())
                .max(Comparator.comparing(String::length))
                .orElse("M");

        double widthToFitFont;
        double heightToFitFont;
        switch (keyLayout) {
            case SINGLE_LINE:
            case TWO_LINE:
                widthToFitFont = Math.max(getWidth() - PADDING * 2, 0);
                heightToFitFont = Math.max(getHeight() / 2 - PADDING * 4, 0);
                break;
            case FOUR_SECTIONS:
                widthToFitFont = Math.max(getWidth() / 2 - PADDING * 2, 0);
                heightToFitFont = Math.max(getHeight() / 2 - PADDING * 4, 0);
                break;
            default:
                throw new RuntimeException("Unsupported key layout");
        }

        double fontSize = fontProvider.getFontSize(sample, font, widthToFitFont, heightToFitFont);

        //set font sizes
        setFontSizes(fontSize);
    }

    private void setFontSizes(double fontSize) {
        setFontSize(topLeftChar, fontSize);
        setFontSize(bottomLeftChar, fontSize);
        setFontSize(bottomRightChar, fontSize);
        setFontSize(topRightChar, fontSize);
    }

    private void setFontSize(Text text, double fontSize) {
        Font font = text.getFont();
        text.setFont(new Font(font.getName(), fontSize));
    }


    /**
     * Updates position of text.
     */
    private void layoutChars() {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        FontMetrics topLeftFont = fontLoader.getFontMetrics(topLeftChar.getFont());
        FontMetrics bottomLeftFont = fontLoader.getFontMetrics(bottomLeftChar.getFont());
        FontMetrics topRightFont = fontLoader.getFontMetrics(topRightChar.getFont());
        FontMetrics bottomRightFont = fontLoader.getFontMetrics(bottomRightChar.getFont());
        // vertical alignment
        double topLine = Math.max(topLeftFont.getLineHeight(), topRightFont.getLineHeight());
        double bottomLine = Math.max(bottomLeftFont.getLineHeight(), bottomRightFont.getLineHeight()) + topLine;
        topLeftChar.setLayoutY(topLine);
        topRightChar.setLayoutY(topLine);
        bottomLeftChar.setLayoutY(bottomLine);
        bottomRightChar.setLayoutY(bottomLine);
        // horizontal alignment (needed only for the right column)
        double topLeftWidth = fontLoader.computeStringWidth(topLeftChar.getText(), topLeftChar.getFont());
        double bottomLeftWidth = fontLoader.computeStringWidth(bottomLeftChar.getText(), bottomLeftChar.getFont());
        double maxLeftColumnLayoutX = Math.max(topLeftChar.getLayoutX(), bottomLeftChar.getLayoutX());
        double xOffset = Math.max(
                (getWidth() / 2 - PADDING) + KEY_COLUMN_SPACE, // center of the key
                Math.max(topLeftWidth, bottomLeftWidth) + maxLeftColumnLayoutX + KEY_COLUMN_SPACE); // offset based on left column width
        topRightChar.setLayoutX(xOffset);
        bottomRightChar.setLayoutX(xOffset);
    }

    private void setChars(List<String> keysyms) {
        // create set of symbols, removing redundancies (e.g. functional keys often have the same value for every level)
        List<String> symbolSet = keysyms.stream()
                .limit(4)
                .map(mapper::getSymbol)
                .collect(Collectors.toList());
        // If all displayed values would be the same, display the value just once (e.g. functional keys)
        if (new HashSet<>(symbolSet).size() == 1) {
            symbolSet = symbolSet.stream()
                    .limit(1)
                    .collect(Collectors.toList());
        }
        keyLayout = KeyLayout.SINGLE_LINE;
        if (symbolSet.isEmpty()) {
            setTopLeft("");
            setBottomLeft("");
            setTopRight("");
            setBottomRight("");
            return;
        }
        Iterator<String> symbolIterator = symbolSet.iterator();
        if (symbolSet.size() == 1) {
            setBottomLeft(symbolIterator.next());
            setTopLeft("");
            setTopRight("");
            setBottomRight("");
            return;
        }
        keyLayout = KeyLayout.TWO_LINE;
        setBottomLeft(symbolIterator.next());
        setTopLeft(symbolIterator.next());
        if (symbolIterator.hasNext()) {
            keyLayout = KeyLayout.FOUR_SECTIONS;
            setBottomRight(symbolIterator.next());
        } else {
            setTopRight("");
            setBottomRight("");
        }

        if (symbolIterator.hasNext()) {
            setTopRight(symbolIterator.next());
        } else {
            setTopRight("");
        }
        updateVisibility();
    }

    public ModelKey getKey() {
        return key;
    }

    private void setTopLeft(String value) {
        setText(topLeftChar, value);
    }

    private void setBottomLeft(String value) {
        setText(bottomLeftChar, value);
    }

    private void setTopRight(String value) {
        setText(topRightChar, value);
    }

    private void setBottomRight(String value) {
        setText(bottomRightChar, value);
    }

    private void setText(Text text, String value) {
        // Setting visibility is necessary in order to avoid problems with overflowing empty text.
        // This is the case when the KeyLayout is SINGLE_LINE and the text is displayed with a large font size - other three empty text components are outside the component and causing troubles (hover outside of component bounds).
        text.setVisible(StringUtils.isNotBlank(value));
        text.setText(value);
    }

    public void setValue(String text, int levelIndex, int group) {
        List<String> keysymsInGroup = key.getGroup(group);

        while (keysymsInGroup.size() <= levelIndex) {
            keysymsInGroup.add("NoSymbol");
        }
        if (StringUtils.isBlank(text)) {
            text = "NoSymbol";
        }
        keysymsInGroup.set(levelIndex, text);
        updateContent();
    }

    public void updateKeyConfig(KeyConfig keyConfig) {
        this.keyConfig = keyConfig;
        updateVisibility();
    }

    private void updateVisibility() {
        switch (keyLayout) {
            case SINGLE_LINE:
                setVisibility(bottomLeftChar, keyConfig.showLevel1);
                setVisibility(topLeftChar, false);
                setVisibility(bottomRightChar, false);
                setVisibility(topRightChar, false);
                break;
            case TWO_LINE:
                setVisibility(bottomLeftChar, keyConfig.showLevel1);
                setVisibility(topLeftChar, keyConfig.showLevel2);
                setVisibility(bottomRightChar, false);
                setVisibility(topRightChar, false);
                break;
            case FOUR_SECTIONS:
                setVisibility(bottomLeftChar, keyConfig.showLevel1);
                setVisibility(topLeftChar, keyConfig.showLevel2);
                setVisibility(bottomRightChar, keyConfig.showLevel3);
                setVisibility(topRightChar, keyConfig.showLevel4);
                break;
        }
    }

    private void setVisibility(Text text, boolean show) {
        text.setVisible(show && StringUtils.isNotBlank(text.getText()));
    }

    /**
     * There are 3 cases for a key that determine the box for which the font size is calculated.
     * First one is when the whole key can be used:
     * ______
     * | Esc |
     * |     |
     * |_____|
     * Second is when the key contains two values and can be split vertically:
     * ______
     * |  ⇤    |
     * |  ⇥    |
     * |_____|
     * Third case is when the key contains 3 or more values and is separated to 4 sections:
     * ______
     * | A   |
     * | a \ |
     * |_____|
     */
    private enum KeyLayout {
        SINGLE_LINE,
        TWO_LINE,
        FOUR_SECTIONS
    }

    public static class KeyConfig {
        private final boolean showLevel1;
        private final boolean showLevel2;
        private final boolean showLevel3;
        private final boolean showLevel4;

        public KeyConfig(boolean showLevel1, boolean showLevel2, boolean showLevel3, boolean showLevel4) {
            this.showLevel1 = showLevel1;
            this.showLevel2 = showLevel2;
            this.showLevel3 = showLevel3;
            this.showLevel4 = showLevel4;
        }
    }

}
