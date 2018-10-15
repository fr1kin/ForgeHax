package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;
import static net.minecraft.init.Enchantments.EFFICIENCY;

import com.matt.forgehax.asm.events.PlayerAttackEntityEvent;
import com.matt.forgehax.asm.events.PlayerDamageBlockEvent;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Collection;
import java.util.Comparator;
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

  public AutoTool() {
    super(Category.PLAYER, "AutoTool", false, "Automatically switch to the best tool");
    instance = this;
  }

  private double getDigSpeed(InvItem item, IBlockState state) {
    double str = item.getItemStack().getStrVsBlock(state);
    int eff = getEnchantmentLevel(EFFICIENCY, item);
    return Math.max(str + (str > 1.D ? (eff * eff + 1.D) : 0.D), 0.D);
  }

  private double getAttackDamage(InvItem item) {
    Collection<AttributeModifier> attributes =
        item.getItem()
            .getAttributeModifiers(EntityEquipmentSlot.MAINHAND, item.getItemStack())
            .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
    return attributes == null
        ? 0.D
        : attributes.stream().findAny().map(AttributeModifier::getAmount).orElse(0.D);
  }

  private double getAttackSpeed(InvItem item) {
    Collection<AttributeModifier> attributes =
        item.getItem()
            .getAttributeModifiers(EntityEquipmentSlot.MAINHAND, item.getItemStack())
            .get(SharedMonsterAttributes.ATTACK_SPEED.getName());
    return attributes == null
        ? 0.D
        : attributes
            .stream()
            .findAny()
            .map(AttributeModifier::getAmount)
            .map(Math::abs)
            .orElse(0.D);
  }

  private double getEntityAttackModifier(InvItem item, Entity target) {
    return EnchantmentHelper.getModifierForCreature(
        item.getItemStack(),
        target instanceof EntityLivingBase
            ? ((EntityLivingBase) target).getCreatureAttribute()
            : EnumCreatureAttribute.UNDEFINED);
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
        .max(
            Comparator.<InvItem>comparingDouble(item -> getDigSpeed(item, state))
                .thenComparing(item -> !item.getItem().isDamageable())
                .thenComparing(item -> 8 - Math.abs(current.getIndex() - item.getIndex())))
        .orElse(current);
  }

  private InvItem getBestWeapon(Entity target) {
    InvItem current = LocalPlayerInventory.getSelected();
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(InvItem::nonNull)
        .max(
            Comparator.<InvItem>comparingDouble(
                    item ->
                        (getAttackDamage(item) + 1.D + getEntityAttackModifier(item, target))
                            / (getAttackSpeed(item) + 1.D))
                .thenComparing(
                    item ->
                        EnchantmentHelper.getEnchantmentLevel(
                            Enchantments.FIRE_ASPECT, item.getItemStack()))
                .thenComparing(
                    item ->
                        EnchantmentHelper.getEnchantmentLevel(
                            Enchantments.SWEEPING, item.getItemStack()))
                .thenComparing(item -> !item.getItem().isDamageable())
                .thenComparing(item -> 8 - Math.abs(current.getIndex() - item.getIndex())))
        .orElse(current);
  }

  public InvItem selectBestTool(BlockPos pos) {
    if (isEnabled()) LocalPlayerInventory.setSelected(getBestTool(pos));
    return LocalPlayerInventory.getSelected();
  }

  public InvItem selectBestWeapon(Entity target) {
    if (isEnabled()) LocalPlayerInventory.setSelected(getBestWeapon(target));
    return LocalPlayerInventory.getSelected();
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
