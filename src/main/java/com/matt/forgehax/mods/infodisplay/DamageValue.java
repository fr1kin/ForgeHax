package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

@RegisterMod
public class DamageValue extends ToggleMod {

  public DamageValue() {
    super(Category.GUI, "DamageValue", true, "Shows damage value in main or offhand");
  }

  public final Setting<Boolean> mainhand =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("mainhand")
          .description("Show the damage value for the current item in main hand")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> offhand =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("offhand")
          .description("Show the damage value for the current item in off hand")
          .defaultTo(true)
          .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  public String getInfoDisplayText() {
    StringBuilder builderDamage = new StringBuilder("Damage value: ");
    ItemStack itemStackM = MC.player.getHeldItemMainhand();
    ItemStack itemStackO = MC.player.getHeldItemOffhand();

    if (mainhand.get()) {
      builderDamage.append(String.format("%s", itemStackM.getMaxDamage() - itemStackM.getItemDamage()));
    }

    if (offhand.get()) {
      if (mainhand.get()) {
        builderDamage.append(" ");
      }
      builderDamage.append(String.format(TextFormatting.GRAY + "[%s]" + TextFormatting.WHITE, itemStackO.getMaxDamage() - itemStackO.getItemDamage()));
    }

    return builderDamage.toString();
  }
}
