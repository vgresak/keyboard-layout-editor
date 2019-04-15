package cz.gresak.keyboardeditor.service.api.xkbconfig;

import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.model.ModelKey;

public interface KeyboardModelUpdater {
    /**
     * Loads current keyboard layout contained in passed configuration into keyboard model (i.e. sets related key values of {@link ModelKey}).
     *
     * @param keyboardModel keyboard model to be updated
     * @param config        configuration containing source data for the keyboard model
     */
    void updateModel(KeyboardModel keyboardModel, Config config);
}
