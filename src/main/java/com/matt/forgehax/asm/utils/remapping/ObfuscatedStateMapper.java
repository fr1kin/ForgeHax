package com.matt.forgehax.asm.utils.remapping;

import bspkrs.mmv.ClassSrgData;
import bspkrs.mmv.CsvData;
import bspkrs.mmv.CsvFile;
import bspkrs.mmv.MemberSrgData;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.ForgeHaxProperties;
import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.reflection.FastReflectionForge;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.IStateMapper;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Type;

/** Created on 1/12/2017 by fr1kin */
public class ObfuscatedStateMapper implements ASMCommon, IStateMapper {
  private static ObfuscatedStateMapper INSTANCE = null;

  public static ObfuscatedStateMapper getInstance() {
    return INSTANCE == null ? INSTANCE = new ObfuscatedStateMapper() : INSTANCE;
  }

  private final BiMap<String, String> mcClasses;

  private final Map<String, Map<String, McpTypeData>> mcpMethodData;
  private final Map<String, Map<String, McpTypeData>> mcpFieldData;

  protected ObfuscatedStateMapper() {
    LOGGER.info("Using build mapping \"" + ForgeHaxProperties.getMcpMappingUrl() + "\"");

    MCPMappingLoader mcpMappingLoader = null;
    try {
      mcpMappingLoader = new MCPMappingLoader(ForgeHaxProperties.getMcpMappingUrl());
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      ASMStackLogger.printStackTrace(e);
    }

    LOGGER.info("Mapping data successfully initialize");

    Objects.requireNonNull(mcpMappingLoader, "MCPMappingLoader failed to lookup obfuscation data");

    if (isObfuscated()) LOGGER.info("initializing ObfuscatedStateMapper WITH obfuscation");
    else LOGGER.info("initializing ObfuscatedStateMapper WITHOUT obfuscation");

    this.mcClasses =
        ImmutableBiMap.copyOf(
            FastReflectionForge.Fields.FMLDeobfuscatingRemapper_classNameBiMap.get(
                FMLDeobfuscatingRemapper.INSTANCE));

    this.mcpMethodData =
        buildMcpTypeData(
            mcpMappingLoader.getCsvMethodData(),
            mcpMappingLoader.getSrgFileData().class2MethodDataSet,
            getConvertedMap(
                FastReflectionForge.Fields.FMLDeobfuscatingRemapper_rawMethodMaps.get(
                    FMLDeobfuscatingRemapper.INSTANCE),
                str -> str.split("\\(")[0]),
            ((csvData, data) -> csvData.getMcpName() + data.getSrgDescriptor()));
    this.mcpFieldData =
        buildMcpTypeData(
            mcpMappingLoader.getCsvFieldData(),
            mcpMappingLoader.getSrgFileData().class2FieldDataSet,
            getConvertedMap(
                FastReflectionForge.Fields.FMLDeobfuscatingRemapper_rawFieldMaps.get(
                    FMLDeobfuscatingRemapper.INSTANCE),
                str -> str.split(":")[0]),
            ((csvData, data) -> csvData.getMcpName()));
  }

  public boolean isObfuscated() {
    return RuntimeState.isObfuscated();
  }

  public Map<String, String> getMcClasses() {
    return Collections.unmodifiableMap(mcClasses);
  }

  public Map<String, Map<String, McpTypeData>> getMcpFieldData() {
    return mcpFieldData;
  }

  public Map<String, Map<String, McpTypeData>> getMcpMethodData() {
    return mcpMethodData;
  }

  protected String getClassName(String className, Map<String, String> map) {
    // mcp map -> obf name
    String name = map.get(className);
    if (Strings.isNullOrEmpty(name)) {
      LOGGER.warn("Could not lookup name for class '" + className + "'");
      return null;
    } else return name;
  }

  @Nullable
  @Override
  public String getObfClassName(String className) {
    return getClassName(className, mcClasses.inverse());
  }

  @Nullable
  @Override
  public String getMcpClassName(String className) {
    return getClassName(className, mcClasses);
  }

  @Nullable
  @Override
  public String getSrgMethodName(
      String parentClassName, String methodName, String methodDescriptor) {
    try {
      return getMcpMethodData()
          .get(parentClassName)
          .get(methodName + methodDescriptor)
          .getSrgName();
    } catch (Exception e) {
      LOGGER.warn(
          "Could not lookup srg name for method '"
              + parentClassName
              + "::"
              + methodName
              + methodDescriptor
              + "'");
      return null;
    }
  }

  @Nullable
  @Override
  public String getObfMethodName(
      String parentClassName, String methodName, String methodDescriptor) {
    try {
      return getMcpMethodData()
          .get(parentClassName)
          .get(methodName + methodDescriptor)
          .getObfName();
    } catch (Exception e) {
      LOGGER.warn(
          "Could not lookup obf name for method '"
              + parentClassName
              + "::"
              + methodName
              + methodDescriptor
              + "'");
      return null;
    }
  }

