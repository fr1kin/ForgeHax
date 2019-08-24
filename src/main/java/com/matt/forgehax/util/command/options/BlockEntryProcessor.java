package com.matt.forgehax.util.command.options;

import com.google.common.collect.Sets;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.blocks.BlockEntry;
import com.matt.forgehax.util.blocks.BlockOptionHelper;
import com.matt.forgehax.util.blocks.properties.BoundProperty;
import com.matt.forgehax.util.blocks.properties.ColorProperty;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.ExecuteData;
import com.matt.forgehax.util.command.exception.CommandExecuteException;
import java.util.Collection;

/**
 * Created on 6/6/2017 by fr1kin
 */
public class BlockEntryProcessor {
  
  public static void buildCollection(ExecuteData data) {
    data.requiredArguments(1);
    data.requiresEntry("meta");
    
    String arg = data.getArgumentAsString(0);
    
    int meta = data.get("meta");
    
    boolean id = data.options().has("id");
    boolean regex = data.options().has("regex");
    
    if (id && regex) {
      throw new CommandExecuteException("Cannot contain both id and regex flag");
    }
    
    Collection<BlockEntry> process = Sets.newHashSet();
    
    try {
      if (regex) {
        process.addAll(BlockOptionHelper.getAllBlocksMatchingByUnlocalized(arg));
      } else {
        process.add(
          id ? new BlockEntry(SafeConverter.toInteger(arg), meta) : new BlockEntry(arg, meta));
      }
    } catch (Throwable t) {
      throw new CommandExecuteException(t.getMessage());
    }
    
    data.set("entries", process);
  }
  
  public static void processColor(ExecuteData data) {
    data.requiresEntry("entries");
    
    Collection<BlockEntry> entries = data.get("entries");
    boolean isColorPresent = data.get("isColorPresent", false);
    
    if (isColorPresent) {
      final int colorBuffer = data.get("colorBuffer", Colors.WHITE.toBuffer());
      entries.forEach(entry -> entry.getWritableProperty(ColorProperty.class).set(colorBuffer));
    }
  }
  
  public static void processBounds(ExecuteData data) {
    if (data.hasOption("bounds")) {
      data.requiresEntry("entries");
      final Collection<BlockEntry> entries = data.get("entries");
  
      data.getOptions("bounds")
        .forEach(
          p -> {
            String value = String.valueOf(p);
            String[] mm = value.split("-");
            if (mm.length > 1) {
              int min = SafeConverter.toInteger(mm[0]);
              int max = SafeConverter.toInteger(mm[1]);
              entries.forEach(
                entry -> entry.getWritableProperty(BoundProperty.class).add(min, max));
            } else {
              throw new IllegalArgumentException(
                String.format(
                  "Invalid argument \"%s\" given for bounds option. Should be formatted like min-max",
                  value));
            }
          });
    }
  }
}
