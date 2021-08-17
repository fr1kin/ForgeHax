package dev.fiki.forgehax.api.extension;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.Tuple;
import dev.fiki.forgehax.api.entity.ImmutableSlot;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
public class ItemEx {
  private static final Map<String, Enchantment> ENCHANTMENT_CACHE = Maps.newHashMap();

  private static Enchantment lookupEnchantmentId(String name) {
    return ENCHANTMENT_CACHE.computeIfAbsent(name,
        id -> ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(id)));
  }

  public static boolean isFoodItem(Item item) {
    return ItemGroup.TAB_FOOD == item.getItemCategory();
  }

  public static boolean isFoodItem(ItemStack stack) {
    return isFoodItem(stack.getItem());
  }

  public static Block getBlockForItem(ItemStack stack) {
    val item = stack.getItem();
    return item instanceof BlockItem ? ((BlockItem) item).getBlock() : Blocks.AIR;
  }

  public static boolean canBeDamaged(ItemStack stack) {
    return stack.getItem().canBeDepleted();
  }

  public static int getDurability(ItemStack stack) {
    return canBeDamaged(stack) ? stack.getMaxDamage() - stack.getDamageValue() : 0;
  }

  public static int getStackCount(ItemStack stack) {
    return stack.getCount();
  }

  public static ListNBT getEnchantmentNBT(ItemStack stack) {
    return Items.ENCHANTED_BOOK == stack.getItem()
        ? EnchantedBookItem.getEnchantments(stack)
        : stack.getEnchantmentTags();
  }

  public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
    val tags = getEnchantmentNBT(stack);
    val enchants = Maps.<Enchantment, Integer>newHashMap();
    for (val nbt : tags) {
      if (nbt instanceof CompoundNBT) {
        val compound = (CompoundNBT) nbt;
        enchants.put(lookupEnchantmentId(compound.getString("id")), compound.getInt("lvl"));
      }
    }
    return enchants;
  }

  public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
    return getEnchantmentNBT(stack).stream()
        .filter(CompoundNBT.class::isInstance)
        .map(CompoundNBT.class::cast)
        .map(comp -> new Tuple<>(lookupEnchantmentId(comp.getString("id")), comp.getInt("lvl")))
        // must match the enchantment we are looking for
        // this will filter out nulls as well
        .filter(tuple -> enchantment.equals(tuple.getFirst()))
        // get the enchantment level
        .map(Tuple::getSecond)
        .findAny()
        // if none found, enchantment level 0 will indicate no enchantment
        .orElse(0);
  }

  public static boolean hasEnchantment(ItemStack stack, Enchantment enchantment) {
    return getEnchantmentLevel(stack, enchantment) > 0;
  }

  public static int compareEnchantments(ItemStack stackA, ItemStack stackB) {
    val enchantsA = getEnchantments(stackA);
    val enchantsB = getEnchantments(stackB);

    int n = 0;
    for (val e : enchantsA.entrySet()) {
      int otherLvl = enchantsB.getOrDefault(e.getKey(), 0);
      n += otherLvl - e.getValue();
    }
    return n;
  }

  public static List<ItemStack> getShulkerContents(ItemStack stack) {
    CompoundNBT compound = stack.getTag();
    if (compound != null && compound.contains("BlockEntityTag", 10)) {
      CompoundNBT tags = compound.getCompound("BlockEntityTag");
      if (tags.contains("Items", 9)) {
        // load in the items
        NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tags, contents);
        return contents;
      }
    }
    return Collections.emptyList();
  }

  public static double getDiggingSpeed(ItemStack stack, PlayerEntity player, BlockState blockState, BlockPos blockPos) {
    float speed = stack.getDestroySpeed(blockState);
    int efficiencyLevel = getEnchantmentLevel(stack, Enchantments.BLOCK_EFFICIENCY);
    // this is from mojangs own algorithm
    return blockState.getDestroyProgress(player, player.level, blockPos) > 0.D
        ? Math.max(speed + (speed > 1.D ? (efficiencyLevel * efficiencyLevel + 1.D) : 0.D), 0.D)
        : 1.D;
  }

  public static double getAttackDamage(ItemStack stack) {
    Collection<AttributeModifier> attr = stack.getAttributeModifiers(EquipmentSlotType.MAINHAND)
        .get(Attributes.ATTACK_DAMAGE);
    return !attr.isEmpty() ? attr.iterator().next().getAmount() : 0.D;
  }

  public static double getAttackSpeed(ItemStack stack) {
    Collection<AttributeModifier> attr = stack.getAttributeModifiers(EquipmentSlotType.MAINHAND)
        .get(Attributes.ATTACK_SPEED);
    return !attr.isEmpty() ? Math.abs(attr.iterator().next().getAmount()) : 0.D;
  }

  public static double getEntityAttackModifier(ItemStack stack, @Nullable Entity target) {
    return EnchantmentHelper.getDamageBonus(stack, EntityEx.isLivingType(target)
        ? ((LivingEntity) target).getMobType()
        : CreatureAttribute.UNDEFINED);
  }

  public static double getAttackDamageInterval(ItemStack stack, Entity target) {
    return (getAttackDamage(stack) + getEntityAttackModifier(stack, target))
        / (getAttackSpeed(stack) + 1.D);
  }

  //
  //
  //

  private static boolean isLocalPlayerInventory(IInventory inventory) {
    return inventory instanceof PlayerInventory
        && ((PlayerInventory) inventory).player instanceof ClientPlayerEntity;
  }

  public static int getSlotNumber(Slot slot) {
    return slot.index;
  }

  public static ItemStack click(Slot slot, ClickType clickType, int mouseButton) {
    if (isLocalPlayerInventory(slot.container)) {
      val inv = (PlayerInventory) slot.container;
      val lp = (ClientPlayerEntity) inv.player;
      return LocalPlayerEx.sendWindowClick(lp, getSlotNumber(slot), mouseButton, clickType);
    } else {
      log.warn("Cannot select slot for non local player inventory!");
      return ItemStack.EMPTY;
    }
  }

  public static ItemStack swap(Slot slot, Slot otherSlot) {
    if (!isSwappable(otherSlot)) {
      if (isSwappable(slot)) {
        // swap argument order and call again
        return swap(otherSlot, slot);
      } else {
        log.warn("Slots #{} and #{} are not swappable", getSlotNumber(slot), getSlotNumber(otherSlot));
        return ItemStack.EMPTY;
      }
    } else {
      return click(slot, ClickType.SWAP, getHotbarIndex(otherSlot));
    }
  }

  public static boolean isInHotbar(Slot slot) {
    return isLocalPlayerInventory(slot.container)
        && getSlotNumber(slot) >= 36 && getSlotNumber(slot) < 45;
  }

  public static boolean isInOffhand(Slot slot) {
    return isLocalPlayerInventory(slot.container)
        && getSlotNumber(slot) == 45;
  }

  public static boolean isSwappable(Slot slot) {
    return isInHotbar(slot) || isInOffhand(slot);
  }

  public static int getHotbarIndex(Slot slot) {
    if (isInHotbar(slot)) {
      return slot.getSlotIndex();
    } else if (isInOffhand(slot)) {
      // this is a hardcoded number used by mojang, i have no idea why its 40
      return 40;
    }
    throw new IllegalStateException("Slot #" + getSlotNumber(slot) + " is not in hotbar");
  }

  public static Slot toImmutable(Slot slot) {
    return new ImmutableSlot(slot);
  }

  public static boolean isEqual(Slot slotA, Slot slotB) {
    return slotB != null
        && getSlotNumber(slotA) == getSlotNumber(slotB)
        && ItemStack.isSame(slotA.getItem(), slotB.getItem());
  }

  public static int getDistanceFromSelected(Slot slot) {
    if (isLocalPlayerInventory(slot.container)) {
      val inv = (PlayerInventory) slot.container;
      val lp = (ClientPlayerEntity) inv.player;
      return Math.abs(getSlotNumber(LocalPlayerEx.getSelectedSlot(lp)) - getSlotNumber(slot));
    } else {
      log.warn("Slot is not for a local players inventory!");
      return 0;
    }
  }
}
