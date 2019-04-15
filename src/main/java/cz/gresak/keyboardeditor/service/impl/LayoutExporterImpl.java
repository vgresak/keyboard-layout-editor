package cz.gresak.keyboardeditor.service.impl;

import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.model.ModelKey;
import cz.gresak.keyboardeditor.service.api.LayoutExportResult;
import cz.gresak.keyboardeditor.service.api.LayoutExporter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;


public class LayoutExporterImpl implements LayoutExporter {

    @Override
    public LayoutExportResult export(KeyboardModel model, File outputFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile, false))) {
            export(model, pw);
        } catch (IOException e) {
            return LayoutExportResult.error(e);
        }
        return LayoutExportResult.ok();
    }

    private void export(KeyboardModel model, PrintWriter pw) {
        pw.println("xkb_symbols \"basic\" {");
        printGroupNames(model, pw);
        printKeys(model, pw);
        pw.println("};");
    }

    private void printGroupNames(KeyboardModel model, PrintWriter pw) {
        model.getGroupNames().forEach((key, value) -> pw.printf("\tname[group%d]=\"%s\";%n", key, value));
    }

    private void printKeys(KeyboardModel model, PrintWriter pw) {
        model.getLines().stream()
                .flatMap(line -> line.getKeys().stream())
                .forEach(key -> printKey(key, pw));
    }

    private void printKey(ModelKey key, PrintWriter pw) {
        pw.printf("\tkey %s { %n", key.getKeycode());
        printTypes(key, pw);
        printSymbols(key, pw);
        pw.println("\n\t};");
    }

    private void printTypes(ModelKey key, PrintWriter pw) {
        Map<Integer, String> types = key.getTypes();
        List<String> typesDefinition = types.entrySet().stream()
                .sorted(comparingByKey())
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .map(entry -> String.format("\t\ttype[group%d]= \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        pw.print(StringUtils.join(typesDefinition, ",\n"));
        if (key.getValues() != null && !key.getValues().isEmpty()) {
            pw.printf(",\n");
        }
    }

    private void printSymbols(ModelKey key, PrintWriter pw) {
        Map<Integer, List<String>> values = key.getValues();
        List<String> symbols = values.entrySet().stream()
                .sorted(comparingByKey())
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> {
                    List<String> groupValues = entry.getValue().stream()
                            .map(value -> StringUtils.isBlank(value) ? "NoSymbol" : value) // replace empty strings in list with NoSymbol
                            .collect(Collectors.toList());
                    return String.format("\t\tsymbols[Group%d] = [ %s ]", entry.getKey(), StringUtils.join(groupValues, ",\t"));
                }).collect(Collectors.toList());
        pw.print(StringUtils.join(symbols, ",\n"));
    }
}
