package cz.gresak.keyboardeditor.service.api;

/**
 * Contains predefined keyboard models and their location.
 */
public enum PredefinedKeyboardModel {
    /**
     * 104 key ANSI / American keyboard containing alphabetical part, functional keys, navigation keys and numeric part.
     */
    FULL_FEATURED("/model/ansi104.json"),
    /**
     * 104 key ANSI / American keyboard without functional keys, navigation keys and numeric part.
     */
    ALPHABETICAL("/model/ansi104_alphabetical.json"),
    /**
     * 104 key ANSI / American keyboard with functional keys and without navigation keys and numeric part.
     */
    FN_KEYS("/model/ansi104_function_keys.json"),
    /**
     * 104 key ANSI / American keyboard with functional keys, navigation keys and without numeric part.
     */
    FN_AND_NAV_KEYS("/model/ansi104_function_keys_navigation_keys.json");

    private final String path;

    PredefinedKeyboardModel(String path) {
        this.path = path;
    }

    public static PredefinedKeyboardModel of(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getPath() {
        return path;
    }
}
