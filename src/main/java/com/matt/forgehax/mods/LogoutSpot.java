package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.google.common.collect.Sets;
import com.matt.forgehax.Helper;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterMod
public class LogoutSpot extends ToggleMod {
  
  private final Setting<Boolean> render =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("render")
      .description("Draw a box where the player logged out")
      .defaultTo(true)
      .build();
  private final Setting<Integer> max_distance =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("max-distance")
      .description("Distance from box before deleting it")
      .defaultTo(320)
      .build();
  private final Setting<Boolean> print_message =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("print-message")
      .description("Print connect/disconnect messages in chat")
      .defaultTo(true)
      .build();
  
  private final Set<LogoutPos> spots = Sets.newHashSet();
  
  public LogoutSpot() {
    super(Category.RENDER, "LogoutSpot", false, "show where a player logs out");
  }
  
  private void reset() {
    synchronized (spots) {
      spots.clear();
    }
  }
  
  private void printWarning(String fmt, Object... args) {
    if (print_message.get()) {
      Helper.printWarning(fmt, args);
    }
  }
  
  @Override
  public void onLoad() {
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("clear")
      .description("Clear cloned players")
      .processor(data -> reset())
      .build();
  }
  
  @Override
  protected void onDisabled() {
    reset();
  }
  
  @SubscribeEvent
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    synchronized (spots) {
      if (spots.removeIf(spot -> spot.getId().equals(event.getPlayerInfo().getId()))) {
        printWarning("%s has joined!", event.getPlayerInfo().getName());
      }
    }
  }
  
  @SubscribeEvent
  public void onPlayerDisconnect(PlayerConnectEvent.Leave event) {
    if (getWorld() == null) {
      return;
    }
    
    EntityPlayer player = getWorld().getPlayerEntityByUUID(event.getPlayerInfo().getId());
    if (player != null && getLocalPlayer() != null && !getLocalPlayer().equals(player)) {
      AxisAlignedBB bb = player.getEntityBoundingBox();
      synchronized (spots) {
        if (spots.add(
          new LogoutPos(
            event.getPlayerInfo().getId(),
            event.getPlayerInfo().getName(),
            new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
            new Vec3d(bb.minX, bb.minY, bb.minZ)))) {
          printWarning("%s has disconnected!", event.getPlayerInfo().getName());
        }
      }
    }
  }
  
  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlayEvent(Render2DEvent event) {
    if (!render.get()) {
      return;
    }
    
    synchronized (spots) {
      spots.forEach(
        spot -> {
          Vec3d top = spot.getTopVec();
          Plane upper = VectorUtils.toScreen(top);
          if (upper.isVisible()) {
            double distance = getLocalPlayer().getPositionVector().distanceTo(top);
            String name = String.format("%s (%.1f)", spot.getName(), distance);
            SurfaceHelper.drawTextShadow(
              name,
              (int) upper.getX() - (SurfaceHelper.getTextWidth(name) / 2),
              (int) upper.getY() - (SurfaceHelper.getTextHeight() + 1),
              Colors.RED.toBuffer());
          }
        });
    }
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (!render.get()) {
      return;
    }
    
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    synchronized (spots) {
      spots.forEach(
        spot ->
          GeometryTessellator.drawLines(
            event.getBuffer(),
            spot.getMins().x,
            spot.getMins().y,
            spot.getMins().z,
            spot.getMaxs().x,
            spot.getMaxs().y,
            spot.getMaxs().z,
            GeometryMasks.Line.ALL,
            Colors.RED.toBuffer()));
    }
    
    event.getTessellator().draw();
  }
  
  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (max_distance.get() > 0) {
      synchronized (spots) {
        spots.removeIf(
          pos ->
            getLocalPlayer().getPositionVector().distanceTo(pos.getTopVec())
              > max_distance.getAsDouble());
      }
    }
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    reset();
  }
  
  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    reset();
  }
  
  private class LogoutPos {
    
    final UUID id;
    final String name;
    final Vec3d maxs;
    final Vec3d mins;
    
    private LogoutPos(UUID uuid, String name, Vec3d maxs, Vec3d mins) {
      this.id = uuid;
      this.name = name;
      this.maxs = maxs;
      this.mins = mins;
    }
    
    public UUID getId() {
      return id;
    }
    
    public String getName() {
      return name;
    }
    
    public Vec3d getMaxs() {
      return maxs;
    }
    
    public Vec3d getMins() {
      return mins;
    }
    
    public Vec3d getTopVec() {
      return new Vec3d(
        (getMins().x + getMaxs().x) / 2.D, getMaxs().y, (getMins().z + getMaxs().z) / 2.D);
    }
    
    @Override
    public boolean equals(Object other) {
      return this == other
        || (other instanceof LogoutPos && getId().equals(((LogoutPos) other).getId()));
    }
    
    @Override
    public int hashCode() {
      return getId().hashCode();
    }
  }
}
