package dev.fiki.javac.plugin.type;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {
  private static final Pattern DESCRIPTOR_PATTERN = Pattern.compile("^\\[?L?(.+?);?$");

  public static String getInternalName(TypeMirror type) {
    switch (type.getKind()) {
      case ARRAY:
        return "[" + getInternalName(((Type.ArrayType) type).getComponentType());
      case DECLARED:
        return "L" + getInternalName((DeclaredType) type) + ";";
      case TYPEVAR:
        return "L" + getInternalName(getGenericDeclaredType(type)) + ";";
      case BOOLEAN:
        return "Z";
      case BYTE:
        return "B";
      case CHAR:
        return "C";
      case DOUBLE:
        return "D";
      case FLOAT:
        return "F";
      case INT:
        return "I";
      case LONG:
        return "J";
      case SHORT:
        return "S";
      case VOID:
        return "V";
      default:
        throw new Error("Unknown type \"" + type + "\"");
    }
  }

  private static String getInternalName(DeclaredType type) {
    return type == null ? "java/lang/Object" : getInternalClassName(type.asElement());
  }

  public static String getInternalClassName(Element type) {
    StringBuilder name = new StringBuilder(type.getSimpleName());

    for (Element next = type.getEnclosingElement(); next != null; next = next.getEnclosingElement()) {
      if (next instanceof PackageElement) {
        PackageElement pkg = (PackageElement) next;
        name.insert(0, toInternalClassName(pkg.getQualifiedName().toString()) + "/");
      } else if (next instanceof TypeElement) {
        name.insert(0, next.getSimpleName() + "$");
      }
    }

    return name.toString();
  }

  public static String toClassName(String internalClassName) {
    return internalClassName.replace('/', '.').replace('$', '.');
  }

  public static String toInternalClassName(String className) {
    return className.replace('.', '/');
  }

  public static DeclaredType getGenericDeclaredType(TypeMirror type) {
    while (type != null) {
      if (type instanceof DeclaredType) {
        return (DeclaredType) type;
      } else if (type instanceof TypeVariable) {
        type = ((TypeVariable) type).getUpperBound();
      }
    }
    return null;
  }

  public static String getMethodName(Symbol.MethodSymbol symbol) {
    return symbol.getSimpleName().toString();
  }

  public static String getFieldName(Symbol.VarSymbol symbol) {
    return symbol.getSimpleName().toString();
  }

  public static String[] getArgumentDescriptors(Type[] types) {
    return Arrays.stream(types)
        .map(TypeUtil::getInternalName)
        .toArray(String[]::new);
  }

  public static String[] getArgumentDescriptors(Symbol.MethodSymbol symbol) {
    return getArgumentDescriptors(symbol.getParameters().stream()
        .map(Symbol::asType)
        .toArray(Type[]::new));
  }

  public static String getReturnTypeDescriptor(Symbol.MethodSymbol symbol) {
    return getInternalName(symbol.getReturnType());
  }

  public static TypeRef getDescriptor(Symbol.MethodSymbol symbol) {
    return getDescriptor(symbol.getReturnType(),
        symbol.getParameters().stream()
            .map(Symbol::asType)
            .toArray(Type[]::new));
  }

  public static TypeRef getDescriptor(Type returnType, Type[] argumentTypes) {
    return TypeRef.getMethodType(TypeRef.getType(getInternalName(returnType)),
        Arrays.stream(argumentTypes)
            .map(TypeUtil::getInternalName)
            .map(TypeRef::getType)
            .toArray(TypeRef[]::new));
  }

  public static String getDescriptorElement(String descriptor) {
    if (descriptor.startsWith("(")) {
      throw new IllegalArgumentException("Method descriptors not allowed!");
    }

    Matcher matcher = DESCRIPTOR_PATTERN.matcher(descriptor);
    if (matcher.find()) {
      return matcher.group();
    } else {
      throw new Error("Could not determine type for descriptor " + descriptor);
    }
  }

  public static String[] getDescriptorParameters(String descriptor) {
    if (!descriptor.startsWith("(")) {
      throw new IllegalArgumentException("Only method descriptors allowed!");
    }

    List<String> params = new ArrayList<>();
    main: for (int i = 0; i < descriptor.length(); i++) {
      char at = descriptor.charAt(i);
      switch (at) {
        case '[': // skip over the arrays
        case '(':
          // start of parameter list
          break;
        case ')':
          // end of parameter list
          break main;
        case 'L':
          int objectStart;
          for (objectStart = ++i;; ++i) {
            if (i >= descriptor.length()) {
              throw new Error("Failed to parse malformed descriptor \"" + descriptor + "\"");
            } else if (descriptor.charAt(i) == ';') {
              break;
            }
          }
          params.add(descriptor.substring(objectStart, i));
          break;
        default:
          params.add("" + at);
      }
    }

    return params.toArray(new String[0]);
  }
}
