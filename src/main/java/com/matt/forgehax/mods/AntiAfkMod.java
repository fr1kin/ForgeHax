package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.*;

@RegisterMod
public class AntiAfkMod extends ToggleMod {
    private final Setting<Long> delay = getCommandStub().builders().<Long>newSettingBuilder()
            .name("delay")
            .description("Delay time (in MS) between tasks")
            .defaultTo(10_000L)
            .min(0L)
            .build();
    private final Setting<Long> runtime = getCommandStub().builders().<Long>newSettingBuilder()
            .name("runtime")
            .description("Time to run each task")
            .defaultTo(5_000L)
            .min(0L)
            .build();

    private final Setting<Boolean> swing = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("swing")
            .description("Swing the players arm")
            .defaultTo(true)
            .build();
    private final Setting<Boolean> walk = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("walk")
            .description("Walk in different directions")
            .defaultTo(false)
            .build();
    private final Setting<Boolean> spin = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("spin")
            .description("Spin the players view")
            .defaultTo(false)
            .build();
    private final Setting<Boolean> mine = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("mine")
            .description("Place and break a block that is in the players inventory. Only runs if the player has a block that can break in 1 hit and be placed under the player.")
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
        return TaskEnum.ALL.stream()
                .filter(IAFKTask::isRunnable)
                .filter(TaskEnum::isEnabled)
                .collect(Collectors.toList());
    }

    private void reset() {
        timer.reset();
        ranStop.set(false);
        setTask(TaskEnum.NONE);
    }

    @Override
    public String getDebugDisplayText() {
        return super.getDebugDisplayText() + " " + String.format("[%s | %s | next = %s]",
                getTask().name(),
                isTaskRunning() ? "Running" : "Waiting",
                isTaskRunning() ? (SimpleTimer.toFormattedTime(Math.max(runtime.get() - timer.getTimeElapsed(), 0))) : (SimpleTimer.toFormattedTime(Math.max(delay.get() - timer.getTimeElapsed(), 0))));
    }

    @SubscribeEvent
    public void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if(isTaskRunning()) {
            // Reset the timer if the player is not afk
            reset();
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        reset();
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(!timer.isStarted())
            timer.start(); // start timer if it hasn't already

        if(!isTaskRunning()) {
            if (timer.hasTimeElapsed(delay.get())) {
                List<TaskEnum> next = getNextTask();
                if(!next.isEmpty()) { // wait again to check if the task is valid
                    setTask(next.get(ThreadLocalRandom.current().nextInt(next.size()))); // select a random task
                    getTask().onStart();
                }
                timer.start();
            }
        } else {
            if(timer.hasTimeElapsed(runtime.get())) {
                boolean prev = ranStop.get();
                if(ranStop.compareAndSet(false, true)) // only run once
                    getTask().onStop();

                if(getTask().isRunning()) {
                    if(prev == ranStop.get())
                        getTask().onTick(); // only run if this task did not execute onStop() on the same tick
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
            public void onTick() {}

            @Override
            public void onStart() {}

            @Override
            public void onStop() {}

            @Override
            public boolean isRunnable() {
                return false;
            }
        },
        SWING {
            @Override
            public void onTick() {}

            @Override
            public void onStart() {}

            @Override
            public void onStop() {
                getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        },
        WALK {
            double angle = 0;

            @Override
            public void onTick() {
                Bindings.forward.setPressed(true);
                LocalPlayerUtils.setViewAngles(getLocalPlayer().rotationPitch, angle);
            }

            @Override
            public void onStart() {
                ForgeHaxHooks.isSafeWalkActivated = true;
                Bindings.forward.bind();
                angle = (Math.round((LocalPlayerUtils.getViewAngles().getYaw() + 1.f) / 90.f) * 90.f) + 90.f;
            }

            @Override
            public void onStop() {
                getModManager().get(SafeWalkMod.class).ifPresent(mod -> ForgeHaxHooks.isSafeWalkActivated = mod.isEnabled());
                Bindings.forward.setPressed(false);
                Bindings.forward.unbind();
            }
        },
        SPIN {
            float ang = 0.f;
            double p, y;

            @Override
            public void onTick() {
                LocalPlayerUtils.setViewAngles(MathHelper.clamp(getLocalPlayer().rotationPitch + MathHelper.cos(ang += 0.1f), -90.f, 90.f), getLocalPlayer().rotationYaw + 1.8f);
            }

            @Override
            public void onStart() {
                ang = 0.f;
                p = getLocalPlayer().rotationPitch;
                y = getLocalPlayer().rotationYaw;
            }

            @Override
            public void onStop() {
                LocalPlayerUtils.setViewAngles(p, y);
            }
        },
        MINE { // TODO: get working
            static final int WAIT_TICKS = 20; // 1 second

            int counter = 0;

            boolean halting = false;
            boolean placed = false;

            @Override
            public void onTick() {
                if(halting && !placed)
                    return; // stop anymore processing

                if(counter % WAIT_TICKS == 0) {
                    LocalPlayerInventory.InvItem item = LocalPlayerInventory.getHotbarInventory().stream()
                            .filter(itm -> itm.getItemStack().getItem() instanceof ItemRedstone)
                            .findAny().orElse(LocalPlayerInventory.InvItem.EMPTY);

                    BlockPos pos = getLocalPlayer().getPosition();
                    IBlockState state = getWorld().getBlockState(pos);

                    Vec3d eyePos = getLocalPlayer().getPositionEyes(1.f);
                    Vec3d targPos = eyePos.addVector(0, -MC.playerController.getBlockReachDistance(), 0);

                    RayTraceResult result = getWorld().rayTraceBlocks(eyePos, targPos, false, false, true);

                    if(result == null)
                        return;

                    if(item.nonNull()) {
                        LocalPlayerInventory.setSelected(item);

                        if(!placed) {
                            int count = item.getItemStack().getCount();
                            if(MC.playerController.processRightClickBlock(getLocalPlayer(), getWorld(), result.getBlockPos(), result.sideHit, result.hitVec, EnumHand.MAIN_HAND).equals(EnumActionResult.SUCCESS)) {
                                getLocalPlayer().swingArm(EnumHand.MAIN_HAND);

                                if (!item.getItemStack().isEmpty() && item.getItemStack().getCount() != count)
                                    MC.entityRenderer.itemRenderer.resetEquippedProgress(EnumHand.MAIN_HAND);

                                placed = true;
                            }
                        } else {
                            if(result.typeOfHit.equals(RayTraceResult.Type.BLOCK) && !getWorld().isAirBlock(result.getBlockPos()) && MC.playerController.onPlayerDamageBlock(result.getBlockPos(), result.sideHit)) {
                                getLocalPlayer().swingArm(EnumHand.MAIN_HAND);
                                placed = false;
                            }
                        }
                    }
                }
                ++counter;
            }

            @Override
            public void onStart() {
                counter = 0;
                halting = placed = false;
            }

            @Override
            public void onStop() {
                halting = true;
            }

            @Override
            public boolean isRunnable() {
                /*
                return LocalPlayerInventory.getHotbarInventory().stream().anyMatch(item -> item.getItemStack().getItem() instanceof ItemRedstone)
                        && getWorld().isAirBlock(getLocalPlayer().getPosition().add(0, 1, 0));//*/
                return false; // disabled until functional
            }

            @Override
            public boolean isRunning() {
                return placed;
            }
        },
        ;

        Setting<Boolean> parentSetting;

        TaskEnum() {}

        public void setParentSetting(Setting<Boolean> parentSetting) {
            this.parentSetting = parentSetting;
        }

        public boolean isEnabled() {
            Objects.requireNonNull(parentSetting, "Setting must be set for all tasks in enum");
            return parentSetting.get();
        }

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
