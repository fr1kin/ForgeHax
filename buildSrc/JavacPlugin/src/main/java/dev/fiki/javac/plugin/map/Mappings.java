package dev.fiki.javac.plugin.map;

import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.javac.plugin.type.TypeRef;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public final class Mappings {
  private static Mappings instance = null;

  public static Mappings getInstance() {
    if (instance == null) {
      String loc = System.getenv("REMAPPER_FILE");
      if (loc == null) {
        loc = System.getProperty("remapper.file");
      }

      if (loc != null) {
        Path file = Paths.get(loc);
        if (Files.exists(file)) {
          try {
            return instance = new Mappings(file);
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          System.err.println("Mappings file \"" + loc + "\" does not exist");
        }
      } else {
        System.err.println("No mappings file provided");
      }
      instance = new Mappings();
    }
    return instance;
  }

  public final Map<String, ClassInfo> classMap = new HashMap<>();
  public final Map<String, MethodInfo> srgToMethod = new HashMap<>();
  public final Map<String, FieldInfo> srgToField = new HashMap<>();

  public Mappings(Path file) throws IOException {
    try (BufferedReader io = Files.newBufferedReader(file)) {
      Scanner scanner = new Scanner(io);

      ClassInfo classInfo = null;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        // skip empty lines
        if (line.trim().isEmpty()) {
          continue;
        }

        if (Character.isWhitespace(line.charAt(0))) {
          // whitespace at first char means this is a member of a class structure
          // must be a field or method

          // skip the first whitespace
          // a space character is used as a delimiter
          Scanner s = new Scanner(line.substring(1)).useDelimiter("\\s");
          String type = s.next();
          switch (type) {
            case "F":
            case "FS":
              FieldInfo field = new FieldInfo(Objects.requireNonNull(
                  classInfo, "No parent class identified"));
              field.name = s.next();
              field.obfName = s.next();
              field.srgName = s.next();
              srgToField.put(field.srgName, field);
              classInfo.srgToField.put(field.srgName, field);
              break;
            case "M":
            case "MS":
              MethodInfo method = new MethodInfo(Objects.requireNonNull(
                  classInfo, "No parent class identified"));
              method.name = s.next();
              method.obfName = s.next();
              method.srgName = s.next();
              method.descriptor = TypeRef.getMethodType(s.next());
              method.obfDescriptor = TypeRef.getMethodType(s.next());
              srgToMethod.put(method.srgName, method);
              classInfo.srgToMethod.put(method.srgName, method);
              break;
          }
        } else {
          // this must be a class
          Scanner s = new Scanner(line).useDelimiter("\\s");
          classInfo = new ClassInfo();
          classInfo.name = s.next();
          classInfo.obfName = s.next();
          classMap.put(classInfo.name, classInfo);
        }
      }
    }
  }

  Mappings() {}

  public ClassInfo getClassMapping(String className, Format format) {
    switch (format) {
      case OBFUSCATED:
        return classMap.values().stream()
            .filter(ci -> className.equals(ci.obfName))
            .findAny()
            .orElse(null);
      default:
        return classMap.get(className);
    }
  }

  public MethodInfo getMethodMapping(String parentClassName, final String methodName, final TypeRef descriptor, Format format) {
    ClassInfo parent = getClassMapping(parentClassName, format);
    if (parent != null) {
      switch (format) {
        case SRG:
          return parent.srgToMethod.get(methodName);
        case OBFUSCATED:
          return parent.srgToMethod.values().stream()
              .filter(mi -> methodName.equals(mi.obfName))
              .filter(mi -> descriptor.equals(mi.obfDescriptor))
              .findAny()
              .orElse(null);
        default:
        case NORMAL:
          return parent.srgToMethod.values().stream()
              .filter(mi -> methodName.equals(mi.name))
              .filter(mi -> descriptor.equals(mi.descriptor))
              .findAny()
              .orElse(null);
      }
    }
    return null;
  }

  public FieldInfo getFieldMapping(String parentClassName, final String fieldName, Format format) {
    ClassInfo parent = getClassMapping(parentClassName, format);
    if (parent != null) {
      switch (format) {
        case SRG:
          return parent.srgToField.get(fieldName);
        case OBFUSCATED:
          return parent.srgToField.values().stream()
              .filter(fi -> fieldName.equals(fi.obfName))
              .findAny()
              .orElse(null);
        default:
        case NORMAL:
          return parent.srgToField.values().stream()
              .filter(fi -> fieldName.equals(fi.name))
              .findAny()
              .orElse(null);
      }
    }
    return null;
  }
}
