package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class CoordsHud extends HudMod {
  
  public CoordsHud() {
    super(Category.GUI, "CoordsHUD", false, "Display world coords");
  }
  
  private final Setting<Boolean> translate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("translate")
          .description("show corresponding Nether or Overworld coords")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> toniostyle =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("toniostyle")
          .description("Makes coords fancier (IMO)")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> multiline =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("multiline")
          .description("show translated coords above")
          .defaultTo(true)
          .build();
  
  @Override
  protected Align getDefaultAlignment() { return Align.BOTTOMRIGHT; }
  @Override
  protected int getDefaultOffsetX() { return 1; }
  @Override
  protected int getDefaultOffsetY() { return 1; }
  @Override
  protected double getDefaultScale() { return 1d; }
  
  double thisX;
  double thisY;
  double thisZ;
  double otherX;
  double otherZ;
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent ev) {
    if (getWorld() == null) return;
  
    EntityPlayerSP player = getLocalPlayer();
    thisX = player.posX;
    thisY = player.posY;
    thisZ = player.posZ;
    
    double thisFactor = getWorld().provider.getMovementFactor();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }
  
  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();
   	if (toniostyle.get()) {
      if (!translate.get() || (translate.get() && multiline.get())) {
        text.add(String.format("[ X %.1f | %.1f Z ] (%.0f Y)", thisX, thisZ, thisY));
      }
      if (translate.get()) {
        if (multiline.get()) {
          text.add(String.format("[ %.1f | %.1f ]", otherX, otherZ));
        } else {
          text.add(String.format(
              "[ X %.1f | %.1f Z ] (%.0f Y)      %.1f | %.1f", thisX, thisZ, thisY, otherX, otherZ));
        }
      }
	} else { 
      if (!translate.get() || (translate.get() && multiline.get())) {
        text.add(String.format("%01.1f, %01.0f, %01.1f", thisX, thisY, thisZ));
      }
      if (translate.get()) {
        if (multiline.get()) {
          text.add(String.format("(%01.1f, %01.1f)", otherX, otherZ));
        } else {
          text.add(String.format(
              "%01.1f, %01.0f, %01.1f (%01.1f, %01.1f)", thisX, thisY, thisZ, otherX, otherZ));
        }
      }
	}
    
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());
  }
}
