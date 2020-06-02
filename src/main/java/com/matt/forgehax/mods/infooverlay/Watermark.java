package com.matt.forgehax.mods.infooverlay;

import java.util.Random;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.color.Color;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@RegisterMod
public class Watermark extends HudMod {

  public Watermark() {
    super(Category.GUI, "Watermark", true, "Display a watermark on your screen");
  }

  Random random = new Random();
  private int color = 0;

  private final Setting<String> text =
    getCommandStub()
      .builders()
      .<String>newSettingBuilder()
      .name("text")
      .description("Watermark text")
      .defaultTo("ForgeHax 2.10.0")
      .build();

  private final Setting<Boolean> rainbow =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("rainbow")
      .description("Change color according to server ticks")
      .defaultTo(false)
      .build();

  @Override
  protected Align getDefaultAlignment() { return Align.TOPLEFT; }
  @Override
  protected int getDefaultOffsetX() { return 80; }
  @Override
  protected int getDefaultOffsetY() { return 15; }
  @Override
  protected double getDefaultScale() { return 1d; }

  @SubscribeEvent
  public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketTimeUpdate) {
	  color = Color.of(Math.random(), Math.random(), Math.random(), 1.0f).toBuffer();
    }
  }

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
	int align = alignment.get().ordinal();	
	int clr;
	if (rainbow.get()) clr = color;
	else clr = Colors.BETTER_PURPLE.toBuffer();
    SurfaceHelper.drawTextAlign(text.get(), getPosX(0), getPosY(0),
								clr, scale.get(), true, align);
  }
}
