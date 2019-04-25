package cz.gresak.keyboardeditor.service.api;

import cz.gresak.keyboardeditor.model.KeyboardModel;

import java.io.File;

/**
 * Exports the layout to symbols file.
 */
public interface LayoutExporter {
    /**
     * Saves given keyboard model as specified output file. Model is saved in XKB symbols format. If specified file already exists, the file is overwritten.
     * <p>
     * Result describes whether the operation was successful or not.
     *
     * @param model         keyboard layout model to be exported
     * @param outputFile    output file containing exported model in XKB symbols format
     * @param exportConfig  configuration of export
     * @return export result describing the outcome of the export operation
     */
    LayoutExportResult export(KeyboardModel model, File outputFile, ExportConfig exportConfig);
}
