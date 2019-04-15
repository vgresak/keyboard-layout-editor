package cz.gresak.keyboardeditor.service.api.xkbconfig;

/**
 * Loads current XKB configuration using <code>xkbcomp "$DISPLAY" -</code> command. If command fails, default layout xkbcompout is used instead.
 */
public interface CurrentConfigLoader {

    /**
     * Loads current configuration using <code>xkbcomp "$DISPLAY" -</code> command. If command fails, default layout xkbcompout is used instead.
     * <p>
     * Command output is parsed into {@link Config} object.
     *
     * @return current configuration parsed from the <code>xkbcomp</code> command
     */
    Config getCurrentConfig();
}
