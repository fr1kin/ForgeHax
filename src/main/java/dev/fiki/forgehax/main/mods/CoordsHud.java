package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.math.AlignHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.HudMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class CoordsHud extends HudMod {
  public CoordsHud() {
    super(Category.RENDER, "CoordsHUD", false, "Display world coords");
  }
  
  private final Setting<Boolean> translate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("translate")
          .description("show corresponding Nether or Overworld coords")
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
  protected AlignHelper.Align getDefaultAlignment() { return AlignHelper.Align.BOTTOMRIGHT; }
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
    ClientPlayerEntity player = Common.getLocalPlayer();
    thisX = player.getPosX();
    thisY = player.getPosY();
    thisZ = player.getPosZ();
    
    double thisFactor = Common.getWorld().getDimension().getMovementFactor();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }
  
  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();
    
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
    
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());
  }
}
