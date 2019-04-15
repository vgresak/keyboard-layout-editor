package cz.gresak.keyboardeditor.service.api;

import cz.gresak.keyboardeditor.model.ModelKey;

/**
 * Maps values of {@link ModelKey#getValues()} to their symbolic representation.
 */
public interface KeysymMapper {

    /**
     * Translates value to the symbolic representation.
     * Value can be either unicode value in UXXXX or 0X100XXXX format or keysym as defined in keysymdef.h without "XK_" prefix.
     * <p>
     * When the value is unicode value, the symbol is derived directly using {@link Character#toChars(int)} method.
     * In case of a non unicode value, symbol is looked up in "defaults/keysym_to_symbol" file.
     * If this file does not contain passed value, method returns passed in value.
     *
     * @param value value to be mapped to its symbolic representation
     * @return Symbolic representation or passed in value if the value is not unicode and "defaults/keysym_to_symbol" file does not contain the value.
     */
    String getSymbol(String value);
}
