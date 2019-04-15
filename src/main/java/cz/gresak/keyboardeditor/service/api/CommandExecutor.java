package cz.gresak.keyboardeditor.service.api;

import java.util.Optional;

public interface CommandExecutor {
    /**
     * Executes provided command and returns command output.
     *
     * @param command command to execute
     * @return stdout and stderr of executed command or {@link Optional#empty()} when the command execution fails
     */
    Optional<String> execute(String command);
}
