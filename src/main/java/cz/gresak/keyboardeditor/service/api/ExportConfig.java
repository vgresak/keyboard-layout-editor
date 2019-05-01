package cz.gresak.keyboardeditor.service.api;

import java.util.Arrays;

public class ExportConfig {
    private boolean exportOnlySelectedGroup;
    private boolean[] groupsToExport;
    private boolean exportType;
    private boolean showSettingsOnExport;

    public ExportConfig(ExportConfig exportConfig) {
        this();
        if (exportConfig != null) {
            this.exportOnlySelectedGroup = exportConfig.exportOnlySelectedGroup;
            this.exportType = exportConfig.exportType;
            this.groupsToExport = Arrays.copyOf(exportConfig.groupsToExport, exportConfig.groupsToExport.length);
            this.showSettingsOnExport = exportConfig.showSettingsOnExport;
        }
    }

    public ExportConfig() {
        this.exportOnlySelectedGroup = true;
        this.exportType = true;
        this.groupsToExport = new boolean[8];
        this.showSettingsOnExport = true;
    }

    public boolean isExportOnlySelectedGroup() {
        return exportOnlySelectedGroup;
    }

    public ExportConfig setExportOnlySelectedGroup(boolean exportOnlySelectedGroup) {
        this.exportOnlySelectedGroup = exportOnlySelectedGroup;
        return this;
    }

    public boolean[] getGroupsToExport() {
        return groupsToExport;
    }

    public ExportConfig setGroupsToExport(boolean[] groupsToExport) {
        this.groupsToExport = groupsToExport;
        return this;
    }

    public boolean canGroupBeExported(int group) {
        if (group < 1 || group > 8) {
            return false;
        }
        return groupsToExport[group - 1];
    }

    public ExportConfig setGroupToExport(int index, boolean value) {
        this.groupsToExport[index] = value;
        return this;
    }

    public boolean isExportType() {
        return exportType;
    }

    public ExportConfig setExportType(boolean exportType) {
        this.exportType = exportType;
        return this;
    }

    public boolean isShowSettingsOnExport() {
        return showSettingsOnExport;
    }

    public ExportConfig setShowSettingsOnExport(boolean showSettingsOnExport) {
        this.showSettingsOnExport = showSettingsOnExport;
        return this;
    }
}
