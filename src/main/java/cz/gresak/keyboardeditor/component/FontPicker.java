package cz.gresak.keyboardeditor.component;

import cz.gresak.keyboardeditor.service.ServiceLoader;
import cz.gresak.keyboardeditor.service.api.FontProvider;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class FontPicker extends Dialog<Font> {
    private Font selectedFont;
    private Text sampleText;

    public FontPicker() {
        setTitle("Font");
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setContent(content());
        setResultConverter(this::resultConverter);
    }

    private Font resultConverter(ButtonType buttonType) {
        if (ButtonType.CANCEL.equals(buttonType)) {
            return null;
        }
        return selectedFont;
    }

    private Node content() {
        GridPane content = new GridPane();
        content.setVgap(10);
        content.setHgap(10);
        Text fontFamilyLabel = new Text("Font family");
        content.add(fontFamilyLabel, 0, 0);
        ComboBox<String> fontFamilyCombo = new ComboBox<>(FXCollections.observableList(Font.getFamilies()));
        SingleSelectionModel<String> fontFamilySelection = fontFamilyCombo.getSelectionModel();
        content.add(fontFamilyCombo, 1, 0);

        Text fontLabel = new Text("Font variant");
        content.add(fontLabel, 0, 1);
        ComboBox<String> fontCombo = new ComboBox<>();
        content.add(fontCombo, 1, 1);

        fontFamilyCombo.setOnAction(actionEvent -> {
            fontCombo.setItems(FXCollections.observableList(Font.getFontNames(fontFamilySelection.getSelectedItem())));
            fontCombo.getSelectionModel().selectFirst();
            updateSelectedFont(fontCombo);
        });
        fontCombo.setOnAction(actionEvent -> updateSelectedFont(fontCombo));

        Text infoText = new Text("This setting changes default font used to display all key symbols.\nWhen the default font cannot be used to display given symbol, application automatically tries to find substitute using fonts installed on the system.");
        content.add(infoText, 0, 2);
        GridPane.setColumnSpan(infoText, 2);

        sampleText = new Text("The quick brown fox jumps over the lazy dog");
        content.add(sampleText, 0, 3);
        GridPane.setColumnSpan(sampleText, 2);

        Font defaultFont = ServiceLoader.lookup(FontProvider.class).getDefaultFont();
        fontFamilySelection.select(defaultFont.getFamily());
        fontCombo.setItems(FXCollections.observableList(Font.getFontNames(fontFamilySelection.getSelectedItem())));
        fontCombo.getSelectionModel().select(defaultFont.getName());

        updateSelectedFont(fontCombo);

        return content;
    }

    private void updateSelectedFont(ComboBox<String> fontCombo) {
        selectedFont = new Font(fontCombo.getSelectionModel().getSelectedItem(), Font.getDefault().getSize());
        sampleText.setFont(new Font(selectedFont.getName(), 18));
    }
}
