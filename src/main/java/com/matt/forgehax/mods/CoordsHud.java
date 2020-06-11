package com.matt.forgehax.mods;

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

import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getPlayerDirection;

@RegisterMod
public class CoordsHud extends HudMod {

  public CoordsHud() {
    super(Category.GUI, "Coords", false, "Displays your current coordinates");
  }

  private final Setting<Boolean> translate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("translate")
          .description("Show corresponding Nether or Overworld coords")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> multiline =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("multiline")
          .description("Show translated coords above")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> direction =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("direction")
          .description("Show the facing value")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> viewEntity =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("view-entity")
          .description("Show the current coords for viewentity (freecam)")
          .defaultTo(false)
          .build();

  @Override
  protected Align getDefaultAlignment() {
    return Align.BOTTOMRIGHT;
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

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent ev) {
    if (MC.world == null) {
      return;
    }

    Entity entity = getEntity();
    thisX = entity.posX;
    thisY = entity.posY;
    thisZ = entity.posZ;

    double thisFactor = MC.world.provider.getMovementFactor();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();

    // Direction
    String facingNormal = String.format("%s " + "[%s]", facingTable[getPlayerDirection()], towardsTable[getPlayerDirection()]);

    // Multiline coords + direction
    String facingWithTCoords = String.format("%s " + "[%s] (%01.1f, %01.1f)",
        facingTable[getPlayerDirection()], towardsTable[getPlayerDirection()], otherX, otherZ);

    // Only OW coords
    String coordsNormal = String.format("%01.1f, %01.1f, %01.1f", thisX, thisY, thisZ);

    // Multiline Nether coords
    String coordsMultiTranslated = String.format("(%01.1f, %01.1f)", otherX, otherZ);

    // Single line OW + Nether coords
    String coordsTranslated = String.format(
        "%01.1f, %01.1f, %01.1f (%01.1f, %01.1f)", thisX, thisY, thisZ, otherX, otherZ);

    if (!translate.get()
        || (translate.get() && multiline.get())
        || (translate.get() && MC.player.dimension == 1)) {
      text.add(coordsNormal); // x, y, z

      if (direction.get()) {
        if (!multiline.get()
            || !translate.get() && multiline.get()
            || (translate.get() && MC.player.dimension == 1)) {
          text.add(facingNormal); // Facing [f]
        }
      }
    }

    if (translate.get() && MC.player.dimension != 1) {
      if (multiline.get()) {
        if (direction.get()) {
          text.add(facingWithTCoords); // Facing (tx, tz)
        } else {
          text.add(coordsMultiTranslated); // (tx, tz)
        }
      } else {
        text.add(coordsTranslated); // x, y, z (tx, tz)

        if (direction.get()) {
          text.add(facingNormal); // Facing [f]
        }
      }
    }

    // Printing
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());
  }

  private final String[] facingTable = {
      "South",
      "South West",
      "West",
      "North West",
      "North",
      "North East",
      "East",
      "South East"
  };

  private final String[] towardsTable = {
      "+Z",
      "-X +Z",
      "-X",
      "-X -Z",
      "-Z",
      "+X -Z",
      "+X",
      "+X +Z"
  };

  public Entity getEntity() {
    return viewEntity.get() ? MC.getRenderViewEntity() : MC.player;
  }
}
