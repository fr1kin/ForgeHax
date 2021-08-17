package dev.fiki.forgehax.api.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class EnchantmentUtils {
  
  public static List<ItemEnchantment> getEnchantments(ListNBT tags) {
    if (tags == null) {
      return Collections.emptyList();
    }

    List<ItemEnchantment> list = Lists.newArrayList();
    for (int i = 0; i < tags.size(); i++) {
      list.add(new ItemEnchantment(tags.getCompound(i).getString("id"),
          tags.getCompound(i).getInt("lvl")));
    }

    return list;
  }

  public static List<ItemEnchantment> getEnchantments(ItemStack stack) {
    return getEnchantments(stack.getEnchantmentTags());
  }

  @Getter
  @AllArgsConstructor
  public static class ItemEnchantment implements Comparator<ItemEnchantment>, Comparable<ItemEnchantment> {
    private static final Map<Enchantment, String> SHORT_ENCHANT_NAMES = Maps.newHashMap();

    static {
      SHORT_ENCHANT_NAMES.put(Enchantments.ALL_DAMAGE_PROTECTION, "P");
      SHORT_ENCHANT_NAMES.put(Enchantments.FIRE_PROTECTION, "FP");
      SHORT_ENCHANT_NAMES.put(Enchantments.FALL_PROTECTION, "FF");
      SHORT_ENCHANT_NAMES.put(Enchantments.BLAST_PROTECTION, "BP");
      SHORT_ENCHANT_NAMES.put(Enchantments.PROJECTILE_PROTECTION, "PP");
      SHORT_ENCHANT_NAMES.put(Enchantments.RESPIRATION, "Re");
      SHORT_ENCHANT_NAMES.put(Enchantments.AQUA_AFFINITY, "Aq");
      SHORT_ENCHANT_NAMES.put(Enchantments.THORNS, "Th");
      SHORT_ENCHANT_NAMES.put(Enchantments.DEPTH_STRIDER, "DS");
      SHORT_ENCHANT_NAMES.put(Enchantments.FROST_WALKER, "FW");
      SHORT_ENCHANT_NAMES.put(Enchantments.BINDING_CURSE, "Bind");
      SHORT_ENCHANT_NAMES.put(Enchantments.SHARPNESS, "Sh");
      SHORT_ENCHANT_NAMES.put(Enchantments.SMITE, "Sm");
      SHORT_ENCHANT_NAMES.put(Enchantments.BANE_OF_ARTHROPODS, "BA");
      SHORT_ENCHANT_NAMES.put(Enchantments.KNOCKBACK, "Kn");
      SHORT_ENCHANT_NAMES.put(Enchantments.FIRE_ASPECT, "FA");
      SHORT_ENCHANT_NAMES.put(Enchantments.MOB_LOOTING, "Lo");
      SHORT_ENCHANT_NAMES.put(Enchantments.SWEEPING_EDGE, "Sw");
      SHORT_ENCHANT_NAMES.put(Enchantments.BLOCK_EFFICIENCY, "Ef");
      SHORT_ENCHANT_NAMES.put(Enchantments.SILK_TOUCH, "ST");
      SHORT_ENCHANT_NAMES.put(Enchantments.UNBREAKING, "Ub");
      SHORT_ENCHANT_NAMES.put(Enchantments.BLOCK_FORTUNE, "Ft");
      SHORT_ENCHANT_NAMES.put(Enchantments.POWER_ARROWS, "Po");
      SHORT_ENCHANT_NAMES.put(Enchantments.PUNCH_ARROWS, "Pu");
      SHORT_ENCHANT_NAMES.put(Enchantments.FLAMING_ARROWS, "Fl");
      SHORT_ENCHANT_NAMES.put(Enchantments.INFINITY_ARROWS, "Inf");
      SHORT_ENCHANT_NAMES.put(Enchantments.FISHING_LUCK, "LoTS");
      SHORT_ENCHANT_NAMES.put(Enchantments.FISHING_SPEED, "Lu");
      SHORT_ENCHANT_NAMES.put(Enchantments.LOYALTY, "Lo");
      SHORT_ENCHANT_NAMES.put(Enchantments.IMPALING, "Im");
      SHORT_ENCHANT_NAMES.put(Enchantments.RIPTIDE, "Rt");
      SHORT_ENCHANT_NAMES.put(Enchantments.CHANNELING, "Ch");
      SHORT_ENCHANT_NAMES.put(Enchantments.MULTISHOT, "Ms");
      SHORT_ENCHANT_NAMES.put(Enchantments.QUICK_CHARGE, "Qc");
      SHORT_ENCHANT_NAMES.put(Enchantments.PIERCING, "Pc");
      SHORT_ENCHANT_NAMES.put(Enchantments.MENDING, "Me");
      SHORT_ENCHANT_NAMES.put(Enchantments.VANISHING_CURSE, "Van");
    }

    private static String createUniqueString(Enchantment enchantment) {
      ResourceLocation resource = Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment),
          "unknown enchantment");
      String name = resource.getPath();

      String[] sections = name.split("_");
      List<String> strings = Lists.newArrayListWithExpectedSize(sections.length);

      for(String section : sections) {
        strings.add(String.valueOf(section.charAt(0)).toUpperCase());
      }

      int charIndex = 0;
      int sectionIndex = 0;

      while(SHORT_ENCHANT_NAMES.containsValue(String.join("", strings))
          && sectionIndex < sections.length) {
        String currentSection = sections[sectionIndex]; // Lure
        if(currentSection.length() - (1 + charIndex) < 0) {
          // ran out of characters, go to next section
          sectionIndex++;
        } else {
          // 1 trims the initial character
          String chars = currentSection.substring(1 + charIndex++);
          String current = strings.get(sectionIndex);
          strings.set(sectionIndex, current + String.valueOf(chars.indexOf(0)).toLowerCase());
        }
      }

      return String.join("", strings);
    }
    
    private final Enchantment enchantment;
    private final int level;
    
    public ItemEnchantment(String id, int level) {
      this(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(id)), level);
    }

    public boolean isMultiLevel() {
      return getEnchantment().getMaxLevel() > 1;
    }
    
    public String getShortName() {
      return SHORT_ENCHANT_NAMES.computeIfAbsent(enchantment, ItemEnchantment::createUniqueString);
    }

    @Override
    public String toString() {
      return enchantment.getFullname(level).getContents();
    }

    @Override
    public int compare(ItemEnchantment o1, ItemEnchantment o2) {
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

    @Override
    public int compareTo(ItemEnchantment o) {
      return compare(this, o);
    }
  }
}
