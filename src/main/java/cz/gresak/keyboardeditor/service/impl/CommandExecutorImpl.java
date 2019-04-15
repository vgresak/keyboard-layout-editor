package cz.gresak.keyboardeditor.service.impl;

import cz.gresak.keyboardeditor.service.api.CommandExecutor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class CommandExecutorImpl implements CommandExecutor {

    @Override
    public Optional<String> execute(String command) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CommandLine cmdLine = CommandLine.parse(command);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == 0) {
                return Optional.of(outputStream.toString());
            }
        } catch (IOException e) {
            //Failed to execute command.
        }
        return Optional.empty();
    }
}
