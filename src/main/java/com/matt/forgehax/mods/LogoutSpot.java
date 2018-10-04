package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.google.common.base.Strings;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterMod
public class LogoutSpot extends ToggleMod {
  public LogoutSpot() {
    super(Category.RENDER, "LogoutSpot", false, "show where a player logs out");
  }

  public final Setting<Boolean> renderPosition =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("RenderPosition")
          .description("Draw a box where the player logged out")
          .defaultTo(true)
          .build();
  public final Setting<Integer> maxDistance =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("MaxDistance")
          .description("Distance from box before deleting it")
          .defaultTo(50)
          .build();
  public final Setting<Boolean> outputToChat =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("OutputToChat")
          .description("Print coords to chat")
          .defaultTo(true)
          .build();

  private final Set<LogoutPos> logoutSpots = new HashSet<>();

  // join/leave event does not work
  @SubscribeEvent
  public void onPacketRecieved(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketPlayerListItem) {
      SPacketPlayerListItem playerListPacket = (SPacketPlayerListItem) event.getPacket();
      if (playerListPacket.getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER)
          || playerListPacket.getAction().equals(SPacketPlayerListItem.Action.ADD_PLAYER)) {
        try {
          playerListPacket
              .getEntries()
              .stream()
              .filter(Objects::nonNull)
              .filter(
                  data -> {
                    String name = getNameFromComponent(data.getProfile());
                    return !Strings.isNullOrEmpty(name) && !isLocalPlayer(name)
                        || playerListPacket
                            .getAction()
                            .equals(SPacketPlayerListItem.Action.REMOVE_PLAYER);
                  })
              .forEach(
                  data -> {
                    final String name = getNameFromComponent(data.getProfile());
                    final UUID id = data.getProfile().getId();
                    switch (playerListPacket.getAction()) {
                      case ADD_PLAYER:
                        logoutSpots.removeIf(
                            pos -> {
                              if (pos.id.equals(id)) {
                                if (outputToChat.getAsBoolean()) {
                                  Helper.printMessage(name + " has joined!");
                                }
                                return true;
                              }
                              return false;
                            });
                        break;

                      case REMOVE_PLAYER: // if they leave and they are in the world save it
                        EntityPlayer player = MC.world.getPlayerEntityByUUID(id);
                        if (player != null) {
                          AxisAlignedBB BB = player.getEntityBoundingBox();
                          Vec3d[] pos = {
                            new Vec3d(BB.minX, BB.minY, BB.minZ),
                            new Vec3d(BB.maxX, BB.maxY, BB.maxZ)
                          };
                          logoutSpots.add(new LogoutPos(pos, id, player.getName()));

                          if (outputToChat.getAsBoolean()) {
                            Helper.printMessage(name + " has disconnected!");
                          }
                        }
                        break;
                    }
                  });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /*@SubscribeEvent
  public void onPlayerJoin(PlayerConnectEvent.Join event) {
      logoutSpots.removeIf(pos -> {
          if (pos.id.equals(event.getPlayerInfo().getId())) {
              if (outputToChat.getAsBoolean()) {
                  Helper.printMessage(event.getPlayerInfo().getName() + " has joined!");
              }
              return true;
          }
          return false;
      });
  }

  @SubscribeEvent
  public void onPlayerLeave(PlayerConnectEvent.Leave event) {
      UUID id = event.getPlayerInfo().getId();
      EntityPlayer player = MC.world.getPlayerEntityByUUID(id);
      if (player != null) {
          AxisAlignedBB BB = player.getEntityBoundingBox();
          Vec3d[] pos = {new Vec3d(BB.minX, BB.minY, BB.minZ), new Vec3d(BB.maxX, BB.maxY, BB.maxZ)};
          logoutSpots.add(new LogoutPos(pos, id, player.getName()));

          if (outputToChat.getAsBoolean()) {
              Helper.printMessage(event.getPlayerInfo().getName() + " has disconnected!");
          }
      }

  }*/

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
    if (renderPosition.getAsBoolean()
        && event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {

      logoutSpots.forEach(
          pos -> {
            Vec3d topVec =
                new Vec3d(
                    (pos.pos[0].x + pos.pos[1].x) / 2,
                    pos.pos[1].y,
                    (pos.pos[0].z + pos.pos[1].z)
                        / 2); // position where to place the text in the world
            VectorUtils.ScreenPos textPos =
                VectorUtils._toScreen(
                    topVec.x, topVec.y, topVec.z); // where to place the text on the screen
            double distance =
                MC.player.getDistance(
                    (pos.pos[0].x + pos.pos[1].x) / 2,
                    pos.pos[0].y,
                    (pos.pos[0].z + pos.pos[1].z) / 2); // distance from player to logout spot
            if (textPos.isVisible) {
              String name = pos.name + String.format(" (%.1f)", distance);
              SurfaceHelper.drawTextShadow(
                  name,
                  textPos.x - (SurfaceHelper.getTextWidth(name) / 2),
                  textPos.y - (SurfaceHelper.getTextHeight() + 1),
                  Utils.toRGBA(255, 0, 0, 0));
            }
          });
    }
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) { // render box
    if (renderPosition.getAsBoolean()) {
      event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

      logoutSpots.forEach(
          position -> {
            GeometryTessellator.drawQuads(
                event.getBuffer(), // horizontal lines
                position.pos[0].x,
                position.pos[0].y,
                position.pos[0].z,
                position.pos[1].x,
                position.pos[1].y,
                position.pos[1].z,
                GeometryMasks.Quad.ALL,
                Utils.Colors.RED);

            GeometryTessellator.drawLines(
                event.getBuffer(), // vertical lines
                position.pos[0].x,
                position.pos[0].y,
                position.pos[0].z,
                position.pos[1].x,
                position.pos[1].y,
                position.pos[1].z,
                GeometryMasks.Quad.ALL,
                Utils.Colors.RED);
          });

      event.getTessellator().draw();
    }
  }

  @SubscribeEvent
  public void onPlayerUpdate(
      LocalPlayerUpdateEvent event) { // delete cloned player if they're too far
    logoutSpots.removeIf(
        pos -> {
          double distance =
              MC.player.getDistance(
                  (pos.pos[0].x + pos.pos[1].x) / 2,
                  pos.pos[0].y,
                  (pos.pos[0].z + pos.pos[1].z) / 2); // distance from player to entity
          return distance >= maxDistance.getAsDouble() && distance > 0;
        });
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    logoutSpots.clear();
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    logoutSpots.clear();
  }

  private class LogoutPos { // keeps track of positions and uuid
    final Vec3d[] pos;
    final UUID id;
    final String name;

    private LogoutPos(Vec3d[] position, UUID uuid, String name) {
      this.pos = position;
      this.id = uuid;
      this.name = name;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof LogoutPos)) return false;

      return (other == this) || this.id.equals(((LogoutPos) other).id);
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }
  }

  private float getLateralDistanceFromPlayer(Entity entityIn) {
    float f = (float) (entityIn.posX - MC.player.posX);
    float f2 = (float) (entityIn.posZ - MC.player.posZ);
    return MathHelper.sqrt(f * f + f2 * f2);
  }

  private String getNameFromComponent(GameProfile profile) {
    return Objects.nonNull(profile) ? profile.getName() : "";
  }

  private boolean isLocalPlayer(String username) {
    return Objects.nonNull(MC.player)
        && MC.player.getDisplayName().getUnformattedText().equals(username);
  }

  @Override
  public void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("clear")
        .description("Clear cloned players")
        .processor(
            data -> {
              logoutSpots.clear();
            })
        .build();
  }
}
