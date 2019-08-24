package com.matt.forgehax.util.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagList;

public class EnchantmentUtils {

  public static List<EntityEnchantment> getEnchantments(NBTTagList tags) {
    if (tags == null) {
      return null;
    }
    List<EntityEnchantment> list = Lists.newArrayList();
    for (int i = 0; i < tags.tagCount(); i++) {
      list.add(
        new EntityEnchantment(
          tags.getCompoundTagAt(i).getShort("id"), tags.getCompoundTagAt(i).getShort("lvl")));
    }
    return list;
  }

  public static List<EntityEnchantment> getEnchantmentsSorted(NBTTagList tags) {
    List<EntityEnchantment> list = getEnchantments(tags);
    if (list != null) {
      Collections.sort(list, new EnchantSort());
    }
    return list;
  }

  // IV.sort(III)

  public static class EnchantSort implements Comparator<EntityEnchantment> {

    @Override
    public int compare(EntityEnchantment o1, EntityEnchantment o2) {
      int deltaEch1 = o1.getEnchantment().getMaxLevel() - o1.getEnchantment().getMinLevel();
      int deltaEch2 = o2.getEnchantment().getMaxLevel() - o2.getEnchantment().getMinLevel();
      if (deltaEch1 == deltaEch2) {
        return 0;
      } else if (deltaEch1 < deltaEch2) {
        return 1;
      } else {
        return -1;
      }
    }
  }

  public static class EntityEnchantment {
  
    private static final Map<Integer, String> SHORT_ENCHANT_NAMES = Maps.newHashMap();

    static {
      SHORT_ENCHANT_NAMES.put(0, "p");
      SHORT_ENCHANT_NAMES.put(1, "fp");
      SHORT_ENCHANT_NAMES.put(2, "ff");
      SHORT_ENCHANT_NAMES.put(3, "bp");
      SHORT_ENCHANT_NAMES.put(4, "pp");
      SHORT_ENCHANT_NAMES.put(5, "r");
      SHORT_ENCHANT_NAMES.put(6, "aa");
      SHORT_ENCHANT_NAMES.put(7, "th");
      SHORT_ENCHANT_NAMES.put(8, "ds");
      SHORT_ENCHANT_NAMES.put(9, "fw");
      SHORT_ENCHANT_NAMES.put(16, "sh");
      SHORT_ENCHANT_NAMES.put(17, "sm");
      SHORT_ENCHANT_NAMES.put(18, "boa");
      SHORT_ENCHANT_NAMES.put(19, "kb");
      SHORT_ENCHANT_NAMES.put(20, "fa");
      SHORT_ENCHANT_NAMES.put(21, "l");
      SHORT_ENCHANT_NAMES.put(32, "eff");
      SHORT_ENCHANT_NAMES.put(33, "st");
      SHORT_ENCHANT_NAMES.put(34, "ub");
      SHORT_ENCHANT_NAMES.put(35, "for");
      SHORT_ENCHANT_NAMES.put(48, "pow");
      SHORT_ENCHANT_NAMES.put(49, "pun");
      SHORT_ENCHANT_NAMES.put(50, "fl");
      SHORT_ENCHANT_NAMES.put(51, "inf");
      SHORT_ENCHANT_NAMES.put(61, "los");
      SHORT_ENCHANT_NAMES.put(62, "lur");
      SHORT_ENCHANT_NAMES.put(70, "mend");
    }

    private final Enchantment enchantment;
    private final int level;

    public EntityEnchantment(int id, int level) {
      this(Enchantment.getEnchantmentByID(id), level);
    }

    public EntityEnchantment(Enchantment enchantment, int level) {
      this.enchantment = enchantment;
      this.level = level;
    }

    public Enchantment getEnchantment() {
      return enchantment;
    }

    public int getLevel() {
      return level;
    }

    public String getShortName() {
      int id = Enchantment.getEnchantmentID(enchantment);
      if (SHORT_ENCHANT_NAMES.containsKey(id)) {
        if (enchantment.getMaxLevel() <= 1) {
          return SHORT_ENCHANT_NAMES.get(id);
        } else {
          return SHORT_ENCHANT_NAMES.get(id) + this.level;
        }
      } else {
        return toString();
      }
    }

    public String toString() {
      return enchantment.getTranslatedName(level);
    }
  }
}
