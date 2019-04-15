package cz.gresak.keyboardeditor.service.api.xkbconfig;

import java.util.List;
import java.util.Map;

public final class Key {
    private final Map<Integer, List<String>> symbolGroups;
    private final String type;

    public Key(Map<Integer, List<String>> symbolGroups, String type) {
        this.symbolGroups = symbolGroups;
        this.type = type;
    }

    public Map<Integer, List<String>> getSymbolGroups() {
        return symbolGroups;
    }


    public String getType() {
        return type;
    }
}
