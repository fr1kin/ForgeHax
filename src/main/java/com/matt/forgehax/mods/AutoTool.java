package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static net.minecraft.init.Enchantments.EFFICIENCY;

import com.matt.forgehax.asm.events.PlayerAttackEntityEvent;
import com.matt.forgehax.asm.events.PlayerDamageBlockEvent;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoTool extends ToggleMod {
  private static AutoTool instance = null;

  public static AutoTool getInstance() {
    return instance;
  }

  private final Setting<Boolean> tools =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("tools")
          .description("Enables AutoTool when tools")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> weapons =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("weapons")
          .description("Enables AutoTool for weapons")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> revert_back =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("revert-back")
          .description("Revert back to the previous item")
          .defaultTo(true)
          .build();

  private final Setting<Integer> durability_threshold =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("durability-threshold")
          .description(
              "Will filter out items with a damage equal to or less than the threshold. Set to 0 to disable.")
          .defaultTo(0)
          .min(0)
          .max((int) Short.MAX_VALUE)
          .build();

  public AutoTool() {
    super(Category.PLAYER, "AutoTool", false, "Automatically switch to the best tool");
    instance = this;
  }

  private boolean isInvincible(InvItem item) {
    return item.isNull() || !item.getItem().isDamageable();
  }

  private boolean isDurabilityGood(InvItem item) {
    return durability_threshold.get() < 1
        || isInvincible(item)
        || (item.getItemStack().getMaxDamage() - item.getItemStack().getItemDamage())
            > durability_threshold.get();
  }

  private boolean isSilkTouchable(InvItem item, IBlockState state, BlockPos pos) {
    return LocalPlayerInventory.getSelected().getIndex() == item.getIndex()
        && getEnchantmentLevel(Enchantments.SILK_TOUCH, item) > 0
        && state.getBlock().canSilkHarvest(getWorld(), pos, state, getLocalPlayer());
  }

  private double getDigSpeed(InvItem item, IBlockState state, BlockPos pos) {
    double str = item.getItemStack().getStrVsBlock(state);
    int eff = getEnchantmentLevel(EFFICIENCY, item);
    return state.getBlockHardness(getWorld(), pos) > 0.D
        ? Math.max(str + (str > 1.D ? (eff * eff + 1.D) : 0.D), 0.D)
        : 1.D;
  }

  private double getAttackDamage(InvItem item) {
    return Optional.ofNullable(
            item.getItemStack()
                .getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()))
        .map(at -> at.stream().findAny().map(AttributeModifier::getAmount).orElse(0.D))
        .orElse(0.D);
  }

  private double getAttackSpeed(InvItem item) {
    return Optional.ofNullable(
            item.getItemStack()
                .getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()))
        .map(
            at ->
                at.stream().findAny().map(AttributeModifier::getAmount).map(Math::abs).orElse(0.D))
        .orElse(0.D);
  }

  private double getEntityAttackModifier(InvItem item, Entity target) {
    return EnchantmentHelper.getModifierForCreature(
        item.getItemStack(),
        Optional.ofNullable(target)
            .filter(EntityLivingBase.class::isInstance)
            .map(EntityLivingBase.class::cast)
            .map(EntityLivingBase::getCreatureAttribute)
            .orElse(EnumCreatureAttribute.UNDEFINED));
  }

  private double calculateDPS(InvItem item, Entity target) {
    return (getAttackDamage(item) + 1.D + getEntityAttackModifier(item, target))
        / (getAttackSpeed(item) + 1.D);
  }

  private int getEnchantmentLevel(Enchantment enchantment, InvItem item) {
    return EnchantmentHelper.getEnchantmentLevel(enchantment, item.getItemStack());
  }

  private InvItem getBestTool(BlockPos pos) {
    InvItem current = LocalPlayerInventory.getSelected();

    if (!BlockHelper.isBlockPlaceable(pos) || getWorld().isAirBlock(pos)) return current;

    final IBlockState state = getWorld().getBlockState(pos);
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(this::isDurabilityGood)
        .max(
            Comparator.<InvItem>comparingDouble(item -> getDigSpeed(item, state, pos))
                .thenComparing(item -> isSilkTouchable(item, state, pos))
                .thenComparing(this::isInvincible)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .orElse(current);
  }

  private InvItem getBestWeapon(Entity target) {
    InvItem current = LocalPlayerInventory.getSelected();
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(this::isDurabilityGood)
        .max(
            Comparator.<InvItem>comparingDouble(item -> calculateDPS(item, target))
                .thenComparing(item -> getEnchantmentLevel(Enchantments.FIRE_ASPECT, item))
                .thenComparing(item -> getEnchantmentLevel(Enchantments.SWEEPING, item))
                .thenComparing(this::isInvincible)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .orElse(current);
  }

  public void selectBestTool(BlockPos pos) {
    if (isEnabled() && tools.get())
      LocalPlayerInventory.setSelected(getBestTool(pos), revert_back.get(), ticks -> ticks > 5);
  }

  public void selectBestWeapon(Entity target) {
    if (isEnabled() && weapons.get())
      LocalPlayerInventory.setSelected(
          getBestWeapon(target),
          revert_back.get(),
          ticks -> getLocalPlayer().getCooledAttackStrength(0.f) >= 1.f && ticks > 30);
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
