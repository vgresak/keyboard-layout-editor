package cz.gresak.keyboardeditor.service.impl.xkbconfig;

import cz.gresak.keyboardeditor.service.api.CommandExecutor;
import cz.gresak.keyboardeditor.service.api.xkbconfig.Config;
import cz.gresak.keyboardeditor.service.api.xkbconfig.CurrentConfigLoader;
import cz.gresak.keyboardeditor.service.api.xkbconfig.Key;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.gresak.keyboardeditor.service.ServiceLoader.lookup;

public class CurrentConfigLoaderImpl implements CurrentConfigLoader {
    private static final String XKB_COMMAND = "xkbcomp %s -";
    private static final String DEFAULT_DISPLAY = ":0";
    private static final CurrentConfigLoader INSTANCE = new CurrentConfigLoaderImpl();
    private CommandExecutor commandExecutor = lookup(CommandExecutor.class);

    private CurrentConfigLoaderImpl() {
    }

    /**
     * Retrieves singleton instance.
     *
     * @return instance of {@link CurrentConfigLoader}.
     */
    public static CurrentConfigLoader getInstance() {
        return INSTANCE;
    }

    @Override
    public Config getCurrentConfig() {
        String xkbcompOutput = loadXkbCompOutput().orElseGet(this::loadDefault);
        Config config = new Config();
        setSymbols(config, xkbcompOutput);
        setTypes(config, xkbcompOutput);
        return config;
    }

    /**
     * Runs {@value XKB_COMMAND} command and retrieves its output.
     *
     * @return Output of command or {@link Optional#empty()} if there is a error during the command execution.
     */
    private Optional<String> loadXkbCompOutput() {
        try {
            String display = EnvironmentUtils.getProcEnvironment().get("DISPLAY");
            String xkbCommand = String.format(XKB_COMMAND, display == null ? DEFAULT_DISPLAY : display);
            return commandExecutor.execute(xkbCommand);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Loads predefined output of {@value XKB_COMMAND} command.
     *
     * @return Predefined output containing set keyboard layout.
     */
    private String loadDefault() {
        try (InputStream is = getClass().getResourceAsStream("/defaults/xkbcompout")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private void setTypes(Config config, String xkbcompOutput) {
        Pattern typesPattern = Pattern.compile("^xkb_types\\s.*?\\{", Pattern.MULTILINE);
        Matcher typesMatcher = typesPattern.matcher(xkbcompOutput);
        if (!typesMatcher.find()) {
            return;
        }
        String types = getBracesBody(xkbcompOutput, typesMatcher.end());
        Pattern singleTypePattern = Pattern.compile("type\\s*\"(.*?)\"\\s*\\{");
        Matcher singleTypeMatcher = singleTypePattern.matcher(types);
        while (singleTypeMatcher.find()) {
            config.addType(singleTypeMatcher.group(1));
        }
    }

    private void setSymbols(Config result, String xkbcompOutput) {
        Pattern symbolsPattern = Pattern.compile("^xkb_symbols\\s.*?\\{", Pattern.MULTILINE);
        Matcher symbolsMatcher = symbolsPattern.matcher(xkbcompOutput);
        if (!symbolsMatcher.find()) {
            return;
        }
        int startIndex = symbolsMatcher.end();
        String symbols = getBracesBody(xkbcompOutput, startIndex);
        setKeys(result, symbols);
        setGroupNames(result, symbols);
    }

    private void setKeys(Config result, String symbols) {
        Pattern keyPattern = Pattern.compile("key\\s+?(<.*?>)\\s+?\\{");
        Matcher keyMatcher = keyPattern.matcher(symbols);

        Pattern symbolsGroupPattern = Pattern.compile("\\[\\s*(.*[^\\W])\\s*]");
        Pattern groupKeysymPattern = Pattern.compile("(\\[[Gg]roup(\\d+)])?.*?\\[(.*?)]");
        while (keyMatcher.find()) {
            String keycode = keyMatcher.group(1);
            String bracesBody = getBracesBody(symbols, keyMatcher.end());
            Matcher symbolsGroupMatcher = symbolsGroupPattern.matcher(bracesBody);

            Map<Integer, List<String>> groupsOfKeysyms = new HashMap<>();
            while (symbolsGroupMatcher.find()) {
                String symbolsGroup = symbolsGroupMatcher.group();
                Matcher groupKeysymMatcher = groupKeysymPattern.matcher(symbolsGroup);
                while (groupKeysymMatcher.find()) {
                    int group = groupKeysymMatcher.group(2) == null ? 1 : Integer.parseInt(groupKeysymMatcher.group(2));
                    List<String> keysyms = Arrays.stream(groupKeysymMatcher.group(3).split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    groupsOfKeysyms.put(group, keysyms);
                }
            }
            Map<Integer, String> types = getTypes(bracesBody, groupsOfKeysyms.keySet());
            Key key = new Key(groupsOfKeysyms, types);
            result.putKey(keycode, key);
        }
    }

    private Map<Integer, String> getTypes(String bracesBody, Set<Integer> groups) {
        Pattern globalTypePattern = Pattern.compile("type\\s*=\\s*\"(.*?)\"");
        Pattern groupTypePattern = Pattern.compile("type\\[[Gg]roup(\\d+)]\\s*=\\s*\"(.*?)\"");
        String globalType = getFirstGroupIfMatches(bracesBody, globalTypePattern);
        Map<Integer, String> result = new HashMap<>();
        if (globalType != null) {
            groups.forEach(group -> result.put(group, globalType));
        } else {
            // types might be defined for each group separately
            Matcher groupTypeMatcher = groupTypePattern.matcher(bracesBody);
            while (groupTypeMatcher.find()) {
                int group = Integer.parseInt(groupTypeMatcher.group(1));
                result.put(group, groupTypeMatcher.group(2));
            }
        }
        return result;
    }

    private void setGroupNames(Config result, String symbols) {
        Pattern groupNamePattern = Pattern.compile("name\\[[Gg]roup(\\d+)]\\s*=\\s*\"(.*?)\"\\s*[;]?");
        Matcher groupNameMatcher = groupNamePattern.matcher(symbols);
        while (groupNameMatcher.find()) {
            int group = Integer.parseInt(groupNameMatcher.group(1));
            String groupName = groupNameMatcher.group(2);
            result.putGroupName(group, groupName);
        }
    }

    private String getFirstGroupIfMatches(String value, Pattern pattern) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getBracesBody(String string, int startIndex) {
        int braces = 1;
        int index = startIndex;
        for (; index < string.length() && braces > 0; index++) {
            char character = string.charAt(index);
            if (character == '{') {
                braces++;
            } else if (character == '}') {
                braces--;
            }
        }
        return string.substring(startIndex, index - 1);
    }
}