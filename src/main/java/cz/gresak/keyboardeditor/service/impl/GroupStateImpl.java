package cz.gresak.keyboardeditor.service.impl;


import cz.gresak.keyboardeditor.service.api.CommandExecutor;
import cz.gresak.keyboardeditor.service.api.GroupState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cz.gresak.keyboardeditor.service.ServiceLoader.lookup;

public class GroupStateImpl implements GroupState {
    private static GroupState instance;
    private int group;
    private List<GroupChangedListener> listeners = new ArrayList<>();
    private CommandExecutor commandExecutor = lookup(CommandExecutor.class);

    private GroupStateImpl() {
        loadCurrentGroup();
    }

    /**
     * Retrieves singleton instance of {@link GroupState}.
     *
     * @return singleton instance
     */
    public static GroupState getInstance() {
        if (instance == null) {
            instance = new GroupStateImpl();
        }
        return instance;
    }

    private void loadCurrentGroup() {
        Optional<String> commandResult = commandExecutor.execute("xkblayout-state print %c");
        try {
            group = Integer.parseInt(commandResult.orElse("0")) + 1; // xkblayout-state returns values 0-7
        } catch (NumberFormatException e) {
            //xkblayout-state failed to return a number
            group = 1;
        }
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public void setGroup(int group) {
        if (group < 1 || group > 8) {
            throw new IllegalArgumentException("Group has to be in range 1-8");
        }
        this.group = group;
        listeners.forEach(listener -> listener.groupChanged(group));
    }

    @Override
    public void addListener(GroupChangedListener listener) {
        this.listeners.add(listener);
    }

}
