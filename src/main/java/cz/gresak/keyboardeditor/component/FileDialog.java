package cz.gresak.keyboardeditor.component;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class FileDialog {
    private final static FileChooser fileChooser = new FileChooser();

    static {
        fileChooser.setInitialFileName("custom");
        File initialDir = new File(System.getProperty("user.dir"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
    }

    public static File showSaveDialog(Window window) {
        File chosenFile = fileChooser.showSaveDialog(window);
        if (chosenFile != null) {
            fileChooser.setInitialDirectory(chosenFile.getParentFile());
            fileChooser.setInitialFileName(chosenFile.getName());
        }
        return chosenFile;
    }
}
