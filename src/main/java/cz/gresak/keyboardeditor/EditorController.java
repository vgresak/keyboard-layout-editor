package cz.gresak.keyboardeditor;

import cz.gresak.keyboardeditor.component.CharacterMap;
import cz.gresak.keyboardeditor.component.FileDialog;
import cz.gresak.keyboardeditor.component.FontPicker;
import cz.gresak.keyboardeditor.component.Key;
import cz.gresak.keyboardeditor.component.WebsiteLink;
import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.model.Line;
import cz.gresak.keyboardeditor.model.ModelKey;
import cz.gresak.keyboardeditor.service.ServiceLoader;
import cz.gresak.keyboardeditor.service.api.FontProvider;
import cz.gresak.keyboardeditor.service.api.GroupState;
import cz.gresak.keyboardeditor.service.api.KeyboardModelLoader;
import cz.gresak.keyboardeditor.service.api.KeysymMapper;
import cz.gresak.keyboardeditor.service.api.LayoutExportResult;
import cz.gresak.keyboardeditor.service.api.LayoutExporter;
import cz.gresak.keyboardeditor.service.api.PredefinedKeyboardModel;
import cz.gresak.keyboardeditor.service.api.xkbconfig.Config;
import cz.gresak.keyboardeditor.service.api.xkbconfig.CurrentConfigLoader;
import cz.gresak.keyboardeditor.service.api.xkbconfig.KeyboardModelUpdater;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static cz.gresak.keyboardeditor.service.ServiceLoader.lookup;

public class EditorController implements Initializable {
    private static final int outerLeftMargin = 20;
    private static final int outerTopMargin = 20;
    private static final String XKB_SYMBOLS_DIRECTORY = "/usr/share/X11/xkb/symbols/";
    private static final String TYPE_NONE = "None";

    @FXML
    private Pane editorPane;
    @FXML
    private TextField txtValue;
    @FXML
    private Button btnCharMap;
    @FXML
    private Text lblKeycode;
    @FXML
    private ToggleGroup keyboardModelGroup;
    @FXML
    private ToggleGroup keyboardGroupGroup;
    @FXML
    private ComboBox<String> comboLevel;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<String> comboSpecial;
    @FXML
    private CheckMenuItem checkLevel1;
    @FXML
    private CheckMenuItem checkLevel2;
    @FXML
    private CheckMenuItem checkLevel3;
    @FXML
    private CheckMenuItem checkLevel4;
    @FXML
    private CheckMenuItem checkShowNoSymbol;

    private Config config;
    private KeyboardModel model;
    private Map<List<Key>, Double> keyLines;
    private Key selectedKey;
    private CharacterMap characterMap = new CharacterMap();
    private boolean handleComboSpecialInput;

    private KeyboardModelUpdater modelUpdater = lookup(KeyboardModelUpdater.class);
    private GroupState groupState = lookup(GroupState.class);
    private KeyboardModelLoader modelLoader = lookup(KeyboardModelLoader.class);
    private CurrentConfigLoader currentConfigLoader = lookup(CurrentConfigLoader.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = modelLoader.load(PredefinedKeyboardModel.ALPHABETICAL);
        config = currentConfigLoader.getCurrentConfig();
        modelUpdater.updateModel(model, config);
        loadKeys();
        registerSizeChangedListener();
        initLevelCombo();
        initTypesCombo();
        initSpecialCombo();
        keyboardGroupGroup.getToggles().get(groupState.getGroup() - 1).setSelected(true);
    }

    private void loadKeys() {
        editorPane.getChildren().clear();
        keyLines = new LinkedHashMap<>();
        for (Line line : model.getLines()) {
            List<Key> keyLine = new ArrayList<>();
            keyLines.put(keyLine, line.getMarginTop());
            for (ModelKey key : line.getKeys()) {
                Key keyComponent = new Key(key);
                keyComponent.setOnMouseClicked(event -> keyClicked(keyComponent));
                keyLine.add(keyComponent);
                editorPane.getChildren().add(keyComponent);
            }
        }
    }

