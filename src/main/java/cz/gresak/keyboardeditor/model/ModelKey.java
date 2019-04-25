package cz.gresak.keyboardeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelKey {
    private String keycode;
    private Map<Integer, List<String>> values;
    private Map<Integer, String> types;
    private double marginLeft;
    private double width = 1;
    private double height = 1;

    public String getKeycode() {
        return keycode;
    }

    public ModelKey setKeycode(String keycode) {
        this.keycode = keycode;
        return this;
    }

    public Map<Integer, List<String>> getValues() {
        return values;
    }

    public ModelKey setValues(Map<Integer, List<String>> values) {
        this.values = values;
        return this;
    }

    public List<String> getGroup(int group) {
        if (values == null) {
            values = new HashMap<>();
        }
        if (!values.containsKey(group)) {
            values.put(group, new ArrayList<>());
        }
        return values.get(group);
    }

    public String getValue(int group, int level) {
        List<String> groupSymbols = getGroup(group);
        if (level >= 0 && level < groupSymbols.size()) {
            return groupSymbols.get(level);
        }
        return "";
    }

    public Map<Integer, String> getTypes() {
        return types;
    }

    public ModelKey setTypes(Map<Integer, String> types) {
        this.types = types;
        return this;
    }

    public String getType(int group) {
        if (types == null) {
            types = new HashMap<>();
        }
        return types.get(group);
    }

    public ModelKey setType(int group, String type) {
        if (types == null) {
            types = new HashMap<>();
        }
        this.types.put(group, type);
        return this;
    }

    public double getMarginLeft() {
        return marginLeft;
    }

    public ModelKey setMarginLeft(double marginLeft) {
        this.marginLeft = marginLeft;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public ModelKey setWidth(double width) {
        this.width = width;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public ModelKey setHeight(double height) {
        this.height = height;
        return this;
    }

    @Override
    public String toString() {
        return "ModelKey{" + "keycode='" + keycode + '\'' +
                ", values=" + values +
                '}';
    }
}
