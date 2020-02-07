package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.math.AlignHelper;
import dev.fiki.forgehax.main.util.math.AlignHelper.Align;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.MC;

public abstract class HudMod extends ToggleMod {
  protected final EnumSetting<Align> alignment = newEnumSetting(Align.class)
      .name("alignment")
      .description("align to corner")
      .defaultTo(getDefaultAlignment())
      .build();

  protected final IntegerSetting offsetX = newIntegerSetting()
      .name("x-offset")
      .description("shift on X-axis")
      .defaultTo(getDefaultOffsetX())
      .build();

  protected final IntegerSetting offsetY = newIntegerSetting()
      .name("y-offset")
      .description("shift on Y-axis")
      .defaultTo(getDefaultOffsetY())
      .build();

  protected final DoubleSetting scale = newDoubleSetting()
      .name("scale")
      .description("size scaling")
      .defaultTo(getDefaultScale())
      .build();
  
  protected abstract Align getDefaultAlignment();
  protected abstract int getDefaultOffsetX();
  protected abstract int getDefaultOffsetY();
  protected abstract double getDefaultScale();
  
  public HudMod(Category category, String modName, boolean defaultEnabled, String description) {
    super(category, modName, defaultEnabled, description);
  }
  
  // no need to recalc each frame but okay (on GuiScale and Settings change only)
  public final int getPosX(int extraOffset) {
    final int align = alignment.getValue().ordinal();
    final int dirSignX = AlignHelper.getFlowDirX2(align);
    return (extraOffset + offsetX.getValue()) * dirSignX + AlignHelper.alignH(Common.getScreenWidth(), align);
  }
  
  public final int getPosY(int extraOffset) {
    final int align = alignment.getValue().ordinal();
    final int dirSignY = AlignHelper.getFlowDirY2(align);
    return (extraOffset + offsetY.getValue()) * dirSignY + AlignHelper.alignV(Common.getScreenHeight(), align);
  }
}
