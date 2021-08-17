package dev.fiki.forgehax.main.mods.ui;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.math.AlignHelper;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.HudMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.client.entity.player.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "CoordsHUD",
    description = "Display world coords",
    category = Category.UI
)
public class CoordsHud extends HudMod {
  private final BooleanSetting translate = newBooleanSetting()
      .name("translate")
      .description("show corresponding Nether or Overworld coords")
      .defaultTo(true)
      .build();

  private final BooleanSetting multiline = newBooleanSetting()
      .name("multiline")
      .description("show translated coords above")
      .defaultTo(true)
      .build();

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.BOTTOMRIGHT;
  }

  @Override
  protected int getDefaultOffsetX() {
    return 1;
  }

  @Override
  protected int getDefaultOffsetY() {
    return 1;
  }

  @Override
  protected double getDefaultScale() {
    return 1d;
  }

  double thisX;
  double thisY;
  double thisZ;
  double otherX;
  double otherZ;

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    ClientPlayerEntity player = getLocalPlayer();
    thisX = player.getX();
    thisY = player.getY();
    thisZ = player.getZ();

    double thisFactor = player.clientLevel.dimensionType().coordinateScale();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }

  @SubscribeListener
  public void onRenderOverlay(RenderPlaneEvent.Back event) {
    List<String> text = new ArrayList<>();

    if (!translate.getValue() || (translate.getValue() && multiline.getValue())) {
      text.add(String.format("%01.1f, %01.0f, %01.1f", thisX, thisY, thisZ));
    }
    if (translate.getValue()) {
      if (multiline.getValue()) {
        text.add(String.format("(%01.1f, %01.1f)", otherX, otherZ));
      } else {
        text.add(String.format(
            "%01.1f, %01.0f, %01.1f (%01.1f, %01.1f)", thisX, thisY, thisZ, otherX, otherZ));
      }
    }

    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.getValue(), true, alignment.getValue().ordinal());
  }
}
