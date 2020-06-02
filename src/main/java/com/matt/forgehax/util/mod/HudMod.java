package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.math.AlignHelper.Align;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class HudMod extends ToggleMod {
  
  protected static ScaledResolution scaledRes = new ScaledResolution(MC);
  
  protected final Setting<Align> alignment;
  protected final Setting<Integer> offsetX;
  protected final Setting<Integer> offsetY;
  protected final Setting<Double> scale;
  
  protected abstract Align getDefaultAlignment();
  protected abstract int getDefaultOffsetX();
  protected abstract int getDefaultOffsetY();
  protected abstract double getDefaultScale();
  
  public HudMod(Category category, String modName, boolean defaultEnabled, String description) {
    super(category, modName, defaultEnabled, description);
    
    this.alignment =
        getCommandStub()
            .builders()
            .<Align>newSettingEnumBuilder()
            .name("alignment")
            .description("align to corner")
            .defaultTo(getDefaultAlignment())
            .build();
  
    this.offsetX =
        getCommandStub()
            .builders()
            .<Integer>newSettingBuilder()
            .name("x-offset")
            .description("shift on X-axis")
            .defaultTo(getDefaultOffsetX())
            .build();
  
    this.offsetY =
        getCommandStub()
            .builders()
            .<Integer>newSettingBuilder()
            .name("y-offset")
            .description("shift on Y-axis")
            .defaultTo(getDefaultOffsetY())
            .build();
  
    this.scale =
        getCommandStub()
            .builders()
            .<Double>newSettingBuilder()
            .name("scale")
            .description("size scaling")
            .defaultTo(getDefaultScale())
            .build();
  }

  @Override
  public boolean notInList() {
	return true;
  }
  
  // no need to recalc each frame but okay (on GuiScale and Settings change only)
  public final int getPosX(int extraOffset) {
    final int align = alignment.get().ordinal();
    final int dirSignX = AlignHelper.getFlowDirX2(align);
    return (extraOffset + offsetX.get()) * dirSignX + AlignHelper.alignH(scaledRes.getScaledWidth(), align);
  }
  
  public final int getPosY(int extraOffset) {
    final int align = alignment.get().ordinal();
    final int dirSignY = AlignHelper.getFlowDirY2(align);
    return (extraOffset + offsetY.get()) * dirSignY + AlignHelper.alignV(scaledRes.getScaledHeight(), align);
  }
  
  @SubscribeEvent
  public void onScreenUpdated(GuiScreenEvent.InitGuiEvent.Post ev) {
    scaledRes = new ScaledResolution(MC);
  }
}
