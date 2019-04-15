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
        if (StringUtils.isNotBlank(key.getType())) {
            pw.printf("\t\ttype= \"%s\"%s%n", key.getType(), key.getValues().isEmpty() ? "" : ",");
        }
        Map<Integer, List<String>> values = key.getValues();
        List<String> symbols = values.keySet().stream()
                .sorted()
                .filter(group -> values.get(group) != null && !values.get(group).isEmpty())
                .map(group -> {
                    List<String> groupValues = values.get(group).stream()
                            .map(value -> StringUtils.isBlank(value) ? "NoSymbol" : value) // replace empty strings in list with NoSymbol
                            .collect(Collectors.toList());
                    return String.format("\t\tsymbols[Group%d] = [ %s ]", group, StringUtils.join(groupValues, ",\t"));
                }).collect(Collectors.toList());
        pw.print(StringUtils.join(symbols, ",\n"));
        pw.println("\n\t};");
    }
}
