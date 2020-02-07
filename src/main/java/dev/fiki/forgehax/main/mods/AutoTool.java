package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.PlayerAttackEntityEvent;
import dev.fiki.forgehax.common.events.PlayerDamageBlockEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.BlockHelper;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.enchantment.Enchantments.EFFICIENCY;

@RegisterMod
public class AutoTool extends ToggleMod {

  private static AutoTool instance = null;

  public static AutoTool getInstance() {
    return instance;
  }

  private final BooleanSetting tools = newBooleanSetting()
      .name("tools")
      .description("Enables AutoTool when tools")
      .defaultTo(true)
      .build();

  private final BooleanSetting weapons = newBooleanSetting()
      .name("weapons")
      .description("Enables AutoTool for weapons")
      .defaultTo(true)
      .build();

  private final BooleanSetting revert_back = newBooleanSetting()
      .name("revert-back")
      .description("Revert back to the previous item")
      .defaultTo(true)
      .build();

  private final IntegerSetting durability_threshold = newIntegerSetting()
      .name("durability-threshold")
      .description("Will filter out items with a damage equal to or less than the threshold. Set to 0 to disable.")
      .defaultTo(0)
      .min(0)
      .max((int) Short.MAX_VALUE)
      .build();

  public AutoTool() {
    super(Category.PLAYER, "AutoTool", false, "Automatically switch to the best tool");
    instance = this;
  }

  private boolean isInvincible(LocalPlayerInventory.InvItem item) {
    return item.isNull() || !item.getItem().isDamageable();
  }

  private boolean isDurabilityGood(LocalPlayerInventory.InvItem item) {
    return durability_threshold.getValue() < 1
        || isInvincible(item)
        || item.getDurability() > durability_threshold.getValue();
  }

  private boolean isSilkTouchable(LocalPlayerInventory.InvItem item, BlockState state, BlockPos pos) {
    // TODO: 1.15 have to figure out if an item can be silk touched by looking it up in the loot tables now
    return false;
//    return LocalPlayerInventory.getSelected().getIndex() == item.getIndex()
//        && getEnchantmentLevel(Enchantments.SILK_TOUCH, item) > 0
//        && state.getBlock().canSilkHarvest(getWorld(), pos, state, getLocalPlayer());
  }

  private double getDigSpeed(LocalPlayerInventory.InvItem item, BlockState state, BlockPos pos) {
    double str = item.getItemStack().getDestroySpeed(state);
    int eff = getEnchantmentLevel(EFFICIENCY, item);
    return state.getPlayerRelativeBlockHardness(Common.getLocalPlayer(), Common.getWorld(), pos) > 0.D
        ? Math.max(str + (str > 1.D ? (eff * eff + 1.D) : 0.D), 0.D)
        : 1.D;
  }

  private double getAttackDamage(LocalPlayerInventory.InvItem item) {
    return Optional.ofNullable(
        item.getItemStack()
            .getAttributeModifiers(EquipmentSlotType.MAINHAND)
            .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()))
        .map(at -> at.stream().findAny().map(AttributeModifier::getAmount).orElse(0.D))
        .orElse(0.D);
  }

  private double getAttackSpeed(LocalPlayerInventory.InvItem item) {
    return Optional.ofNullable(item.getItemStack()
        .getAttributeModifiers(EquipmentSlotType.MAINHAND)
        .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()))
        .map(at -> at.stream()
            .findAny()
            .map(AttributeModifier::getAmount)
            .map(Math::abs)
            .orElse(0.D))
        .orElse(0.D);
  }

  private double getEntityAttackModifier(LocalPlayerInventory.InvItem item, Entity target) {
    return EnchantmentHelper.getModifierForCreature(
        item.getItemStack(),
        Optional.ofNullable(target)
            .filter(LivingEntity.class::isInstance)
            .map(LivingEntity.class::cast)
            .map(LivingEntity::getCreatureAttribute)
            .orElse(CreatureAttribute.UNDEFINED));
  }

  private double calculateDPS(LocalPlayerInventory.InvItem item, Entity target) {
    return (getAttackDamage(item) + 1.D + getEntityAttackModifier(item, target))
        / (getAttackSpeed(item) + 1.D);
  }

  private int getEnchantmentLevel(Enchantment enchantment, LocalPlayerInventory.InvItem item) {
    return EnchantmentHelper.getEnchantmentLevel(enchantment, item.getItemStack());
  }

  private LocalPlayerInventory.InvItem getBestTool(BlockPos pos) {
    LocalPlayerInventory.InvItem current = LocalPlayerInventory.getSelected();

    if (!BlockHelper.isBlockPlaceable(pos) || Common.getWorld().isAirBlock(pos)) {
      return current;
    }

    final BlockState state = Common.getWorld().getBlockState(pos);
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(this::isDurabilityGood)
        .max(
            Comparator.<LocalPlayerInventory.InvItem>comparingDouble(item -> getDigSpeed(item, state, pos))
                .thenComparing(item -> isSilkTouchable(item, state, pos))
                .thenComparing(this::isInvincible)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .orElse(current);
  }

  private LocalPlayerInventory.InvItem getBestWeapon(Entity target) {
    LocalPlayerInventory.InvItem current = LocalPlayerInventory.getSelected();
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(this::isDurabilityGood)
        .max(
            Comparator.<LocalPlayerInventory.InvItem>comparingDouble(item -> calculateDPS(item, target))
                .thenComparing(item -> getEnchantmentLevel(Enchantments.FIRE_ASPECT, item))
                .thenComparing(item -> getEnchantmentLevel(Enchantments.SWEEPING, item))
                .thenComparing(this::isInvincible)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .orElse(current);
  }

  public void selectBestTool(BlockPos pos) {
    if (isEnabled() && tools.getValue()) {
      LocalPlayerInventory.setSelected(getBestTool(pos), revert_back.getValue(), ticks -> ticks > 5);
    }
  }

  public void selectBestWeapon(Entity target) {
    if (isEnabled() && weapons.getValue()) {
      LocalPlayerInventory.setSelected(
          getBestWeapon(target),
          revert_back.getValue(),
          ticks -> Common.getLocalPlayer().getCooledAttackStrength(0.f) >= 1.f && ticks > 30);
    }
  }

  @SubscribeEvent
  public void onBlockBreak(PlayerDamageBlockEvent event) {
    selectBestTool(event.getPos());
  }

  @SubscribeEvent
  public void onAttackEntity(PlayerAttackEntityEvent event) {
    selectBestWeapon(event.getVictim());
  }
}
