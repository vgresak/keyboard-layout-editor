package cz.gresak.keyboardeditor.service.impl;

import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.model.ModelKey;
import cz.gresak.keyboardeditor.service.ServiceLoader;
import cz.gresak.keyboardeditor.service.api.ExportConfig;
import cz.gresak.keyboardeditor.service.api.GroupState;
import cz.gresak.keyboardeditor.service.api.LayoutExportResult;
import cz.gresak.keyboardeditor.service.api.LayoutExporter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Map.Entry.comparingByKey;


public class LayoutExporterImpl implements LayoutExporter {

    private GroupState groupState = ServiceLoader.lookup(GroupState.class);

    @Override
    public LayoutExportResult export(KeyboardModel model, File outputFile, ExportConfig exportConfig) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile, false))) {
            export(model, pw, exportConfig);
        } catch (IOException e) {
            return LayoutExportResult.error(e);
        }
        return LayoutExportResult.ok();
    }

    private void export(KeyboardModel model, PrintWriter pw, ExportConfig exportConfig) {
        pw.println("xkb_symbols \"basic\" {");
        printGroupNames(model, pw, exportConfig);
        printKeys(model, pw, exportConfig);
        pw.println("};");
    }

    private void printGroupNames(KeyboardModel model, PrintWriter pw, ExportConfig exportConfig) {
        model.getGroupNames().forEach((key, value) -> {
            if (exportConfig.canGroupBeExported(key) || (groupState.getGroup() == key && exportConfig.isExportOnlySelectedGroup())) {
                pw.printf("\tname[group%d]=\"%s\";%n", key, value);
            }
        });
    }

    private void printKeys(KeyboardModel model, PrintWriter pw, ExportConfig exportConfig) {
        model.getLines().stream()
                .flatMap(line -> line.getKeys().stream())
                .forEach(key -> printKey(key, pw, exportConfig));
    }

    private void printKey(ModelKey key, PrintWriter pw, ExportConfig exportConfig) {
        boolean shortSyntax = useShortSyntax(exportConfig, key);
        pw.printf("\tkey %s { ", key.getKeycode());
        if (!shortSyntax) {
            pw.println();
        }
        if (exportConfig.isExportType()) {
            printTypes(key, pw, exportConfig);
        }
        printSymbols(key, pw, exportConfig);
        if (!shortSyntax) {
            pw.print("\n\t");
        }
        pw.println("};");
    }

    private boolean useShortSyntax(ExportConfig exportConfig, ModelKey key) {
        if (exportConfig.isExportType() && !key.getTypes().isEmpty()) {
            return false;
        }
        if (exportConfig.isExportOnlySelectedGroup()) {
            return true;
        }
        boolean[] groupsToExport = exportConfig.getGroupsToExport();
        long countOfExportedGroups = IntStream
                .range(0, groupsToExport.length)
                .mapToObj(idx -> groupsToExport[idx])
                .filter(exportGroup -> exportGroup)
                .count();
        return countOfExportedGroups <= 1;
    }

    private void printTypes(ModelKey key, PrintWriter pw, ExportConfig exportConfig) {
        int selectedGroup = groupState.getGroup();
        Map<Integer, String> types = key.getTypes();
        List<String> typesDefinition = types.entrySet().stream()
                .sorted(comparingByKey())
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .filter(entry -> exportConfig.isExportOnlySelectedGroup() ? selectedGroup == entry.getKey() : exportConfig.canGroupBeExported(entry.getKey()))
                .map(entry -> String.format("\t\ttype[group%d]= \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        pw.print(StringUtils.join(typesDefinition, ",\n"));
        if (key.getValues() != null && !key.getValues().isEmpty() && !typesDefinition.isEmpty()) {
            pw.printf(",\n");
        }
    }

    private void printSymbols(ModelKey key, PrintWriter pw, ExportConfig exportConfig) {
        if (exportConfig.isExportOnlySelectedGroup()) {
            int group = groupState.getGroup();
            List<String> symbolsInSelectedGroup = key.getValues().get(group);
            printSingleGroup(Pair.of(group, symbolsInSelectedGroup), pw, exportConfig, key.getTypes().isEmpty());
        } else {
            // print only selected groups in configuration
            List<Map.Entry<Integer, List<String>>> symbols = key.getValues().entrySet().stream()
                    .sorted(comparingByKey())
                    .filter(entry -> exportConfig.canGroupBeExported(entry.getKey()))
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .collect(Collectors.toList());
            if (symbols.size() == 1) {
                printSingleGroup(symbols.iterator().next(), pw, exportConfig, key.getTypes().isEmpty());
            } else {
                printMultilineGroup(symbols, pw);
            }
        }
    }

    private void printSingleGroup(Map.Entry<Integer, List<String>> symbols, PrintWriter pw, ExportConfig exportConfig, boolean typesEmpty) {
        if (exportConfig.isExportType() && !typesEmpty) {
            pw.print(String.format("\t\tsymbols[Group%d] = %s", symbols.getKey(), getGroupValues(symbols.getValue())));
        } else {
            pw.print(String.format("\t\t%s ", getGroupValues(symbols.getValue())));
        }
    }

    private void printMultilineGroup(List<Map.Entry<Integer, List<String>>> symbols, PrintWriter pw) {
        List<String> symbolsToPrint = symbols.stream()
                .map(entry -> String.format("\t\tsymbols[Group%d] = %s", entry.getKey(), getGroupValues(entry.getValue())))
                .collect(Collectors.toList());
        pw.print(StringUtils.join(symbolsToPrint, ",\n"));
    }

    private String getGroupValues(List<String> values) {
        List<String> groupValues = values.stream()
                .map(value -> StringUtils.isBlank(value) ? "NoSymbol" : value) // replace empty strings in list with NoSymbol
                .collect(Collectors.toList());
        return String.format("[ %s ]", StringUtils.join(groupValues, ",\t"));
    }
}
