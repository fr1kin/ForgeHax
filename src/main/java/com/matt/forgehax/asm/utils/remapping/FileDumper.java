package com.matt.forgehax.asm.utils.remapping;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.utils.asmtype.IASMType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
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
      if (writer != null) {
        writer.close();
      }
    }
  }

  private static void dumpMap(File dumpLocation, final Map<String, String> mapIn) {
    dump(
      dumpLocation,
      writer -> {
        for (Map.Entry<String, String> entry : new TreeMap<>(mapIn).entrySet()) {
          writer.println(entry.getKey() + "->" + entry.getValue());
        }
      });
  }

  private static void dumpMaps(File dumpLocation, final Map<String, Map<String, String>> mapIn) {
    dump(
      dumpLocation,
      writer -> {
        for (Map.Entry<String, Map<String, String>> entry : new TreeMap<>(mapIn).entrySet()) {
          writer.println(entry.getKey());
          for (Map.Entry<String, String> subEntry : new TreeMap<>(entry.getValue()).entrySet()) {
            writer.println("\t" + subEntry.getKey() + "->" + subEntry.getValue());
          }
        }
      });
  }

  private static void dumpASMTypes(File dumpLocation, Map<String, IASMType> mapIn) {
    dump(
      dumpLocation,
      writer -> {
        final StringBuilder builder = new StringBuilder();
        mapIn.forEach(
          (k, v) -> {
            builder.append(k);
            builder.append(":");
            builder.append(v.toString());
            builder.append("\n");
          });
        writer.println(builder.toString());
      });
  }

  private static void dumpMcpTypeMap(
    File dumpLocation, final Map<String, Map<String, ObfuscatedStateMapper.McpTypeData>> mapIn) {
    dump(
      dumpLocation,
      writer -> {
        final StringBuilder builder = new StringBuilder();
        mapIn
          .entrySet()
          .stream()
          .sorted(Comparator.comparing(Map.Entry::getKey))
          .forEach(
            entry -> {
              // class name
              builder.append(entry.getKey());
              builder.append("\n{\n");
              entry
                .getValue()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(
                  dataEntry -> {
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

  public static void dumpAllFiles() {
    ObfuscatedStateMapper obfuscatedRemapper = ObfuscatedStateMapper.getInstance();

    File dumpDir = new File("debuglog");
    dumpDir.mkdirs();

    // dump runtime classes
    dumpMap(new File(dumpDir, "classes.txt"), obfuscatedRemapper.getMcClasses());

    dumpMcpTypeMap(new File(dumpDir, "methods.txt"), obfuscatedRemapper.getMcpMethodData());
    dumpMcpTypeMap(new File(dumpDir, "fields.txt"), obfuscatedRemapper.getMcpFieldData());

    Class<?>[] constants =
      new Class[]{
        TypesMc.Classes.class,
        TypesMc.Fields.class,
        TypesMc.Methods.class,
        TypesHook.Classes.class,
        TypesHook.Fields.class,
        TypesHook.Methods.class
      };

    File typeDumpDir = new File(dumpDir, "typedump");
    typeDumpDir.mkdirs();

    for (Class<?> clazz : constants) {
      try {
        Map<String, IASMType> types = Maps.newHashMap();
        for (Field field : clazz.getFields()) {
          try {
            Object instance = field.get(null);
            if (instance instanceof IASMType) {
              types.put(field.getName(), (IASMType) instance);
            }
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
        dumpASMTypes(
          new File(typeDumpDir, clazz.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt"),
          types);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