    private void registerSizeChangedListener() {
        ChangeListener<Number> sizeChangedListener = (observable, oldValue, newValue) -> layoutKeys();
        editorPane.heightProperty().addListener(sizeChangedListener);
        editorPane.widthProperty().addListener(sizeChangedListener);
    }

    private void initLevelCombo() {
        ObservableList<String> levels = FXCollections.observableArrayList();
        //4 levels
        for (int i = 1; i <= 4; i++) {
            levels.add("Level " + i);
        }
        comboLevel.setItems(levels);
        comboLevel.getSelectionModel().selectFirst();
    }

    private void initTypesCombo() {
        ObservableList<String> types = FXCollections.observableArrayList(config.getTypes());
        types.add(0, TYPE_NONE);
        comboType.setItems(types);
    }

    private void initSpecialCombo() {
        ObservableList<String> specialKeysyms = FXCollections.observableArrayList(
                "Shift_L", "Shift_R", "Control_L", "Control_R", "Caps_Lock", "Shift_Lock",
                "Meta_L", "Meta_R", "Alt_L", "Alt_R", "Super_L", "Super_R", "Hyper_L", "Hyper_R",
                "ISO_Next_Group", "ISO_Prev_Group", "ISO_First_Group", "ISO_Last_Group", "ISO_Level3_Shift",
                "ISO_Lock", "ISO_Level2_Latch", "ISO_Level3_Latch", "ISO_Level3_Lock", "ISO_Level5_Shift", "ISO_Level5_Latch",
                "ISO_Level5_Lock", "ISO_Group_Shift", "ISO_Group_Latch", "ISO_Group_Lock",
                "ISO_Next_Group_Lock", "ISO_Prev_Group_Lock", "ISO_First_Group_Lock", "ISO_Last_Group_Lock",
                "NoSymbol", "BackSpace", "Tab", "ISO_Left_Tab", "Linefeed", "Return", "Clear", "Pause",
                "Num_Lock", "Scroll_Lock", "Sys_Req", "Escape", "Delete", "Multi_key",
                "Home", "End", "Left", "Right", "Up", "Down", "Page_Up", "Page_Down",
                "Begin", "Select", "Print", "Execute", "Insert", "Undo", "Redo",
                "Menu", "Find", "Cancel", "Help", "Break"
        );
        comboSpecial.setItems(specialKeysyms);
    }

    private void layoutKeys() {
        double paneWidth = editorPane.getWidth();
        double paneHeight = editorPane.getHeight();

        double maxLineWidth = maxLineWidth(model);
        double linesHeight = linesHeight(model);

        double keyWidthUnit = ((paneWidth - 2 * outerLeftMargin) / maxLineWidth);
        double keyHeightUnit = ((paneHeight - 2 * outerTopMargin) / linesHeight);

        double y = outerTopMargin;
        for (Map.Entry<List<Key>, Double> entry : keyLines.entrySet()) {
            double x = outerLeftMargin;
            for (Key key : entry.getKey()) {
                ModelKey modelKey = key.getKey();
                double keyWidth = keyWidthUnit * modelKey.getWidth();
                double keyHeight = keyHeightUnit * modelKey.getHeight();
                key.setPrefWidth(keyWidth);
                key.setPrefHeight(keyHeight);
                key.setLayoutX(x + modelKey.getMarginLeft() * keyWidthUnit);
                key.setLayoutY(y + entry.getValue() * keyHeightUnit);
                x += keyWidth + modelKey.getMarginLeft() * keyWidthUnit;
            }
            if (y != outerTopMargin) {
                y += keyHeightUnit * entry.getValue();
            }
            y += keyHeightUnit;
        }
    }

    private double linesHeight(KeyboardModel layout) {
        return layout.getLines().stream().mapToDouble(value -> value.getMarginTop() + 1).sum();
    }

