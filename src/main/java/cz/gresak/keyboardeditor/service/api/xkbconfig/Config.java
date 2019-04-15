package cz.gresak.keyboardeditor.service.api.xkbconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates loaded XKB configuration.
 */
public final class Config {
    private final Map<String, Key> keys = new HashMap<>();
    private final List<String> types = new ArrayList<>();
    private final Map<Integer, String> groupNames = new HashMap<>();

    public void putKey(String keycode, Key key) {
        keys.put(keycode, key);
    }

    public Key getKey(String keycode) {
        return keys.get(keycode);
    }

    public void addType(String type) {
        types.add(type);
    }

    public List<String> getTypes() {
        return types;
    }

    public void putGroupName(Integer group, String groupName) {
        groupNames.put(group, groupName);
    }

    public Map<Integer, String> getGroupNames() {
        return groupNames;
    }
}
