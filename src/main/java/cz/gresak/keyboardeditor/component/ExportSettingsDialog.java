package cz.gresak.keyboardeditor.component;

import cz.gresak.keyboardeditor.service.api.ExportConfig;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExportSettingsDialog extends Dialog<ExportConfig> {
    private ExportConfig config;
    private ToggleGroup exportSelectedGroupToggle;
    private CheckBox checkExportType;
    private RadioButton exportOnlySelectedGroup;
    private RadioButton exportSelectedGroups;
    private CheckBox checkShowDialog;
    private CheckBox[] groups = new CheckBox[8];

    public ExportSettingsDialog(ExportConfig currentConfig) {
        setTitle("Export settings");
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResultConverter(this::resultConverter);
        this.config = new ExportConfig(currentConfig);
        dialogPane.setContent(initContent());
        setValues();
    }

    private Node initContent() {
        checkExportType = new CheckBox("Export type");
        checkExportType.setOnAction(this::onExportTypeChanged);
        exportSelectedGroupToggle = new ToggleGroup();
        exportOnlySelectedGroup = new RadioButton("Export only currently selected group");
        exportOnlySelectedGroup.setUserData(true);
        exportOnlySelectedGroup.setOnAction(this::onExportOnlySelectedGroupsChanged);
        exportOnlySelectedGroup.setToggleGroup(exportSelectedGroupToggle);
        exportSelectedGroups = new RadioButton("Export following groups:");
        exportSelectedGroups.setUserData(false);
        exportSelectedGroups.setOnAction(this::onExportOnlySelectedGroupsChanged);
        exportSelectedGroups.setToggleGroup(exportSelectedGroupToggle);
        checkShowDialog = new CheckBox("Show this dialog before export");
        checkShowDialog.setOnAction(event -> config.setShowSettingsOnExport(checkShowDialog.isSelected()));
        HBox groupsRow1 = getFirstGroupsRow();
        HBox groupsRow2 = getSecondGroupsRow();
        VBox content = new VBox(
                checkExportType,
                new Separator(Orientation.HORIZONTAL),
                exportOnlySelectedGroup,
                exportSelectedGroups,
                groupsRow1,
                groupsRow2,
                new Separator(Orientation.HORIZONTAL),
                checkShowDialog
        );
        content.setPrefWidth(400);
        content.setSpacing(10);
        content.setPadding(new Insets(10));
        return content;
    }

    private void setValues() {
        checkExportType.setSelected(config.isExportType());
        exportSelectedGroupToggle.selectToggle(config.isExportOnlySelectedGroup() ? exportOnlySelectedGroup : exportSelectedGroups);
        boolean[] groupsToExport = config.getGroupsToExport();
        for (int index = 0; index < groups.length; index++) {
            CheckBox checkBox = groups[index];
            checkBox.setSelected(groupsToExport[index]);
            checkBox.setDisable(config.isExportOnlySelectedGroup());
        }
        checkShowDialog.setSelected(config.isShowSettingsOnExport());
    }

    private HBox getFirstGroupsRow() {
        CheckBox[] checkBoxes = generateGroups(0, 4);
        System.arraycopy(checkBoxes, 0, groups, 0, 4);
        return new HBox(10, checkBoxes);
    }

    private HBox getSecondGroupsRow() {
        CheckBox[] checkBoxes = generateGroups(4, 4);
        System.arraycopy(checkBoxes, 0, groups, 4, 4);
        return new HBox(10, checkBoxes);
    }

    private CheckBox[] generateGroups(int startingNumber, int count) {
        CheckBox[] result = new CheckBox[count];
        for (int i = startingNumber; i < startingNumber + count; i++) {
            CheckBox checkBox = new CheckBox(String.format("Group %d", i + 1));
            final int index = i;
            checkBox.setOnAction(event -> setGroupExported(checkBox, index));
            result[i - startingNumber] = checkBox;
        }
        return result;
    }

    private void setGroupExported(CheckBox theGroup, int groupIndex) {
        config.setGroupToExport(groupIndex, theGroup.isSelected());

        updateOkButtonState();
    }

    private void updateOkButtonState() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(shouldOkBeDisabled());
    }

    private boolean shouldOkBeDisabled() {
        if (exportOnlySelectedGroup.isSelected()) {
            return false;
        }
        for (CheckBox group : groups) {
            if (group.isSelected()) {
                return false;
            }
        }
        return true;
    }

    private void onExportOnlySelectedGroupsChanged(ActionEvent event) {
        config.setExportOnlySelectedGroup((boolean) exportSelectedGroupToggle.getSelectedToggle().getUserData());
        for (CheckBox checkBox : groups) {
            checkBox.setDisable(config.isExportOnlySelectedGroup());
        }
        updateOkButtonState();
    }

    private void onExportTypeChanged(ActionEvent event) {
        config.setExportType(checkExportType.isSelected());
    }

    private ExportConfig resultConverter(ButtonType buttonType) {
        if (ButtonType.CANCEL.equals(buttonType)) {
            return null;
        }
        return config;
    }
}