  @Nullable
  @Override
  public String getSrgFieldName(String parentClassName, String fieldName) {
    try {
      return getMcpFieldData().get(parentClassName).get(fieldName).getSrgName();
    } catch (Exception e) {
      LOGGER.warn(
          "Could not lookup srg name for field '" + parentClassName + "." + fieldName + "'");
      return null;
    }
  }

  @Nullable
  @Override
  public String getObfFieldName(String parentClassName, String fieldName) {
    try {
      return getMcpFieldData().get(parentClassName).get(fieldName).getObfName();
    } catch (Exception e) {
      LOGGER.warn(
          "Could not lookup obf name for field '" + parentClassName + "." + fieldName + "'");
      return null;
    }
  }

  public Type translateMethodType(Type methodType) {
    Type[] translated =
        translateTypes(
            mcClasses,
            Lists.asList(methodType.getReturnType(), methodType.getArgumentTypes())
                .toArray(new Type[] {}));
    return Type.getMethodType(translated[0], Arrays.copyOfRange(translated, 1, translated.length));
  }

  public Type translateFieldType(Type fieldType) {
    return translateTypes(mcClasses, fieldType)[0];
  }

  private Type[] translateTypes(Map<String, String> mapIn, Type... types) {
    int index = 0;
    Type[] translated = new Type[types.length];
    for (Type arg : types) {
      switch (arg.getSort()) {
        case Type.ARRAY:
          // ignore primitive arrays
          if (arg.getElementType().getSort() != Type.OBJECT) break;
        case Type.OBJECT:
          String desc = arg.getDescriptor();
          String heading = desc.substring(0, desc.indexOf('L') + 1);
          String name = desc.substring(heading.length(), desc.indexOf(';'));
          String newName = mapIn.get(name);
          arg = Type.getType(heading + (!Strings.isNullOrEmpty(newName) ? newName : name) + ";");
          break;
        default:
          break;
      }
      translated[index++] = arg;
    }
    return translated;
  }

  private Map<String, Map<String, String>> getConvertedMap(
      Map<String, Map<String, String>> mapIn, Function<String, String> getNameFunction) {
    Map<String, Map<String, String>> mapOut = Maps.newHashMap();
    mapIn
        .entrySet()
        .forEach(
            entry -> {
              String realName = getMcpClassName(entry.getKey());
              if (!Strings.isNullOrEmpty(realName)) {
                Map<String, String> subMap = Maps.newHashMap();
                entry
                    .getValue()
                    .entrySet()
                    .forEach(
                        subEntry -> {
                          String key = getNameFunction.apply(subEntry.getKey());
                          String value = getNameFunction.apply(subEntry.getValue());
                          subMap.put(isObfuscated() ? value : key, isObfuscated() ? key : value);
                        });
                mapOut.put(realName, Collections.unmodifiableMap(subMap));
              }
            });
    return mapOut;
  }

  private static <E extends MemberSrgData> Map<String, Map<String, McpTypeData>> buildMcpTypeData(
      final CsvFile csvFile,
      final Map<ClassSrgData, Set<E>> mcpMappings,
      final Map<String, Map<String, String>> runtimeMappings,
      NamingFunction<E> mcpNameFunction) {
    final Map<String, Map<String, McpTypeData>> output = Maps.newHashMap();
    // parse over all classes
    mcpMappings
        .entrySet()
        .forEach(
            parentClassEntry -> {
              final Map<String, McpTypeData> typeDataMap = Maps.newHashMap();
              // lookup the class in the runtime type map
              final Map<String, String> runtimeClass =
                  runtimeMappings.get(parentClassEntry.getKey().getFullyQualifiedSrgName());
              if (!Objects.isNull(runtimeClass)) {
                // parse over all the methods inside the class
                parentClassEntry
                    .getValue()
                    .forEach(
                        data -> {
                          String srgName = data.getSrgName();
                          String obfName = runtimeClass.get(srgName);
                          // get the mcp name (if it exists)
                          CsvData csvData = csvFile.getCsvDataForKey(srgName);
                          String mcpName = !Objects.isNull(csvData) ? csvData.getMcpName() : null;
                          McpTypeData typeData = new McpTypeData(mcpName, srgName, obfName);
                          // add srg to type data conversion
                          typeDataMap.put(srgName, typeData);
                          // add mcp name to type data (if the mcp name exists)
                          if (!Strings.isNullOrEmpty(mcpName))
                            typeDataMap.put(mcpNameFunction.apply(csvData, data), typeData);
                        });
              }
              // class = {mcpTypeName=typeData}
              output.put(
                  parentClassEntry.getKey().getFullyQualifiedSrgName(),
                  Collections.unmodifiableMap(typeDataMap));
            });
    return Collections.unmodifiableMap(output);
  }

  public static class McpTypeData {
    private final String mcpName;
    private final String srgName;
    private final String obfName;

    public McpTypeData(String mcpName, String srgName, String obfName) {
      this.mcpName = mcpName;
      this.srgName = srgName;
      this.obfName = obfName;
    }

    public String getMcpName() {
      return mcpName;
    }

    public String getSrgName() {
      return srgName;
    }

    public String getObfName() {
      return obfName;
    }
  }

  private interface NamingFunction<E extends MemberSrgData> {
    String apply(CsvData csvData, E data);
  }
}
