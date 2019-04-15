package cz.gresak.keyboardeditor.service.impl.xkbconfig;

import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.model.Line;
import cz.gresak.keyboardeditor.model.ModelKey;
import cz.gresak.keyboardeditor.service.api.xkbconfig.Config;
import cz.gresak.keyboardeditor.service.api.xkbconfig.Key;
import cz.gresak.keyboardeditor.service.api.xkbconfig.KeyboardModelUpdater;

public class KeyboardModelUpdaterImpl implements KeyboardModelUpdater {

    @Override
    public void updateModel(KeyboardModel keyboardModel, Config config) {
        keyboardModel.setGroupNames(config.getGroupNames());
        for (Line line : keyboardModel.getLines()) {
            for (ModelKey key : line.getKeys()) {
                Key parsedKey = config.getKey(key.getKeycode());
                key.setValues(parsedKey.getSymbolGroups());
                key.setType(parsedKey.getType());
            }
        }
    }
}
