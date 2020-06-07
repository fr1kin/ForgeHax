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

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;

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
      .description("Show the player's facing value")
      .defaultTo(true)
      .build();

  public enum Mode {
    PLAYER,
    VIEWENTITY
  }

  public final Setting<Mode> mode =
    getCommandStub()
      .builders()
      .<Mode>newSettingEnumBuilder()
      .name("mode")
      .description("Player or viewentity coords (mainly for freecam)")
      .defaultTo(Mode.VIEWENTITY)
      .build();

  private final Setting<Boolean> brackets =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("brackets")
          .description("Changes the coords layout")
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

  int posY;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent ev) {
    if (MC.world == null) {
      return;
    }

    switch (mode.get()) {
      case VIEWENTITY: {
        Entity viewEntity = getRenderEntity();
        thisX = viewEntity.posX;
        thisY = viewEntity.posY;
        thisZ = viewEntity.posZ;
        break;
      }
      case PLAYER: {
        EntityPlayerSP player = getLocalPlayer();
        thisX = player.posX;
        thisY = player.posY;
        thisZ = player.posZ;
        break;
      }
    }

    double thisFactor = MC.world.provider.getMovementFactor();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();
	String facingNormal, facingWithTCoords, coordsNormal, coordsMultiTranslated, coordsTranslated;

    if (brackets.get()) {
      // Direction
      facingNormal = String.format("%s " + "[%s]", getFacing(), getTowards());
      // Multiline coords + direction
      facingWithTCoords = String.format("[ %.1f ⏐ %.1f ] - %s [%s]", otherX, otherZ, getFacing(), getTowards());
      // Only OW coords
      coordsNormal = String.format("[ X %.1f ⏐ %.1f Z ] (%.0f Y)", thisX, thisZ, thisY);
      // Multiline Nether coords
      coordsMultiTranslated = String.format("[ %.1f ⏐ %.1f ]", otherX, otherZ);
      // Single line OW + Nether coords
      coordsTranslated = String.format("[ X %.1f ⏐ %.1f Z ] (%.0f Y)  %.1f ⏐ %.1f", thisX, thisZ, thisY, otherX, otherZ);
    } else {
      // Direction
      facingNormal = String.format("%s " + "[%s]", getFacing(), getTowards());
      // Multiline coords + direction
      facingWithTCoords = String.format("%s " + "[%s] (%01.1f, %01.1f)", getFacing(), getTowards(), otherX, otherZ);
      // Only OW coords
      coordsNormal = String.format("%01.1f, %01.1f, %01.1f", thisX, thisY, thisZ);
      // Multiline Nether coords
      coordsMultiTranslated = String.format("(%01.1f, %01.1f)", otherX, otherZ);
      // Single line OW + Nether coords
      coordsTranslated = String.format("%01.1f, %01.1f, %01.1f (%01.1f, %01.1f)", thisX, thisY, thisZ, otherX, otherZ);
    }
    
    if (!translate.get() || MC.player.dimension == 1) {
      text.add(coordsNormal);
      if (direction.get()) {
        text.add(facingNormal);
      }
    } else if (MC.player.dimension == -1) {
      if (multiline.get()) {
        if (direction.get()) {
          text.add(facingWithTCoords);
        } else {
          text.add(coordsTranslated);
        }
        text.add(coordsNormal);
      } else {
        if (direction.get()) {
          text.add(facingNormal);
        }
        text.add(coordsTranslated);
      }
    } else {
      if (multiline.get()) {
        text.add(coordsNormal);
        if (direction.get()) {
          text.add(facingWithTCoords);
        } else {
          text.add(coordsTranslated);
        }
      } else {
        text.add(coordsTranslated);
        if (direction.get()) {
          text.add(facingNormal);
        }
      }
    }
    // Printing
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
      Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());
  }

  private String getFacing() {
    switch (MathHelper.floor((double) (Minecraft.getMinecraft().player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
      case 0:
        return "South";
      case 1:
        return "South West";
      case 2:
        return "West";
      case 3:
        return "North West";
      case 4:
        return "North";
      case 5:
        return "North East";
      case 6:
        return "East";
      case 7:
        return "South East";
    }
    return "Invalid";
  }

  private String getTowards() {
    switch (MathHelper.floor((double) (Minecraft.getMinecraft().player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
      case 0:
        return "+Z";
      case 1:
        return "-X +Z";
      case 2:
        return "-X";
      case 3:
        return "-X -Z";
      case 4:
        return "-Z";
      case 5:
        return "+X -Z";
      case 6:
        return "+X";
      case 7:
        return "+X +Z";
    }
    return "Invalid";
  }
}
