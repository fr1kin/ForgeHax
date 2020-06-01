package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.draw.SurfaceHelper;

@RegisterMod
public class Watermark extends ToggleMod {

  public Watermark() {
    super(Category.GUI, "Watermark", true, "Display a watermark on your screen");
  }

  private final Setting<String> text =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("text")
      .description("Watermark text")
      .defaultTo("ForgeHax > all")
      .build();

  private final Setting<Double> x_offset =
    getCommandStub()
      .builders()
      .<Double>newSettingBuilder()
      .name("x_offset")
      .description("Offset on x axis")
      .defaultTo(80)
      .build();

  private final Setting<Double> y_offset =
    getCommandStub()
      .builders()
      .<Double>newSettingBuilder()
      .name("y_offset")
      .description("Offset on y axis")
      .defaultTo(8)
      .build();

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    SurfaceHelper.drawText(text.get(), x_offset.get(), y_offset.get(), Colors.BETTER_PURPLE.toBuffer());
  }
}
