package cz.gresak.keyboardeditor.service.api.xkbconfig;

import java.util.List;
import java.util.Map;

public final class Key {
    private final Map<Integer, List<String>> symbolGroups;
    private final Map<Integer, String> types;

    public Key(Map<Integer, List<String>> symbolGroups, Map<Integer, String> types) {
        this.symbolGroups = symbolGroups;
        this.types = types;
    }

    public Map<Integer, List<String>> getSymbolGroups() {
        return symbolGroups;
    }


    public Map<Integer, String> getTypes() {
        return types;
    }
}
