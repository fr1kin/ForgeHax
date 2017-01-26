package com.matt.forgehax.asm2.util;

import com.fr1kin.asmhelper.types.IASMType;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm2.constants.McpConstants;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created on 1/11/2017 by fr1kin
 */
public class FileDumper {
    private static void dump(File dumpLocation, Consumer<PrintWriter> consumer) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(dumpLocation, "UTF-8");
            consumer.accept(writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if(writer != null)
                writer.close();
        }
    }

    private static void dumpMap(File dumpLocation, final Map<String, String> mapIn) {
        dump(dumpLocation, writer -> {
            for(Map.Entry<String, String> entry : new TreeMap<>(mapIn).entrySet()) {
                writer.println(entry.getKey() + "->" + entry.getValue());
            }
        });
    }

    private static void dumpMaps(File dumpLocation, final Map<String, Map<String, String>> mapIn) {
        dump(dumpLocation, writer -> {
            for(Map.Entry<String, Map<String, String>> entry : new TreeMap<>(mapIn).entrySet()) {
                writer.println(entry.getKey());
                for(Map.Entry<String, String> subEntry : new TreeMap<>(entry.getValue()).entrySet()) {
                    writer.println("\t" + subEntry.getKey() + "->" + subEntry.getValue());
                }
            }
        });
    }

    private static void dumpASMTypes(File dumpLocation, Map<String, IASMType> mapIn) {
        dump(dumpLocation, writer -> {
            final StringBuilder builder = new StringBuilder();
            mapIn.entrySet().forEach(entry -> {
                builder.append(entry.getKey());
                builder.append(":");
                builder.append(entry.getValue().toString());
                builder.append("\n");
            });
            writer.println(builder.toString());
        });
    }

    private static void dumpMcpTypeMap(File dumpLocation, final Map<String, Map<String, ObfuscationHelper.McpTypeData>> mapIn) {
        dump(dumpLocation, writer -> {
            final StringBuilder builder = new StringBuilder();
            mapIn.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> {
                        // class name
                        builder.append(entry.getKey());
                        builder.append("\n{\n");
                        entry.getValue().entrySet()
                                .stream()
                                .sorted(Comparator.comparing(Map.Entry::getKey))
                                .forEach(dataEntry -> {
                                    builder.append("\t");
                                    builder.append(dataEntry.getKey());
                                    builder.append("\n\t{\n\t\tSrgName: ");
                                    builder.append(dataEntry.getValue().getSrgName());
                                    builder.append("\n\t\tObfName: ");
                                    builder.append(dataEntry.getValue().getObfName());
                                    builder.append("\n\t\tMcpName: ");
                                    builder.append(dataEntry.getValue().getMcpName());
                                    builder.append("\n\t}\n");
                                });
                        builder.append("}\n");
                    });
            writer.println(builder.toString());
        });
    }

    public static void dumpAllFiles(ObfuscationHelper obfuscationHelper) {
        Objects.requireNonNull(obfuscationHelper);

        File dumpDir = new File("debuglog");
        dumpDir.mkdirs();

        // dump runtime classes
        dumpMap(new File(dumpDir, "classes.txt"), obfuscationHelper.getMcClasses());

        dumpMcpTypeMap(new File(dumpDir, "methods.txt"), obfuscationHelper.getMcpMethodData());
        dumpMcpTypeMap(new File(dumpDir, "fields.txt"), obfuscationHelper.getMcpFieldData());

        McpConstants constants = McpConstants.getInstance();

        Map<String, IASMType> types = Maps.newHashMap();

        try {
            for (Field field : constants.getClass().getFields()) {
                try {
                    Object instance = field.get(constants);
                    if (instance instanceof IASMType)
                        types.put(field.getName(), (IASMType)instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dumpASMTypes(new File(dumpDir, "asm_types.txt"), types);
    }
}
