package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.key.Bindings;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.mods.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.util.SimpleTimer;
import dev.fiki.forgehax.main.util.Utils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiAfkMod extends ToggleMod {
  
  private final Setting<Long> delay =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("delay")
          .description("Delay time (in MS) between tasks")
          .defaultTo(10_000L)
          .min(0L)
          .build();
  private final Setting<Long> runtime =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("runtime")
          .description("Time to run each task")
          .defaultTo(5_000L)
          .min(0L)
          .build();
  private final Setting<Boolean> silent =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("silent")
          .description("Make most afk tasks execute without disrupting the players view")
          .defaultTo(false)
          .changed(cb -> TaskEnum.setSilent(cb.getTo()))
          .build();
  
  private final Setting<Boolean> swing =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("swing")
          .description("Swing the players arm")
          .defaultTo(true)
          .build();
  private final Setting<Boolean> walk =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("walk")
          .description("Walk in different directions")
          .defaultTo(false)
          .build();
  private final Setting<Boolean> spin =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("spin")
          .description("Spin the players view")
          .defaultTo(false)
          .build();
  private final Setting<Boolean> mine =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("mine")
          .description(
              "Place and break a block that is in the players inventory. Only runs if the player has a block that can break in 1 hit and be placed under the player.")
          .defaultTo(false)
          .build();
  
  private final SimpleTimer timer = new SimpleTimer();
  private final AtomicBoolean ranStop = new AtomicBoolean(false);
  
  private TaskEnum task = TaskEnum.NONE;
  
  public AntiAfkMod() {
    super(Category.PLAYER, "AntiAFK", false, "Swing arm to prevent being afk kicked");
    
    TaskEnum.SWING.setParentSetting(swing);
    TaskEnum.WALK.setParentSetting(walk);
    TaskEnum.SPIN.setParentSetting(spin);
    TaskEnum.MINE.setParentSetting(mine);
  }
  
  private TaskEnum getTask() {
    return task;
  }
  
  private void setTask(TaskEnum task) {
    this.task = task;
  }
  
  private boolean isTaskRunning() {
    return !getTask().equals(TaskEnum.NONE);
  }
  
  private List<TaskEnum> getNextTask() {
    return TaskEnum.ALL
        .stream()
        .filter(IAFKTask::isRunnable)
        .filter(TaskEnum::isEnabled)
        .collect(Collectors.toList());
  }
  
  private void reset() {
    timer.reset();
    ranStop.set(false);
    getTask().onStop();
    setTask(TaskEnum.NONE);
  }
  
  @Override
  protected void onLoad() {
    TaskEnum.setSilent(silent.get());
  }
  
  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText()
        + " "
        + String.format(
        "[%s | %s | next = %s]",
        getTask().name(),
        isTaskRunning() ? "Running" : "Waiting",
        isTaskRunning()
            ? (SimpleTimer.toFormattedTime(Math.max(runtime.get() - timer.getTimeElapsed(), 0)))
            : (SimpleTimer.toFormattedTime(Math.max(delay.get() - timer.getTimeElapsed(), 0))));
  }
  
  @SubscribeEvent
  public void onKeyboardInput(InputEvent.KeyInputEvent event) {
    reset();
  }
  
  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    reset();
  }
  
  @SubscribeEvent
  public void onDisconnect(DisconnectFromServerEvent event) {
    reset();
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (!timer.isStarted()) {
      timer.start(); // start timer if it hasn't already
    }
    
    if (!isTaskRunning()) {
      if (timer.hasTimeElapsed(delay.get())) {
        List<TaskEnum> next = getNextTask();
        if (!next.isEmpty()) { // wait again to check if the task is valid
          setTask(
              next.get(ThreadLocalRandom.current().nextInt(next.size()))); // select a random task
          getTask().onStart();
        }
        timer.start();
      }
    } else {
      if (timer.hasTimeElapsed(runtime.get())) {
        boolean prev = ranStop.get();
        if (ranStop.compareAndSet(false, true)) // only run once
        {
          getTask().onStop();
        }
        
        if (getTask().isRunning()) {
          if (prev == ranStop.get()) {
            getTask().onTick(); // only run if this task did not execute onStop() on the same tick
          }
        } else {
          setTask(TaskEnum.NONE);
          ranStop.set(false);
          timer.start();
        }
      } else {
        getTask().onTick(); // run task tick
      }
    }
  }
  
  enum TaskEnum implements IAFKTask {
    NONE {
      @Override
      public void onTick() {
      }
      
      @Override
      public void onStart() {
      }
      
      @Override
      public void onStop() {
      }
      
      @Override
      public boolean isRunnable() {
        return false;
      }
    },
    SWING {
      @Override
      public void onTick() {
      }
      
      @Override
      public void onStart() {
      }
      
      @Override
      public void onStop() {
        swingHand();
      }
    },
    WALK {
      static final int DEGREES = 45;
      
      double angle = 0;
      
      @Override
      public void onTick() {
        Bindings.forward.setPressed(true);
        // TODO: reimplement view angle setting
      }
      
      @Override
      public void onStart() {
        ForgeHaxHooks.isSafeWalkActivated = true;
        Bindings.forward.bind();
        
        Vec3d eye = EntityUtils.getEyePos(Globals.getLocalPlayer());
        
        List<Double> yaws = Lists.newArrayList();
        for (int i = 0; i < (360 / DEGREES); ++i) {
          yaws.add((i * DEGREES) - 180.D);
        }
        Collections.shuffle(yaws);
        
        double lastDistance = -1.D;
        for (double y : yaws) {
          double[] cc = Angle.degrees(0.f, (float) y).getForwardVector();
          Vec3d target = eye.add(new Vec3d(cc[0], cc[1], cc[2]).normalize().scale(64));

          RayTraceContext ctx = new RayTraceContext(eye, target,
              RayTraceContext.BlockMode.COLLIDER,
              RayTraceContext.FluidMode.ANY,
              Globals.getLocalPlayer());
          RayTraceResult result = Globals.getWorld().rayTraceBlocks(ctx);
          double distance = RayTraceResult.Type.MISS.equals(result.getType()) ? 64.D :
              eye.distanceTo(result.getHitVec());
          if ((distance >= 1.D || lastDistance == -1.D)
              && (distance > lastDistance || Math.random() < 0.20D)) {
            angle = y;
            lastDistance = distance;
          }
        }
      }
      
      @Override
      public void onStop() {
        Bindings.forward.setPressed(false);
        Bindings.forward.unbind();
        Globals.getLocalPlayer().setMotion(Vec3d.ZERO);
        Globals.getModManager().get(SafeWalkMod.class).ifPresent(mod -> ForgeHaxHooks.isSafeWalkActivated = mod.isEnabled());
      }
    },
    SPIN {
      float ang = 0.f;
      double p,y;
      
      @Override
      public void onTick() {
        setViewAngles(MathHelper.clamp(
            Globals.getLocalPlayer().rotationPitch + MathHelper.cos(ang += 0.1f), -90.f, 90.f),
            Globals.getLocalPlayer().rotationYaw + 1.8f);
      }
      
      @Override
      public void onStart() {
        ang = 0.f;
        p = Globals.getLocalPlayer().rotationPitch;
        y = Globals.getLocalPlayer().rotationYaw;
      }
      
      @Override
      public void onStop() {
        setViewAngles(p, y);
      }
    },
    MINE {
      static final int MULTIPLIER = 2;
      
      final SimpleTimer halting = new SimpleTimer();
      
      int counter = 0;
      double p;
      
      RayTraceResult getTraceBelow() { // TODO: fix the trace so i dont have to do witchcraft in
        // getBlockBelow()
        Vec3d eyes = EntityUtils.getEyePos(Globals.getLocalPlayer());
        RayTraceContext ctx = new RayTraceContext(eyes,
            eyes.add(0, -Globals.getPlayerController().getBlockReachDistance(), 0),
            RayTraceContext.BlockMode.COLLIDER,
            RayTraceContext.FluidMode.NONE,
            Globals.getLocalPlayer());
        return Globals.getWorld().rayTraceBlocks(ctx);
      }
      
      BlockPos getBlockBelow() {
        RayTraceResult tr = getTraceBelow();
        BlockPos hit = new BlockPos(tr.getHitVec());
        return RayTraceResult.Type.MISS.equals(tr.getType()) ? BlockPos.ZERO :
            (Globals.getWorld().getBlockState(hit.add(0, 1, 0))
                .getBlock()
                .equals(Blocks.REDSTONE_WIRE) ? hit.add(0, 1, 0) : hit);
      }
      
      boolean isPlaced() {
        return Globals.getWorld().getBlockState(getBlockBelow()).getBlock().equals(Blocks.REDSTONE_WIRE);
      }
      
      @Override
      public void onTick() {
        if (counter++ % (TPS * MULTIPLIER) == 0) {
          if (isPlaced()) {
            Globals.sendNetworkPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK,
                getBlockBelow(), Direction.UP));
            swingHand();
            return;
          }
          LocalPlayerInventory.InvItem item =
              LocalPlayerInventory.getHotbarInventory()
                  .stream()
                  .filter(itm -> ItemGroup.REDSTONE.equals(itm.getItemStack().getItem().getGroup()))
                  .findAny()
                  .orElse(LocalPlayerInventory.InvItem.EMPTY);
          
          if (item.isNull()) {
            return;
          }
          
          RayTraceResult result = getTraceBelow();
          
          if (RayTraceResult.Type.MISS.equals(result.getType())) {
            return;
          }

          BlockPos hit = new BlockPos(result.getHitVec());

          if (!Blocks.REDSTONE_WIRE.isValidPosition(Globals.getWorld().getBlockState(hit), Globals.getWorld(), hit)) {
            return; // can't place block
          }
          
          ResetFunction func = LocalPlayerInventory.setSelected(item);
          LocalPlayerInventory.syncSelected();

          BlockRayTraceResult tr = new BlockRayTraceResult(result.getHitVec(), Direction.UP, hit, false);
          
          Globals.sendNetworkPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, tr));
          swingHand();
          
          func.revert();
        }
      }
      
      @Override
      public void onStart() {
        halting.reset();
        counter = TPS * MULTIPLIER - 1; // start by placing the block
        p = Globals.getLocalPlayer().rotationPitch;
        
        BlockPos pos = getBlockBelow();
        Vec3d look = new Vec3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        Angle va = Utils.getLookAtAngles(look);
        setViewAngles(va.getPitch(), va.getYaw());
      }
      
      @Override
      public void onStop() {
        halting.start();
      }
      
      @Override
      public boolean isRunnable() {
        BlockPos below = getBlockBelow();
        return LocalPlayerInventory.getHotbarInventory().stream()
            .anyMatch(item -> ItemGroup.REDSTONE.equals(item.getItemStack().getItem().getGroup()))
            && (Blocks.REDSTONE_WIRE.isValidPosition(Globals.getWorld().getBlockState(below), Globals.getWorld(), below) || isPlaced());
        // return false; // disabled until functional
      }
      
      @Override
      public boolean isRunning() {
        return (!halting.isStarted() || !halting.hasTimeElapsed(5_000)) && isPlaced();
      }
    },
    ;
    
    Setting<Boolean> parentSetting;
    
    TaskEnum() {
    }
    
    public void setParentSetting(Setting<Boolean> parentSetting) {
      this.parentSetting = parentSetting;
    }
    
    public boolean isEnabled() {
      Objects.requireNonNull(parentSetting, "Setting must be set for all tasks in enum");
      return parentSetting.get();
    }
    
    //
    //
    //
    
    static final int TPS = 20;
    static boolean silent = false;
    
    static void swingHand() {
      if (silent) {
        Globals.getNetworkManager().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
      } else {
        Globals.getLocalPlayer().swingArm(Hand.MAIN_HAND);
      }
    }
    
    static void setViewAngles(double p, double y) {
      /*
      if(silent)
          getNetworkManager().sendPacket(new CPacketPlayer.Rotation((float)p, (float)y, getLocalPlayer().onGround));
      else
          LocalPlayerUtils.setViewAngles(p, y);*/
      
      // TODO: view angle stuff
    }
    
    public static void setSilent(boolean silent) {
      TaskEnum.silent = silent;
    }
    
    //
    //
    
    public static final EnumSet<TaskEnum> ALL = EnumSet.allOf(TaskEnum.class);
  }
  
  interface IAFKTask {
    
    void onTick();
    
    void onStart();
    
    void onStop();
    
    default boolean isRunnable() {
      return true;
    }
    
    default boolean isRunning() {
      return false;
    }
  }
}
