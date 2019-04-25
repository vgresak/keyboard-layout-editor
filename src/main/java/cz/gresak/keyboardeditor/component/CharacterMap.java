package cz.gresak.keyboardeditor.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterMap extends Dialog<String> {

    private static final Background WHITE_BACKGROUND = new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, Insets.EMPTY));
    private static final Background GRAY_BACKGROUND = new Background(new BackgroundFill(Paint.valueOf("#eee"), null, null));
    private String selectedValue;
    private String selectedSymbol;
    private FlowPane selectedPane;
    private Font selectedFont = Font.getDefault();
    private GridPane gridPane;
    private List<Text> displayedCharacters = new ArrayList<>();
    private ComboBox<String> categoryCombo;
    private Text symbolText;
    private Text unicodeText;

    public CharacterMap() {
        setTitle("Character map");
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        BorderPane content = new BorderPane();
        dialogPane.setContent(content);
        content.setPrefHeight(400);
        content.setPrefWidth(600);
        content.setMinHeight(400);
        content.setMinWidth(600);
        content.setTop(top());
        content.setCenter(center());
        content.setBottom(bottom());
        setResultConverter(this::getResult);
    }

    private Node top() {
        VBox topBar = new VBox();
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(70);
        ObservableList<Node> children = topBar.getChildren();
        Insets leftTen = new Insets(0, 0, 0, 10);

        HBox fontBox = new HBox();
        Text fontText = new Text("Font: ");
        fontBox.getChildren().add(fontText);
        HBox.setMargin(fontText, new Insets(0, 0, 0, 10));

        ComboBox<String> font = new ComboBox<>();
        font.setItems(getAllFamilies());
        font.getSelectionModel().select(selectedFont.getFamily());
        fontBox.getChildren().add(font);
        HBox.setMargin(font, leftTen);
        font.setOnAction(event -> fontChanged(font.getSelectionModel().getSelectedItem()));

        children.add(fontBox);

        HBox categoryBox = new HBox();
        Text categoryText = new Text("Category: ");
        categoryBox.getChildren().add(categoryText);
        HBox.setMargin(categoryText, new Insets(0, 0, 0, 10));

        categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(Characters.categories()));
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.setOnAction(event -> categoryChanged());
        categoryBox.getChildren().add(categoryCombo);
        HBox.setMargin(categoryCombo, leftTen);

        children.add(categoryBox);

        return topBar;
    }

    private void fontChanged(String selectedFamily) {
        selectedFont = new Font(selectedFamily, selectedFont.getSize());
        displayedCharacters.forEach(character -> character.setFont(selectedFont));
        unicodeText.setFont(new Font(selectedFont.getName(), 20));
        symbolText.setFont(new Font(selectedFont.getName(), 20));
    }

    private void categoryChanged() {
        setCharacterGrid();
    }

    private ObservableList<String> getAllFamilies() {
        return FXCollections.observableArrayList(Font.getFamilies());
    }

    private Node center() {
        ScrollPane centerPane = new ScrollPane();
        centerPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        centerPane.setBorder(Border.EMPTY);
        centerPane.setPrefHeight(200);
        centerPane.setStyle("-fx-focus-color: transparent;");
        gridPane = new GridPane();
        setCharacterGrid();
        centerPane.setContent(gridPane);
        return centerPane;
    }

    private Node bottom() {
        FlowPane bottomPane = new FlowPane();
        bottomPane.setPrefHeight(50);
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        ObservableList<Node> children = bottomPane.getChildren();
        symbolText = createLargeEmptyTextWithMargin(children);
        unicodeText = createLargeEmptyTextWithMargin(children);
        return bottomPane;
    }

    private Text createLargeEmptyTextWithMargin(ObservableList<Node> children) {
        Text textWithMargin = new Text("");
        textWithMargin.setFont(new Font(selectedFont.getName(), 20));
        children.add(textWithMargin);
        FlowPane.setMargin(textWithMargin, new Insets(0, 0, 0, 10));
        return textWithMargin;
    }

    private void setCharacterGrid() {
        displayedCharacters.clear();
        gridPane.getChildren().clear();
        String selectedCategory = categoryCombo.getSelectionModel().getSelectedItem();
        List<Pair<String, String>> characters = Characters.getCharacters(selectedCategory);
        for (int i = 0; i < characters.size(); i++) {
            createCharacterCell(characters, i);

        }
    }

    private void createCharacterCell(List<Pair<String, String>> characters, int index) {
        Pair<String, String> character = characters.get(index);
        int row = index / 16;
        int column = index % 16;
        FlowPane cell = new FlowPane();
        int topBorder = row == 0 ? 0 : 1;
        int leftBorder = column == 0 ? 0 : 1;
        BorderWidths borderWidths = new BorderWidths(topBorder, 0, 0, leftBorder);
        cell.setBorder(new Border(new BorderStroke(Paint.valueOf("#ddd"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, borderWidths)));
        cell.setPrefWidth(35);
        cell.setPrefHeight(35);
        cell.setBackground(WHITE_BACKGROUND);
        Text cellText = new Text(character.getKey());
        cell.setOnMouseClicked(event -> {
            selectedValue = character.getValue();
            selectedSymbol = character.getKey();
            if (selectedPane != null) {
                selectedPane.setBackground(WHITE_BACKGROUND);
            }
            selectedPane = cell;

            unicodeText.setText("Unicode: " + selectedValue);
            symbolText.setText("Symbol: " + selectedSymbol);
        });
        displayedCharacters.add(cellText);
        cellText.setFont(selectedFont);
        cell.setAlignment(Pos.CENTER);
        cell.getChildren().add(cellText);
        gridPane.add(cell, column, row);
        GridPane.setHalignment(cell, HPos.CENTER);
        GridPane.setValignment(cell, VPos.CENTER);
        cell.hoverProperty().addListener((observable, oldValue, newValue) -> cell.setBackground(newValue || cellText.getText().equals(selectedSymbol) ? GRAY_BACKGROUND : WHITE_BACKGROUND));
    }

    private String getResult(ButtonType param) {
        if (ButtonType.CANCEL.equals(param)) {
            return null;
        }
        return selectedValue;
    }

    private static class Characters {
        /**
         * Character map structured as follows:
         * <p>
         * [
         * CATEGORY => [{SYMBOL_REPRESENTATION, SYMBOL_VALUE}, …],
         * …
         * ]
         */
        private static final Map<String, List<Pair<String, String>>> characters = new LinkedHashMap<>();

        static {
            characters.put("Basic Latin", generateRange(0x0020, 0x007F));
            characters.put("Latin-1 Supplement", generateRange(0x00A1, 0x00FF));
            characters.put("Latin Extended-A", generateRange(0x0100, 0x017F));
            characters.put("Latin Extended-B", generateRange(0x0180, 0x024F));
            characters.put("General Punctuation", generateRange(0x2000, 0x206F));
            characters.put("Supplemental Punctuation", generateRange(0x2E00, 0x2E4E));
            characters.put("Mathematical Operators", generateRange(0x2200, 0x22FF));
            characters.put("Mathematical Alphanumeric Symbols", generateRange(0x1D400, 0x1D7FF));
            characters.put("Supplemental Mathematical Operators", generateRange(0x2A00, 0x2AFF));
            characters.put("Miscellaneous Mathematical Symbols-A", generateRange(0x27C0, 0x27EF));
            characters.put("Miscellaneous Mathematical Symbols-B", generateRange(0x2980, 0x29FF));
            characters.put("Currency Symbols", generateRange(0x20A0, 0x20CF));
            characters.put("Miscellaneous Symbols", generateRange(0x2600, 0x26FF));
            characters.put("Miscellaneous Symbols and Arrows", generateRange(0x2B00, 0x2BFF));
            characters.put("Supplemental Arrows-A", generateRange(0x27F0, 0x27FF));
            characters.put("Supplemental Arrows-B", generateRange(0x2900, 0x297F));
            characters.put("Supplemental Arrows-C", generateRange(0x1F800, 0x1F8FF));
            characters.put("Supplemental Symbols and Pictographs", generateRange(0x1F900, 0x1F9FF));
            characters.put("Box Drawing", generateRange(0x2500, 0x257F));
            characters.put("Block Elements", generateRange(0x2580, 0x259F));
            characters.put("Geometric Shapes", generateRange(0x25A0, 0x25FF));
            characters.put("IPA Extensions", generateRange(0x0250, 0x02AF));
            characters.put("Spacing Modifier Letters", generateRange(0x02B0, 0x02FF));
            characters.put("Combining Diacritical Marks", generateRange(0x0300, 0x036F));
            characters.put("Greek and Coptic", generateRange(0x0370, 0x03FF));
            characters.put("Cyrillic", generateRange(0x0400, 0x04FF));
            characters.put("Cyrillic Supplement", generateRange(0x0500, 0x052F));
            characters.put("Cyrillic Extended-A", generateRange(0x2DE0, 0x2DFF));
            characters.put("Cyrillic Extended-B", generateRange(0xA640, 0xA69F));
            characters.put("Cyrillic Extended C", generateRange(0x1C80, 0x1C8F));
            characters.put("Armenian", generateRange(0x0530, 0x058F));
            characters.put("Hebrew", generateRange(0x0590, 0x05FF));
            characters.put("Arabic", generateRange(0x0600, 0x06FF));
            characters.put("Syriac", generateRange(0x0700, 0x074F));
            characters.put("Arabic Supplement", generateRange(0x0750, 0x077F));
            characters.put("Thaana", generateRange(0x0780, 0x07BF));
            characters.put("NKo", generateRange(0x07C0, 0x07FF));
            characters.put("Samaritan", generateRange(0x0800, 0x083F));
            characters.put("Mandaic", generateRange(0x0840, 0x085F));
            characters.put("Syriac Supplement", generateRange(0x0860, 0x086F));
            characters.put("Arabic Extended-A", generateRange(0x08A0, 0x08FF));
            characters.put("Devanagari", generateRange(0x0900, 0x097F));
            characters.put("Bengali", generateRange(0x0980, 0x09FF));
            characters.put("Gurmukhi", generateRange(0x0A00, 0x0A7F));
            characters.put("Gujarati", generateRange(0x0A80, 0x0AFF));
            characters.put("Oriya", generateRange(0x0B00, 0x0B7F));
            characters.put("Tamil", generateRange(0x0B80, 0x0BFF));
            characters.put("Telugu", generateRange(0x0C00, 0x0C7F));
            characters.put("Kannada", generateRange(0x0C80, 0x0CFF));
            characters.put("Malayalam", generateRange(0x0D00, 0x0D7F));
            characters.put("Sinhala", generateRange(0x0D80, 0x0DFF));
            characters.put("Thai", generateRange(0x0E00, 0x0E7F));
            characters.put("Lao", generateRange(0x0E80, 0x0EFF));
            characters.put("Tibetan", generateRange(0x0F00, 0x0FFF));
            characters.put("Myanmar", generateRange(0x1000, 0x109F));
            characters.put("Georgian", generateRange(0x10A0, 0x10FF));
            characters.put("Hangul Jamo", generateRange(0x1100, 0x11FF));
            characters.put("Ethiopic", generateRange(0x1200, 0x137F));
            characters.put("Ethiopic Supplement", generateRange(0x1380, 0x139F));
            characters.put("Cherokee", generateRange(0x13A0, 0x13FF));
            characters.put("Unified Canadian Aboriginal Syllabics", generateRange(0x1400, 0x167F));
            characters.put("Ogham", generateRange(0x1680, 0x169F));
            characters.put("Runic", generateRange(0x16A0, 0x16FF));
            characters.put("Tagalog", generateRange(0x1700, 0x171F));
            characters.put("Hanunoo", generateRange(0x1720, 0x173F));
            characters.put("Buhid", generateRange(0x1740, 0x175F));
            characters.put("Tagbanwa", generateRange(0x1760, 0x177F));
            characters.put("Khmer", generateRange(0x1780, 0x17FF));
            characters.put("Mongolian", generateRange(0x1800, 0x18AF));
            characters.put("Unified Canadian Aboriginal Syllabics Extended", generateRange(0x18B0, 0x18FF));
            characters.put("Limbu", generateRange(0x1900, 0x194F));
            characters.put("Tai Le", generateRange(0x1950, 0x197F));
            characters.put("New Tai Lue", generateRange(0x1980, 0x19DF));
            characters.put("Khmer Symbols", generateRange(0x19E0, 0x19FF));
            characters.put("Buginese", generateRange(0x1A00, 0x1A1F));
            characters.put("Tai Tham", generateRange(0x1A20, 0x1AAF));
            characters.put("Combining Diacritical Marks Extended", generateRange(0x1AB0, 0x1AFF));
            characters.put("Balinese", generateRange(0x1B00, 0x1B7F));
            characters.put("Sundanese", generateRange(0x1B80, 0x1BBF));
            characters.put("Batak", generateRange(0x1BC0, 0x1BFF));
            characters.put("Lepcha", generateRange(0x1C00, 0x1C4F));
            characters.put("Ol Chiki", generateRange(0x1C50, 0x1C7F));
            characters.put("Sundanese Supplement", generateRange(0x1CC0, 0x1CCF));
            characters.put("Vedic Extensions", generateRange(0x1CD0, 0x1CFF));
            characters.put("Phonetic Extensions", generateRange(0x1D00, 0x1D7F));
            characters.put("Phonetic Extensions Supplement", generateRange(0x1D80, 0x1DBF));
            characters.put("Combining Diacritical Marks Supplement", generateRange(0x1DC0, 0x1DFF));
            characters.put("Latin Extended Additional", generateRange(0x1E00, 0x1EFF));
            characters.put("Greek Extended", generateRange(0x1F00, 0x1FFF));
            characters.put("Superscripts and Subscripts", generateRange(0x2070, 0x209F));
            characters.put("Combining Diacritical Marks for Symbols", generateRange(0x20D0, 0x20FF));
            characters.put("Letterlike Symbols", generateRange(0x2100, 0x214F));
            characters.put("Number Forms", generateRange(0x2150, 0x218F));
            characters.put("Arrows", generateRange(0x2190, 0x21FF));
            characters.put("Miscellaneous Technical", generateRange(0x2300, 0x23FF));
            characters.put("Control Pictures", generateRange(0x2400, 0x243F));
            characters.put("Optical Character Recognition", generateRange(0x2440, 0x245F));
            characters.put("Enclosed Alphanumerics", generateRange(0x2460, 0x24FF));
            characters.put("Dingbats", generateRange(0x2700, 0x27BF));
            characters.put("Braille Patterns", generateRange(0x2800, 0x28FF));
            characters.put("Glagolitic", generateRange(0x2C00, 0x2C5F));
            characters.put("Latin Extended-C", generateRange(0x2C60, 0x2C7F));
            characters.put("Coptic", generateRange(0x2C80, 0x2CFF));
            characters.put("Georgian Supplement", generateRange(0x2D00, 0x2D2F));
            characters.put("Tifinagh", generateRange(0x2D30, 0x2D7F));
            characters.put("Ethiopic Extended", generateRange(0x2D80, 0x2DDF));
            characters.put("CJK Radicals Supplement", generateRange(0x2E80, 0x2EFF));
            characters.put("Kangxi Radicals", generateRange(0x2F00, 0x2FDF));
            characters.put("Ideographic Description Characters", generateRange(0x2FF0, 0x2FFF));
            characters.put("CJK Symbols and Punctuation", generateRange(0x3000, 0x303F));
            characters.put("Hiragana", generateRange(0x3040, 0x309F));
            characters.put("Katakana", generateRange(0x30A0, 0x30FF));
            characters.put("Bopomofo", generateRange(0x3100, 0x312F));
            characters.put("Hangul Compatibility Jamo", generateRange(0x3130, 0x318F));
            characters.put("Kanbun", generateRange(0x3190, 0x319F));
            characters.put("Bopomofo Extended", generateRange(0x31A0, 0x31BF));
            characters.put("CJK Strokes", generateRange(0x31C0, 0x31EF));
            characters.put("Katakana Phonetic Extensions", generateRange(0x31F0, 0x31FF));
            characters.put("Enclosed CJK Letters and Months", generateRange(0x3200, 0x32FF));
            characters.put("CJK Compatibility", generateRange(0x3300, 0x33FF));
            characters.put("CJK Unified Ideographs Extension A", generateRange(0x3400, 0x4DBF));
            characters.put("Yijing Hexagram Symbols", generateRange(0x4DC0, 0x4DFF));
            characters.put("CJK Unified Ideographs", generateRange(0x4E00, 0x9FFF));
            characters.put("Yi Syllables", generateRange(0xA000, 0xA48F));
            characters.put("Yi Radicals", generateRange(0xA490, 0xA4CF));
            characters.put("Lisu", generateRange(0xA4D0, 0xA4FF));
            characters.put("Vai", generateRange(0xA500, 0xA63F));
            characters.put("Bamum", generateRange(0xA6A0, 0xA6FF));
            characters.put("Modifier Tone Letters", generateRange(0xA700, 0xA71F));
            characters.put("Latin Extended-D", generateRange(0xA720, 0xA7FF));
            characters.put("Syloti Nagri", generateRange(0xA800, 0xA82F));
            characters.put("Common Indic Number Forms", generateRange(0xA830, 0xA83F));
            characters.put("Phags-pa", generateRange(0xA840, 0xA87F));
            characters.put("Saurashtra", generateRange(0xA880, 0xA8DF));
            characters.put("Devanagari Extended", generateRange(0xA8E0, 0xA8FF));
            characters.put("Kayah Li", generateRange(0xA900, 0xA92F));
            characters.put("Rejang", generateRange(0xA930, 0xA95F));
            characters.put("Hangul Jamo Extended-A", generateRange(0xA960, 0xA97F));
            characters.put("Javanese", generateRange(0xA980, 0xA9DF));
            characters.put("Myanmar Extended-B", generateRange(0xA9E0, 0xA9FF));
            characters.put("Cham", generateRange(0xAA00, 0xAA5F));
            characters.put("Myanmar Extended-A", generateRange(0xAA60, 0xAA7F));
            characters.put("Tai Viet", generateRange(0xAA80, 0xAADF));
            characters.put("Meetei Mayek Extensions", generateRange(0xAAE0, 0xAAFF));
            characters.put("Ethiopic Extended-A", generateRange(0xAB00, 0xAB2F));
            characters.put("Latin Extended-E", generateRange(0xAB30, 0xAB6F));
            characters.put("Cherokee Supplement", generateRange(0xAB70, 0xABBF));
            characters.put("Meetei Mayek", generateRange(0xABC0, 0xABFF));
            characters.put("Hangul Syllables", generateRange(0xAC00, 0xD7AF));
            characters.put("Hangul Jamo Extended-B", generateRange(0xD7B0, 0xD7FF));
            characters.put("High Surrogates", generateRange(0xD800, 0xDB7F));
            characters.put("High Private Use Surrogates", generateRange(0xDB80, 0xDBFF));
            characters.put("Low Surrogates", generateRange(0xDC00, 0xDFFF));
            characters.put("Private Use Area", generateRange(0xE000, 0xF8FF));
            characters.put("CJK Compatibility Ideographs", generateRange(0xF900, 0xFAFF));
            characters.put("Alphabetic Presentation Forms", generateRange(0xFB00, 0xFB4F));
            characters.put("Arabic Presentation Forms-A", generateRange(0xFB50, 0xFDFF));
            characters.put("Variation Selectors", generateRange(0xFE00, 0xFE0F));
            characters.put("Vertical Forms", generateRange(0xFE10, 0xFE1F));
            characters.put("Combining Half Marks", generateRange(0xFE20, 0xFE2F));
            characters.put("CJK Compatibility Forms", generateRange(0xFE30, 0xFE4F));
            characters.put("Small Form Variants", generateRange(0xFE50, 0xFE6F));
            characters.put("Arabic Presentation Forms-B", generateRange(0xFE70, 0xFEFF));
            characters.put("Halfwidth and Fullwidth Forms", generateRange(0xFF00, 0xFFEF));
            characters.put("Specials", generateRange(0xFFF0, 0xFFFF));
            characters.put("Linear B Syllabary", generateRange(0x10000, 0x1007F));
            characters.put("Linear B Ideograms", generateRange(0x10080, 0x100FF));
            characters.put("Aegean Numbers", generateRange(0x10100, 0x1013F));
            characters.put("Ancient Greek Numbers", generateRange(0x10140, 0x1018F));
            characters.put("Ancient Symbols", generateRange(0x10190, 0x101CF));
            characters.put("Phaistos Disc", generateRange(0x101D0, 0x101FF));
            characters.put("Lycian", generateRange(0x10280, 0x1029F));
            characters.put("Carian", generateRange(0x102A0, 0x102DF));
            characters.put("Coptic Epact Numbers", generateRange(0x102E0, 0x102FF));
            characters.put("Old Italic", generateRange(0x10300, 0x1032F));
            characters.put("Gothic", generateRange(0x10330, 0x1034F));
            characters.put("Old Permic", generateRange(0x10350, 0x1037F));
            characters.put("Ugaritic", generateRange(0x10380, 0x1039F));
            characters.put("Old Persian", generateRange(0x103A0, 0x103DF));
            characters.put("Deseret", generateRange(0x10400, 0x1044F));
            characters.put("Shavian", generateRange(0x10450, 0x1047F));
            characters.put("Osmanya", generateRange(0x10480, 0x104AF));
            characters.put("Osage", generateRange(0x104B0, 0x104FF));
            characters.put("Elbasan", generateRange(0x10500, 0x1052F));
            characters.put("Caucasian Albanian", generateRange(0x10530, 0x1056F));
            characters.put("Linear A", generateRange(0x10600, 0x1077F));
            characters.put("Cypriot Syllabary", generateRange(0x10800, 0x1083F));
            characters.put("Imperial Aramaic", generateRange(0x10840, 0x1085F));
            characters.put("Palmyrene", generateRange(0x10860, 0x1087F));
            characters.put("Nabataean", generateRange(0x10880, 0x108AF));
            characters.put("Hatran", generateRange(0x108E0, 0x108FF));
            characters.put("Phoenician", generateRange(0x10900, 0x1091F));
            characters.put("Lydian", generateRange(0x10920, 0x1093F));
            characters.put("Meroitic Hieroglyphs", generateRange(0x10980, 0x1099F));
            characters.put("Meroitic Cursive", generateRange(0x109A0, 0x109FF));
            characters.put("Kharoshthi", generateRange(0x10A00, 0x10A5F));
            characters.put("Old South Arabian", generateRange(0x10A60, 0x10A7F));
            characters.put("Old North Arabian", generateRange(0x10A80, 0x10A9F));
            characters.put("Manichaean", generateRange(0x10AC0, 0x10AFF));
            characters.put("Avestan", generateRange(0x10B00, 0x10B3F));
            characters.put("Inscriptional Parthian", generateRange(0x10B40, 0x10B5F));
            characters.put("Inscriptional Pahlavi", generateRange(0x10B60, 0x10B7F));
            characters.put("Psalter Pahlavi", generateRange(0x10B80, 0x10BAF));
            characters.put("Old Turkic", generateRange(0x10C00, 0x10C4F));
            characters.put("Old Hungarian", generateRange(0x10C80, 0x10CFF));
            characters.put("Rumi Numeral Symbols", generateRange(0x10E60, 0x10E7F));
            characters.put("Brahmi", generateRange(0x11000, 0x1107F));
            characters.put("Kaithi", generateRange(0x11080, 0x110CF));
            characters.put("Sora Sompeng", generateRange(0x110D0, 0x110FF));
            characters.put("Chakma", generateRange(0x11100, 0x1114F));
            characters.put("Mahajani", generateRange(0x11150, 0x1117F));
            characters.put("Sharada", generateRange(0x11180, 0x111DF));
            characters.put("Sinhala Archaic Numbers", generateRange(0x111E0, 0x111FF));
            characters.put("Khojki", generateRange(0x11200, 0x1124F));
            characters.put("Multani", generateRange(0x11280, 0x112AF));
            characters.put("Khudawadi", generateRange(0x112B0, 0x112FF));
            characters.put("Grantha", generateRange(0x11300, 0x1137F));
            characters.put("Newa", generateRange(0x11400, 0x1147F));
            characters.put("Tirhuta", generateRange(0x11480, 0x114DF));
            characters.put("Siddham", generateRange(0x11580, 0x115FF));
            characters.put("Modi", generateRange(0x11600, 0x1165F));
            characters.put("Mongolian Supplement", generateRange(0x11660, 0x1167F));
            characters.put("Takri", generateRange(0x11680, 0x116CF));
            characters.put("Ahom", generateRange(0x11700, 0x1173F));
            characters.put("Warang Citi", generateRange(0x118A0, 0x118FF));
            characters.put("Zanabazar Square", generateRange(0x11A00, 0x11A4F));
            characters.put("Soyombo", generateRange(0x11A50, 0x11AAF));
            characters.put("Pau Cin Hau", generateRange(0x11AC0, 0x11AFF));
            characters.put("Bhaiksuki", generateRange(0x11C00, 0x11C6F));
            characters.put("Marchen", generateRange(0x11C70, 0x11CBF));
            characters.put("Masaram Gondi", generateRange(0x11D00, 0x11D5F));
            characters.put("Cuneiform", generateRange(0x12000, 0x123FF));
            characters.put("Cuneiform Numbers and Punctuation", generateRange(0x12400, 0x1247F));
            characters.put("Early Dynastic Cuneiform", generateRange(0x12480, 0x1254F));
            characters.put("Egyptian Hieroglyphs", generateRange(0x13000, 0x1342F));
            characters.put("Anatolian Hieroglyphs", generateRange(0x14400, 0x1467F));
            characters.put("Bamum Supplement", generateRange(0x16800, 0x16A3F));
            characters.put("Mro", generateRange(0x16A40, 0x16A6F));
            characters.put("Bassa Vah", generateRange(0x16AD0, 0x16AFF));
            characters.put("Pahawh Hmong", generateRange(0x16B00, 0x16B8F));
            characters.put("Miao", generateRange(0x16F00, 0x16F9F));
            characters.put("Ideographic Symbols and Punctuation", generateRange(0x16FE0, 0x16FFF));
            characters.put("Tangut", generateRange(0x17000, 0x187FF));
            characters.put("Tangut Components", generateRange(0x18800, 0x18AFF));
            characters.put("Kana Supplement", generateRange(0x1B000, 0x1B0FF));
            characters.put("Kana Extended-A", generateRange(0x1B100, 0x1B12F));
            characters.put("Nushu", generateRange(0x1B170, 0x1B2FF));
            characters.put("Duployan", generateRange(0x1BC00, 0x1BC9F));
            characters.put("Shorthand Format Controls", generateRange(0x1BCA0, 0x1BCAF));
            characters.put("Byzantine Musical Symbols", generateRange(0x1D000, 0x1D0FF));
            characters.put("Musical Symbols", generateRange(0x1D100, 0x1D1FF));
            characters.put("Ancient Greek Musical Notation", generateRange(0x1D200, 0x1D24F));
            characters.put("Tai Xuan Jing Symbols", generateRange(0x1D300, 0x1D35F));
            characters.put("Counting Rod Numerals", generateRange(0x1D360, 0x1D37F));
            characters.put("Sutton SignWriting", generateRange(0x1D800, 0x1DAAF));
            characters.put("Glagolitic Supplement", generateRange(0x1E000, 0x1E02F));
            characters.put("Mende Kikakui", generateRange(0x1E800, 0x1E8DF));
            characters.put("Adlam", generateRange(0x1E900, 0x1E95F));
            characters.put("Arabic Mathematical Alphabetic Symbols", generateRange(0x1EE00, 0x1EEFF));
            characters.put("Mahjong Tiles", generateRange(0x1F000, 0x1F02F));
            characters.put("Domino Tiles", generateRange(0x1F030, 0x1F09F));
            characters.put("Playing Cards", generateRange(0x1F0A0, 0x1F0FF));
            characters.put("Enclosed Alphanumeric Supplement", generateRange(0x1F100, 0x1F1FF));
            characters.put("Enclosed Ideographic Supplement", generateRange(0x1F200, 0x1F2FF));
            characters.put("Miscellaneous Symbols and Pictographs", generateRange(0x1F300, 0x1F5FF));
            characters.put("Emoticons (Emoji)", generateRange(0x1F600, 0x1F64F));
            characters.put("Ornamental Dingbats", generateRange(0x1F650, 0x1F67F));
            characters.put("Transport and Map Symbols", generateRange(0x1F680, 0x1F6FF));
            characters.put("Alchemical Symbols", generateRange(0x1F700, 0x1F77F));
            characters.put("Geometric Shapes Extended", generateRange(0x1F780, 0x1F7FF));
            characters.put("CJK Unified Ideographs Extension B", generateRange(0x20000, 0x2A6DF));
            characters.put("CJK Unified Ideographs Extension C", generateRange(0x2A700, 0x2B73F));
            characters.put("CJK Unified Ideographs Extension D", generateRange(0x2B740, 0x2B81F));
            characters.put("CJK Unified Ideographs Extension E", generateRange(0x2B820, 0x2CEAF));
            characters.put("CJK Unified Ideographs Extension F", generateRange(0x2CEB0, 0x2EBEF));
            characters.put("CJK Compatibility Ideographs Supplement", generateRange(0x2F800, 0x2FA1F));
            characters.put("Tags", generateRange(0xE0000, 0xE007F));
            characters.put("Variation Selectors Supplement", generateRange(0xE0100, 0xE01EF));
        }

        private static List<Pair<String, String>> generateRange(int start, int end) {
            if (start >= end) {
                return Collections.emptyList();
            }
            List<Pair<String, String>> result = new ArrayList<>(end - start);
            for (int i = start; i <= end; i++) {
                String representation = new String(Character.toChars(i));
                String value = String.format("U%04X", i);
                result.add(new Pair<>(representation, value));

            }
            return result;
        }

        static Set<String> categories() {
            return characters.keySet();
        }

        static List<Pair<String, String>> getCharacters(String category) {
            return characters.get(category);
        }

    }
}
