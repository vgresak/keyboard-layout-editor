package cz.gresak.keyboardeditor.component;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URISyntaxException;

public class FileDialog {
    private final static FileChooser fileChooser = new FileChooser();

    static {
        fileChooser.setInitialFileName("custom");
        try {
            File sourceLocation = new File(FileDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (sourceLocation.exists()) {
                if (!sourceLocation.isDirectory()) {
                    // sourceLocation is JAR file, select parent dir
                    sourceLocation = sourceLocation.getAbsoluteFile().getParentFile();
                }
                fileChooser.setInitialDirectory(sourceLocation);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static File showSaveDialog(Window window) {
        return fileChooser.showSaveDialog(window);
    }
}
