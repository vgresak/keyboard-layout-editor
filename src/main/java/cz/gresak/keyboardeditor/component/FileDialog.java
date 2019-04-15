package cz.gresak.keyboardeditor.component;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class FileDialog {
    private final static FileChooser fileChooser = new FileChooser();

    static {
        File initialDir = new File(System.getProperty("user.home"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
    }

    public static File showSaveDialog(Window window) {
        return fileChooser.showSaveDialog(window);
    }
}
