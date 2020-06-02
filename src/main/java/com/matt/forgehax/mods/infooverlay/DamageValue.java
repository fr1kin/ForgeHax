package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.item.ItemStack;

@RegisterMod
public class DamageValue extends ToggleMod {

  public DamageValue() {
    super(Category.GUI, "DamageValue", true, "Shows damage value in main or offhand");
  }

  public enum Modes {
    MAINHAND,
    OFFHAND,
    BOTH
  }

  public final Setting<Modes> damageValueMode =
    getCommandStub()
      .builders()
      .<Modes>newSettingEnumBuilder()
      .name("mode")
      .description("")
      .defaultTo(Modes.MAINHAND)
      .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  @Override
  public boolean notInList() {
	return true;
  }

  public String getInfoDisplayText() {
    StringBuilder builderDamage = new StringBuilder("Damage value: ");
    ItemStack itemStackM = MC.player.getHeldItemMainhand();
    ItemStack itemStackO = MC.player.getHeldItemOffhand();

    switch (damageValueMode.get()){
      case MAINHAND: {
        builderDamage.append(String.format("%s", itemStackM.getMaxDamage() - itemStackM.getItemDamage()));
        break;
      }
      case OFFHAND: {
        builderDamage.append(String.format("[%s]", itemStackO.getMaxDamage() - itemStackO.getItemDamage()));
        break;
      }
      case BOTH: {
        builderDamage.append(String.format("%s [%s]", (itemStackM.getMaxDamage() - itemStackM.getItemDamage()),
          (itemStackO.getMaxDamage() - itemStackO.getItemDamage())));
        break;
      }
      default: {
        builderDamage.append("INVALID");
      }
    }

    return builderDamage.toString();
  }
}
