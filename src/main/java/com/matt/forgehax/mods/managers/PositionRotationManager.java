package com.matt.forgehax.mods.managers;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.google.common.collect.Lists;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.util.math.AngleN;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.List;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/15/2017 by fr1kin */
@RegisterMod
public class PositionRotationManager extends ServiceMod {
  private static final SimpleManagerContainer<MovementUpdateListener> MANAGER =
      new SimpleManagerContainer<>();

  private static final RotationState STATE = new RotationState();

  public static SimpleManagerContainer<MovementUpdateListener> getManager() {
    return MANAGER;
  }

  public static RotationState getState() {
    return STATE;
  }

  public PositionRotationManager() {
    super("PositionRotationManager");
  }

  private static AngleN getPlayerAngles(EntityPlayer player) {
    return AngleN.degrees(player.rotationPitch, player.rotationYaw);
  }

  private static void setPlayerAngles(EntityPlayerSP player, AngleN angles) {
    if (angles != null) {
      player.rotationPitch = (float) angles.pitch();
      player.rotationYaw = (float) angles.yaw();
    }
  }

  private static void setPlayerPosition(EntityPlayerSP player, Vec3d position) {
    player.posX = position.x;
    player.posY = position.y;
    player.posZ = position.z;
  }

  @SubscribeEvent
  public void onMovementUpdatePre(LocalPlayerUpdateMovementEvent.Pre event) {
    // updated view angles
    AngleN va = getPlayerAngles(event.getLocalPlayer());

    STATE.setServerViewAngles(va);
    STATE.setClientViewAngles(va);

    // always set to false before processing the task(s)
    STATE.setSilent(false);
    STATE.setHalted(false);
    STATE.setSkipped(false);
    STATE.setActive(false);

    // process tasks until there are none left or one changes the players view angles
    for (MovementUpdateListener listener : getManager().functions()) {
      listener.onLocalPlayerMovementUpdate(STATE);
      if (STATE.isSkipped()) {
        event.setCanceled(true);
        return;
      } else if (STATE.isHalted() || !va.equals(STATE.getServerViewAngles())) {
        STATE.setActive(true);
        break; // break is forced or angles have changed
      }
    }

    setPlayerAngles(event.getLocalPlayer(), STATE.getServerViewAngles());
  }

  @SubscribeEvent
  public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {
    // reset angles if silent aiming is enabled
    RotationState s = STATE;
    if (STATE.isSilent()) setPlayerAngles(event.getLocalPlayer(), STATE.getClientViewAngles());

    // process tasks
    STATE.tasks.forEach(Runnable::run);
    STATE.tasks.clear();
  }

  public interface MovementUpdateListener {
    void onLocalPlayerMovementUpdate(RotationState state);
  }

  public static class RotationState {
    private final List<Runnable> tasks = Lists.newArrayList();

    private AngleN serverViewAngles = null;
    private AngleN clientViewAngles = null;

    private boolean silent = false;
    private boolean active = false;
    private boolean halted = false;
    private boolean skipped = false;

    public void processAfter(Runnable task) {
      tasks.add(task);
    }

    public EntityPlayerSP getLocalPlayer() {
      return Helper.getLocalPlayer();
    }

    public AngleN getServerViewAngles() {
      return serverViewAngles;
    }

    public AngleN getActiveServerViewAngles() {
      return isActive() ? getServerViewAngles() : getPlayerAngles(getLocalPlayer());
    }

    public void setServerViewAngles(AngleN va) {
      this.serverViewAngles = va;
    }

    public void setServerViewAngles(float pitch, float yaw) {
      setServerViewAngles(AngleN.degrees(pitch, yaw));
    }

    public AngleN getClientViewAngles() {
      return clientViewAngles;
    }

    public void setClientViewAngles(AngleN va) {
      this.clientViewAngles = va;
    }

    public void setClientViewAngles(float pitch, float yaw) {
      setClientViewAngles(AngleN.degrees(pitch, yaw));
    }

    public void setViewAngles(AngleN va, boolean silent) {
      setSilent(silent);
      setServerViewAngles(va);
      if (!silent) setClientViewAngles(va);
    }

    public void setViewAngles(float pitch, float yaw, boolean silent) {
      setSilent(silent);
      setServerViewAngles(pitch, yaw);
      if (!silent) setClientViewAngles(pitch, yaw);
    }

    public void setViewAngles(AngleN va) {
      setViewAngles(va, false);
    }

    public void setViewAngles(float pitch, float yaw) {
      setViewAngles(pitch, yaw, false);
    }

    public void setViewAnglesSilent(AngleN va) {
      setViewAngles(va, true);
    }

    public void setViewAnglesSilent(float pitch, float yaw) {
      setViewAngles(pitch, yaw, true);
    }

    private boolean isActive() {
      return active;
    }

    private void setActive(boolean active) {
      this.active = active;
    }

    public boolean isSilent() {
      return silent;
    }

    public void setSilent(boolean silent) {
      this.silent = silent;
    }

    public boolean isHalted() {
      return halted;
    }

    public void setHalted(boolean halted) {
      this.halted = halted;
    }

    public boolean isSkipped() {
      return skipped;
    }

    public void setSkipped(boolean skipped) {
      this.skipped = skipped;
    }

    private void reset() {
      this.tasks.clear();
      this.serverViewAngles = null;
      this.clientViewAngles = null;
      this.halted = this.skipped = false;
    }

    private boolean needsUpdate() {
      return serverViewAngles == null || clientViewAngles == null;
    }
  }
}
