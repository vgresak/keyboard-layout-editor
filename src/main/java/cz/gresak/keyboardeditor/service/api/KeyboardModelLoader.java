package cz.gresak.keyboardeditor.service.api;

import cz.gresak.keyboardeditor.model.KeyboardModel;

import java.io.File;

/**
 * Loads keyboard model from JSON file.
 */
public interface KeyboardModelLoader {
    /**
     * Loads specified keyboard model using JSON file that is bundled with the application.
     *
     * @param model predefined keyboard model
     * @return created keyboard model
     */
    KeyboardModel load(PredefinedKeyboardModel model);

    /**
     * Loads keyboard model from specified file.
     *
     * @param file JSON file containing keyboard model
     * @return created keyboard model from specified file
     */
    KeyboardModel load(File file);

    final class KeyboardModelLoaderException extends RuntimeException {
        public KeyboardModelLoaderException(Throwable cause) {
            super(cause);
        }
    }
}
