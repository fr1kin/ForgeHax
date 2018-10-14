package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

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

  private double getDestroySpeed(ItemStack stack, IBlockState state) {
    return stack.getStrVsBlock(state)
        + Math.pow(EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack), 2);
  }

  private double getAttackDamage(Item item) {
    return item instanceof ItemSword
        ? Fields.ItemSword_attackDamage.get(item)
        : item instanceof ItemTool ? Fields.ItemTool_damageVsEntity.get(item) : 0.D;
  }

  private InvItem getBestTool(BlockPos pos) {
    InvItem current = LocalPlayerInventory.getSelected();

    if (!BlockHelper.isBlockPlaceable(pos) || getWorld().isAirBlock(pos)) return current;

    final IBlockState state = getWorld().getBlockState(pos);
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(InvItem::nonNull)
        .max(
            Comparator.<InvItem>comparingDouble(item -> getDestroySpeed(item.getItemStack(), state))
                .thenComparing(item -> !item.getItem().isDamageable())
                .thenComparing(current::equals))
        .orElse(current);
  }

  private InvItem getBestWeapon() {
    InvItem current = LocalPlayerInventory.getSelected();
    return LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(InvItem::nonNull)
        .max(
            Comparator.<InvItem>comparingDouble(item -> getAttackDamage(item.getItem()))
                .thenComparing(current::equals))
        .orElse(current);
  }

  public InvItem selectBestTool(BlockPos pos) {
    if (isEnabled()) LocalPlayerInventory.setSelected(getBestTool(pos));
    return LocalPlayerInventory.getSelected();
  }

  public InvItem selectBestWeapon() {
    if (isEnabled()) LocalPlayerInventory.setSelected(getBestWeapon());
    return LocalPlayerInventory.getSelected();
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    switch (event.phase) {
      case START:
        {
          RayTraceResult tr = MC.objectMouseOver;

          if (tr == null || !MC.gameSettings.keyBindAttack.isKeyDown()) return;

          switch (tr.typeOfHit) {
            case BLOCK:
              selectBestTool(tr.getBlockPos());
              break;
            case ENTITY:
              selectBestWeapon();
              break;
          }
          break;
        }
    }
  }
}
