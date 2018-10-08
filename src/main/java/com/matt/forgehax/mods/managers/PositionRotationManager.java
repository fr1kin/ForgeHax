package com.matt.forgehax.mods.managers;

import com.google.common.collect.Lists;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.util.math.AngleN;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.task.SimpleManagerContainer;
import com.matt.forgehax.util.task.TaskChain;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.util.Pair;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/15/2017 by fr1kin */
@RegisterMod
public class PositionRotationManager extends ServiceMod {
  private static final SimpleManagerContainer<MovementUpdateListener> MANAGER =
      new SimpleManagerContainer<>();
  private static final SimpleWrapperImpl STATE = new SimpleWrapperImpl();

  public static SimpleManagerContainer<MovementUpdateListener> getManager() {
    return MANAGER;
  }

  public static ReadableRotationState getState() {
    return STATE;
  }

  public PositionRotationManager() {
    super("PositionRotationManager");
  }

  private final RotationState gState = new RotationState();
  private TaskChain<Consumer<ReadableRotationState>> futureTasks = TaskChain.empty();

  private boolean stopping = false;
  private TaskChain<MovementUpdateListener> stopTasks = TaskChain.empty();
  private MovementUpdateListener superiorListener = null;

  private static AngleN getPlayerAngles(EntityPlayer player) {
    return AngleN.degrees(player.rotationPitch, player.rotationYaw);
  }

  private static void setPlayerAngles(EntityPlayerSP player, AngleN angles) {
    player.rotationPitch = (float) angles.getPitch();
    player.rotationYaw = (float) angles.getYaw();
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

    RotationState gs = new RotationState();
    gs.setServerAngles(va);
    gs.setClientAngles(va);

    // boolean to check if any task has been processed
    boolean changed = false;

    // true if only the client angle has been updated
    boolean clientOnly = false;

    RotationState.Local ls = null;
    // process tasks until there are none left or one changes the players view angles
    for (MovementUpdateListener listener : getManager().functions()) {
      ls = new Local(gs);
      listener.onLocalPlayerMovementUpdate(ls);

      boolean clientCng = !gs.getClientAngles().equals(ls.getClientAngles());
      boolean serverCng = !gs.getServerAngles().equals(ls.getServerAngles());

      if (ls.isCanceled()) {
        // cancel event, do not update any view angles
        event.setCanceled(true);

        // set the current server angles to the previous states as this event wont fire
        RotationState rs = new RotationState(gs);
        rs.setServerAngles(gState.getServerAngles());
        synchronized (STATE) {
          STATE.setCurrentState(rs);
        }
        return;
      } else if (clientCng || serverCng) {
        if (clientOnly && clientCng) {
          // continue to next event and test if its client-side only
          continue;
        } else if (clientOnly) {
          // copy the data from this local state, the client shouldn't need the callbacks
          AngleN cva = gs.getClientAngles();
          gs.copyOf(ls);
          gs.setClientAngles(cva);
          gs.setListener(listener);
          break;
        } else if (clientCng && !serverCng) {
          // if the server angles are not changed, allow another task to set them
          changed = true; // flag this as changed
          clientOnly = true;
          gs.setClientAngles(ls.getClientAngles());
        } else {
          changed = true; // flag this as changed
          gs.copyOf(ls); // copy local state
          gs.setListener(listener);
          break;
        }
      } else if (ls.isHalted()) {
        // copy local state
        changed = true;
        gs.copyOf(ls);
        gs.setListener(listener);
        break;
      }
    }

    /* // may work on this later
    MovementUpdateListener currentListener = gState.getListener();
    MovementUpdateListener updateListener = gs.getListener();
    if(currentListener != null && currentListener != updateListener) {
      // listeners are changing, invoke the shutdown callback
      superiorListener = updateListener;
      stopTasks = Optional.ofNullable(gState.getListenerCallback())
          .map(cb -> cb.onStopped(superiorListener))
          .orElse(TaskChain.empty());

      if(stopTasks.hasNext()) {
        gs = new RotationState();
        gs.setServerAngles(va);
        gs.setClientAngles(va);

        ls = new Local(gs);
        ls.setServerAngles(va);
        ls.setClientAngles(va);

        stopTasks.next().onLocalPlayerMovementUpdate(ls);

        gs.copyOf(ls);
      }

      stopping = stopTasks.hasNext();
    }*/

    gState.copyOf(gs);

    // update the future tasks that are processed in the post movement update hook
    futureTasks = changed ? ls.getFutureTasks() : TaskChain.empty();

    // set the player angles to the server angles
    setPlayerAngles(event.getLocalPlayer(), gState.getServerAngles());
  }

  @SubscribeEvent
  public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {
    // reset angles if silent aiming is enabled
    if (gState.isSilent()) setPlayerAngles(event.getLocalPlayer(), gState.getClientAngles());

    // process all the tasks
    while (futureTasks.hasNext()) futureTasks.next().accept(gState);
    // set to empty task chain
    futureTasks = TaskChain.empty();

    // update the read-only state
    synchronized (STATE) {
      STATE.setCurrentState(gState);
    }
  }

  public interface MovementUpdateListener {
    /**
     * Called when this event has focus.
     *
     * @param state read/write data for manipulating the players rotation.
     */
    void onLocalPlayerMovementUpdate(RotationState.Local state);
  }

  public interface ListenerCallback {
    TaskChain<MovementUpdateListener> onStopped(MovementUpdateListener superior);
  }

  public interface ReadableRotationState {
    /**
     * Gets the local player instance
     *
     * @return local player instance. null if not in a world
     */
    default EntityPlayerSP getLocalPlayer() {
      return Helper.getLocalPlayer();
    }

