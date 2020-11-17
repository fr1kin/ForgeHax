package dev.fiki.forgehax.api.mod;

import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.math.AlignHelper;
import dev.fiki.forgehax.api.math.AlignHelper.Align;
import dev.fiki.forgehax.main.Common;

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

  public HudMod() {
    super();
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
