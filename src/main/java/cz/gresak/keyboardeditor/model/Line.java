package cz.gresak.keyboardeditor.model;

import java.util.List;

public class Line {
    private double marginTop;
    private List<ModelKey> keys;

    public double getMarginTop() {
        return marginTop;
    }

    public Line setMarginTop(double marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    public List<ModelKey> getKeys() {
        return keys;
    }

    public Line setKeys(List<ModelKey> keys) {
        this.keys = keys;
        return this;
    }
}