    /**
     * The client-sided view angles of the player
     *
     * @return angle in degrees
     */
    AngleN getClientAngles();

    /**
     * The server-sided view angles of the player (what the server thinks the players view angles
     * are)
     *
     * @return angle in degrees
     */
    AngleN getServerAngles();

    /**
     * Will return the client-sided view angles or the immediate view angles if no task is currently
     * active.
     *
     * @return angle in degrees
     */
    default AngleN getRenderClientViewAngles() {
      return isActive()
          ? getClientAngles()
          : AngleN.degrees(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
    }

    /**
     * Will return the server-sided view angles or the immediate view angles if no task is currently
     * active.
     *
     * @return angle in degrees
     */
    default AngleN getRenderServerViewAngles() {
      return isActive()
          ? getServerAngles()
          : AngleN.degrees(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
    }

    /**
     * If the client and server view angles are purposefully desynchronized.
     *
     * @return true of the angles are desynced
     */
    boolean isSilent();

    /**
     * If a viewing task is currently being processed.
     *
     * @return true if a viewing task is being processed
     */
    default boolean isActive() {
      return getListener() != null;
    }

    /**
     * The current active listener.
     *
     * @return null if one does not exist
     */
    MovementUpdateListener getListener();
  }

  public static class RotationState implements ReadableRotationState {
    private AngleN serverViewAngles = AngleN.ZERO;
    private AngleN clientViewAngles = AngleN.ZERO;

    private Pair<MovementUpdateListener, ListenerCallback> activeListener = null;

    private RotationState() {}

    private RotationState(RotationState other) {
      copyOf(other);
    }

    private void copyOf(RotationState other) {
      this.serverViewAngles = other.serverViewAngles;
      this.clientViewAngles = other.clientViewAngles;
      this.activeListener = other.activeListener;
    }

    public AngleN getServerAngles() {
      return serverViewAngles;
    }

    public void setServerAngles(AngleN va) {
      Objects.requireNonNull(va);
      this.serverViewAngles = va;
    }

    public void setServerAngles(float pitch, float yaw) {
      setServerAngles(AngleN.degrees(pitch, yaw));
    }

    public AngleN getClientAngles() {
      return clientViewAngles;
    }

    public void setClientAngles(AngleN va) {
      Objects.requireNonNull(va);
      this.clientViewAngles = va;
    }

    public void setClientAngles(float pitch, float yaw) {
      setClientAngles(AngleN.degrees(pitch, yaw));
    }

    public void setViewAngles(AngleN va, boolean silent) {
      setServerAngles(va);
      if (!silent) setClientAngles(va);
    }

    public void setViewAngles(float pitch, float yaw, boolean silent) {
      setServerAngles(pitch, yaw);
      if (!silent) setClientAngles(pitch, yaw);
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

    public boolean isSilent() {
      return !Objects.equals(clientViewAngles, serverViewAngles);
    }

    public MovementUpdateListener getListener() {
      return activeListener == null ? null : activeListener.getKey();
    }

    private ListenerCallback getListenerCallback() {
      return activeListener == null ? null : activeListener.getValue();
    }

    private void setListener(MovementUpdateListener listener, ListenerCallback callback) {
      this.activeListener = new Pair<>(listener, callback);
    }

    private void setListener(MovementUpdateListener listener) {
      setListener(listener, getListenerCallback());
    }

    public void setCallback(ListenerCallback callback) {
      setListener(getListener(), callback);
    }

    private void unsetListener() {
      setListener(null, null);
    }

    protected TaskChain<Consumer<ReadableRotationState>> getFutureTasks() {
      return TaskChain.empty();
    }

    /** A class that contains variables localized to each listener instance */
    public static class Local extends RotationState {
      private final List<Consumer<ReadableRotationState>> later = Lists.newArrayList();

      private boolean halted = false;
      private boolean canceled = false;
      private boolean clientSided = false;

      private Local(RotationState other) {
        super(other);
      }

      @Override
      protected TaskChain<Consumer<ReadableRotationState>> getFutureTasks() {
        return TaskChain.<Consumer<ReadableRotationState>>builder().addAll(later).build();
      }

      public void invokeLater(Consumer<ReadableRotationState> task) {
        later.add(task);
      }

      public boolean isHalted() {
        return halted;
      }

      public void setHalted(boolean halted) {
        this.halted = halted;
      }

      public boolean isCanceled() {
        return canceled;
      }

      public void setCanceled(boolean canceled) {
        this.canceled = canceled;
      }

      public boolean isClientSided() {
        return clientSided;
      }

      public void setClientSided(boolean clientSided) {
        this.clientSided = clientSided;
      }
    }
  }

  private static class SimpleWrapperImpl implements ReadableRotationState {
    private ReadableRotationState state =
        new ReadableRotationState() {
          @Override
          public AngleN getClientAngles() {
            return AngleN.ZERO;
          }

          @Override
          public AngleN getServerAngles() {
            return AngleN.ZERO;
          }

          @Override
          public boolean isSilent() {
            return false;
          }

          @Override
          public MovementUpdateListener getListener() {
            return null;
          }
        };

    private void setCurrentState(ReadableRotationState state) {
      Objects.requireNonNull(state);
      this.state = state;
    }

    @Override
    public synchronized AngleN getClientAngles() {
      return state.getClientAngles();
    }

    @Override
    public synchronized AngleN getServerAngles() {
      return state.getServerAngles();
    }

    @Override
    public synchronized boolean isSilent() {
      return state.isSilent();
    }

    @Override
    public synchronized MovementUpdateListener getListener() {
      return state.getListener();
    }
  }
}
