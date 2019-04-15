package cz.gresak.keyboardeditor.service.api;

/**
 * Holds selected group state. Group is a numeric value in range 1-8.
 */
public interface GroupState {
    /**
     * Retrieves currently selected group. Group is a numeric value in range 1-8.
     *
     * @return selected group
     */
    int getGroup();

    /**
     * Sets group value.
     *
     * @param group group value to be set
     * @throws IllegalArgumentException when group is not in range 1-8
     */
    void setGroup(int group);

    /**
     * Registers listener that is called when {@link #setGroup(int)} is called.
     *
     * @param listener listener to be registered
     */
    void addListener(GroupChangedListener listener);

    /**
     * Interface of a listener that listens for group change.
     */
    interface GroupChangedListener {
        /**
         * Callback with a newly selected group.
         *
         * @param newGroup newly set group
         */
        void groupChanged(int newGroup);
    }
}
