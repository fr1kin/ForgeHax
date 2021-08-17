package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.player.PlayerAttackEntityEvent;
import dev.fiki.forgehax.asm.events.player.PlayerDamageBlockEvent;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoTool",
    description = "Automatically switch to the best tool",
    category = Category.PLAYER
)
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class})
public class AutoTool extends ToggleMod {
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

  private final BooleanSetting revertBack = newBooleanSetting()
      .name("revert-back")
      .description("Revert back to the previous item")
      .defaultTo(true)
      .build();

  private final IntegerSetting durabilityThreshold = newIntegerSetting()
      .name("durability-threshold")
      .description("Will filter out items with a damage equal to or less than the threshold. Set to 0 to disable.")
      .defaultTo(0)
      .min(0)
      .max((int) Short.MAX_VALUE)
      .build();

  private boolean isInvincible(ItemStack stack) {
    // can't break nothing (hand)
    return !stack.isEmpty() || !stack.isDamageableItem();
  }

  private boolean isDurabilityGood(ItemStack stack) {
    return durabilityThreshold.getValue() < 1
        || isInvincible(stack)
        || stack.getDurability() > durabilityThreshold.getValue();
  }

  private boolean isDurabilityGood(Slot slot) {
    return isDurabilityGood(slot.getItem());
  }

  private Slot getBestTool(BlockPos pos) {
    val lp = getLocalPlayer();
    // we should be able to mine
    if (!lp.canPlaceBlocksAt(pos) && !lp.getWorld().isEmptyBlock(pos)) {
      return lp.getHotbarSlots().stream()
          .filter(this::isDurabilityGood)
          // prefer the fastest digging tool
          .max(Comparator.comparing(Slot::getItem,
              Comparator.<ItemStack>comparingDouble(stack -> lp.getDiggingSpeedAt(stack, pos))
                  // if all the same speed, then choose a tool that will not lose durability
                  .thenComparing(this::isInvincible))
              // find the tool closest in the hotbar
              .thenComparing(ItemEx::getDistanceFromSelected, Comparator.reverseOrder()))
          .orElse(lp.getSelectedSlot());
    }
    return lp.getSelectedSlot();
  }

  private Slot getBestWeapon(Entity target) {
    val lp = getLocalPlayer();
    return lp.getHotbarSlots().stream()
        .filter(this::isDurabilityGood)
        .max(Comparator.comparing(Slot::getItem,
            Comparator.<ItemStack>comparingDouble(stack -> stack.getAttackDamageInterval(target))
                .thenComparingInt(stack -> stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT))
                .thenComparingInt(stack -> stack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE))
                .thenComparing(this::isInvincible))
            .thenComparing(ItemEx::getDistanceFromSelected, Comparator.reverseOrder()))
        .orElse(lp.getSelectedSlot());
  }

  public void selectBestTool(BlockPos pos) {
    if (isEnabled() && tools.getValue()) {
      getLocalPlayer().setSelectedSlot(getBestTool(pos), revertBack.isEnabled(), ticks -> ticks > 5);
    }
  }

  public void selectBestWeapon(Entity target) {
    if (isEnabled() && weapons.getValue()) {
      val lp = getLocalPlayer();
      lp.setSelectedSlot(getBestWeapon(target), revertBack.isEnabled(),
          ticks -> lp.getAttackStrengthScale(0.f) >= 1.f && ticks > 30);
    }
  }

  @SubscribeListener
  public void onBlockBreak(PlayerDamageBlockEvent event) {
    selectBestTool(event.getPos());
  }

  @SubscribeListener
  public void onAttackEntity(PlayerAttackEntityEvent event) {
    selectBestWeapon(event.getVictim());
  }
}
