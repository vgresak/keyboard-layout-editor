package cz.gresak.keyboardeditor.model;

import java.util.List;
import java.util.Map;

public class KeyboardModel {

    private List<Line> lines;
    private Map<Integer, String> groupNames;

    public List<Line> getLines() {
        return lines;
    }

    public KeyboardModel setLines(List<Line> lines) {
        this.lines = lines;
        return this;
    }

    public Map<Integer, String> getGroupNames() {
        return groupNames;
    }

    public KeyboardModel setGroupNames(Map<Integer, String> groupNames) {
        this.groupNames = groupNames;
        return this;
    }
}
