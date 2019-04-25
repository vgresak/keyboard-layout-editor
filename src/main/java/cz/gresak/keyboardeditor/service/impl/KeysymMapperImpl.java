package cz.gresak.keyboardeditor.service.impl;

import cz.gresak.keyboardeditor.service.api.KeysymMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeysymMapperImpl implements KeysymMapper {

    private static final Pattern UNICODE_VALUE_PATTERN = Pattern.compile("(U([0-9a-fA-F]{4}))|(0x100([0-9a-fA-F]{4}))");
    private boolean showNoSymbol;
    private List<ShowNoSymbolChangedListener> listeners = new ArrayList<>();

    @Override
    public String getSymbol(String value) {
        if (isUnicode(value)) {
            return unicodeToString(value);
        }
        String symbol = KeysymToSymbol.getSymbol(value);
        if (symbol != null) {
            return symbol;
        }
        if (isNoSymbol(value)) {
            return showNoSymbol ? "NoSymbol" : "";
        }
        return value;
    }

    private boolean isNoSymbol(String value) {
        return StringUtils.isBlank(value) || "NoSymbol".equals(value);
    }

    @Override
    public void showNoSymbol(boolean showNoSymbol) {
        this.showNoSymbol = showNoSymbol;
        listeners.forEach(listener -> listener.onChanged(showNoSymbol));
    }

    @Override
    public void addShowNoSymbolChangedListener(ShowNoSymbolChangedListener listener) {
        listeners.add(listener);
    }

    private boolean isUnicode(String value) {
        return value != null && UNICODE_VALUE_PATTERN.matcher(value).matches();
    }

    private String unicodeToString(String value) {
        Matcher matcher = UNICODE_VALUE_PATTERN.matcher(value);
        if (matcher.matches()) {
            String numberPart = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);
            int unicodeNumber = Integer.parseInt(numberPart, 16);
            return String.valueOf(Character.toChars(unicodeNumber));
        }
        throw new RuntimeException("Value " + value + " does not match unicode regex.");
    }

    /**
     * Contains translation of keysym to symbol.
     */
    private static final class KeysymToSymbol {
        /**
         * Translation map.
         * Key = keysym.
         * Value = symbol.
         */
        private static final Map<String, String> MAP = new HashMap<>();
        /**
         * Regex pattern matching single line in keysym_to_symbol file. First group = keysym; second group = symbol to be displayed.
         */
        private static final Pattern LINE_PATTERN = Pattern.compile("^\uFEFF?(\\w+)\\s+\"(.*?)\"");

        static {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(KeysymToSymbol.class.getResourceAsStream("/defaults/keysym_to_symbol"), StandardCharsets.UTF_16BE))) {
                reader.lines().forEach(line -> {
                    Matcher matcher = LINE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        MAP.put(matcher.group(1), matcher.group(2));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        static String getSymbol(String keysym) {
            return MAP.get(keysym);
        }
    }
}