    private double maxLineWidth(KeyboardModel layout) {
        return layout.getLines().stream()
                .mapToDouble(line ->
                        line.getKeys().stream()
                                .mapToDouble(key -> key.getWidth() + key.getMarginLeft())
                                .sum())
                .max()
                .orElse(0);
    }

    public void loadCustomKeyboardModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load keyboard model");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file", "*.json"));
        Stage stage = (Stage) editorPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        model = modelLoader.load(file);
        updateModel();
    }

    public void selectKeyboardModel() {
        PredefinedKeyboardModel selectedModel = PredefinedKeyboardModel.of(String.valueOf(keyboardModelGroup.getSelectedToggle().getUserData()));
        model = modelLoader.load(selectedModel);
        updateModel();
    }

    private void updateModel() {
        modelUpdater.updateModel(model, config);
        loadKeys();
        layoutKeys();
    }

    public void comboLevelChanged() {
        updateSelectedKeyLabels();
        setSpecialComboBoxToCurrentKey();
    }

    private void keyClicked(Key key) {
        if (selectedKey != null) {
            selectedKey.unselect();
        }
        key.select();
        selectedKey = key;
        updateSelectedKeyLabels();
        setSpecialComboBoxToCurrentKey();
    }

    private void setSpecialComboBoxToCurrentKey() {
        int levelIndex = comboLevel.getSelectionModel().getSelectedIndex();
        int group = groupState.getGroup();
        String selectedValue = selectedKey.getKey().getValue(group, levelIndex);
        handleComboSpecialInput = false;
        SelectionModel<String> selectionModel = comboSpecial.getSelectionModel();
        selectionModel.clearSelection();
        comboSpecial.getItems().stream()
                .filter(selectedValue::equals)
                .findFirst()
                .ifPresent(selectionModel::select);
        handleComboSpecialInput = true;
    }

    private void updateSelectedKeyLabels() {
        if (selectedKey == null) {
            lblKeycode.setText("");
            disableEditation();
            return;
        }
        ModelKey key = selectedKey.getKey();
        String keycode = key.getKeycode();
        int group = groupState.getGroup();
        int levelIndex = comboLevel.getSelectionModel().getSelectedIndex();
        String value = key.getValue(group, levelIndex);
        txtValue.setText(value);
        lblKeycode.setText(keycode);
        setEditationDisabled(false);
        String keyType = key.getType(group);
        if (StringUtils.isBlank(keyType)) {
            comboType.getSelectionModel().select(TYPE_NONE);
        } else {
            comboType.getSelectionModel().select(keyType);
        }
    }

    private void setEditationDisabled(boolean b) {
        txtValue.setDisable(b);
        btnCharMap.setDisable(b);
        comboType.setDisable(b);
        comboSpecial.setDisable(b);
    }

    private void disableEditation() {
        setEditationDisabled(true);
    }

    public void valueChanged() {
        if (selectedKey == null) {
            return;
        }
        setValue(txtValue.getText());
    }

    public void openCharacterMap() {
        if (selectedKey == null) {
            return;
        }
        Optional<String> dialogResult = characterMap.showAndWait();
        dialogResult.ifPresent(this::setValue);
    }

    private void setValue(String text) {
        int levelIndex = comboLevel.getSelectionModel().getSelectedIndex();
        int group = groupState.getGroup();
        selectedKey.setValue(text, levelIndex, group);
        updateSelectedKeyLabels();
    }

    public void exportLayout() {
        LayoutExporter exporter = lookup(LayoutExporter.class);
        Window window = editorPane.getScene().getWindow();
        File file = FileDialog.showSaveDialog(window);
        if (file != null) {
            LayoutExportResult exportResult = exporter.export(model, file);
            if (exportResult.isOk()) {
                showExportFinishedAlert(file);
            } else {
                showExportFailedAlert(file, exportResult);
            }
        }
    }

    private void showExportFinishedAlert(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard layout export complete");
        alert.setHeaderText("Keyboard layout export finished successfully");
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().add(new Text("To use exported layout immediately, run following commands:"));
        TextArea commands = new TextArea();
        commands.setEditable(false);
        String newSymbolsFileName = file.getName();
        String commandsText = String.format("sudo mv %s %s%s\nsetxkbmap -layout \"%s\"", file.getAbsolutePath(), XKB_SYMBOLS_DIRECTORY, newSymbolsFileName, newSymbolsFileName);
        commands.setText(commandsText);
        content.getChildren().add(commands);
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    private void showExportFailedAlert(File file, LayoutExportResult exportResult) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Saving failed.");
        alert.setContentText(String.format("Failed to export keyboard model to file %s", file));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exportResult.getException().printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    public void close() {
        Stage stage = (Stage) editorPane.getScene().getWindow();
        stage.close();
    }

    public void chooseDefaultFont() {
        FontPicker fontPicker = new FontPicker();
        Optional<Font> dialogResult = fontPicker.showAndWait();
        if (dialogResult.isPresent()) {
            Font font = dialogResult.get();
            FontProvider fontProvider = lookup(FontProvider.class);
            fontProvider.setDefaultFont(font);
        }
    }

    public void groupChanged() {
        int selectedGroup = Integer.parseInt((String) keyboardGroupGroup.getSelectedToggle().getUserData());
        groupState.setGroup(selectedGroup);
        unselectKey();
    }

    public void updateType() {
        if (selectedKey == null) {
            return;
        }
        String selectedItem = comboType.getSelectionModel().getSelectedItem();
        String type = selectedItem.equals(TYPE_NONE) ? null : selectedItem;
        selectedKey.getKey().setType(groupState.getGroup(), type);
    }

    public void loadCurrentLayout() {
        config = currentConfigLoader.getCurrentConfig();
        updateModel();
        unselectKey();
    }

    private void unselectKey() {
        if (selectedKey != null) {
            selectedKey.unselect();
        }
        selectedKey = null;
        disableEditation();
    }

    public void updateShownLevels() {
        keyLines.keySet().forEach(keys -> keys.forEach(
                key -> key.updateKeyConfig(
                        new Key.KeyConfig(checkLevel1.isSelected(), checkLevel2.isSelected(), checkLevel3.isSelected(), checkLevel4.isSelected())
                ))
        );
    }

    public void selectSpecialKeysym() {
        if (handleComboSpecialInput) {
            String selectedItem = comboSpecial.getSelectionModel().getSelectedItem();
            setValue(selectedItem);
        }
    }

    public void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About this application");
        VBox content = new VBox();
        content.getChildren().add(new Text("Keyboard layout editor is an application used to help create custom keyboard layouts.\n" +
                "Application works with X Keyboard Extension (XKB) – it loads current keyboard layout and it is able to export symbols file.\n\n" +
                "Author: Viktor Grešák\n" +
                "Created in 2019\n\n" +
                "Third party software used:"));
        content.getChildren().add(new WebsiteLink("Gson 2.8.5", "https://github.com/google/gson"));
        content.getChildren().add(new WebsiteLink("Apache Commons Exec 1.3", "https://commons.apache.org/proper/commons-exec"));
        content.getChildren().add(new WebsiteLink("Apache Commons Lang 3.8.1", "https://commons.apache.org/proper/commons-lang"));
        content.getChildren().add(new WebsiteLink("Apache Commons IO 2.6", "https://commons.apache.org/proper/commons-io"));
        content.getChildren().add(new WebsiteLink("Gradle 5.2.1", "https://gradle.org"));
        content.getChildren().add(new FlowPane(new Text("All software used is licensed under "), new WebsiteLink("Apache License 2.0", "http://www.apache.org/licenses/LICENSE-2.0")));
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    public void setShowNoSymbol() {
        ServiceLoader.lookup(KeysymMapper.class).showNoSymbol(checkShowNoSymbol.isSelected());
    }
}
