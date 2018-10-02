package com.matt.forgehax.mods.services;

import com.google.common.collect.Queues;
import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.math.AngleN;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.swing.text.html.parser.Entity;
import java.util.Objects;
import java.util.Queue;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 6/15/2017 by fr1kin
 */
@RegisterMod
public class PositionRotationManager extends ServiceMod {
    private static final Queue<MovementTask> QUEUE = Queues.newPriorityQueue();

    public static boolean registerCallback(OnMovementUpdate task, PriorityEnum priority) {
        return QUEUE.offer(new MovementTask(task, priority));
    }
    public static boolean registerCallback(OnMovementUpdate task) {
        return QUEUE.offer(new MovementTask(task, PriorityEnum.DEFAULT));
    }

    public static boolean unregisterCallback(OnMovementUpdate task) {
        return QUEUE.remove(task); // equals() is overwritten so this will work
    }

    public PositionRotationManager() {
        super("PositionRotationManager");
    }

    private AngleN originalAngles = AngleN.ZERO;
    private Vec3d originalPosition = Vec3d.ZERO;

    private AngleN getAngles(EntityPlayer entity) {
        return AngleN.degrees(entity.rotationPitch, entity.rotationYaw);
    }

    @SubscribeEvent
    public void onMovementUpdatePre(LocalPlayerUpdateMovementEvent.Pre event) {
        originalAngles = getAngles(event.getLocalPlayer());
        originalPosition = event.getLocalPlayer().getPositionVector();

        // process tasks until there are none left or one changes the players view angles
        for(MovementTask task : QUEUE) {
            task.onUpdate(event.getLocalPlayer());
            if(!originalAngles.equals(getAngles(event.getLocalPlayer())))
                break;
        }
    }

    @SubscribeEvent
    public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {

    }

    public interface OnMovementUpdate {
        boolean onUpdate(EntityPlayerSP localPlayer);
    }

    private static class MovementTask implements OnMovementUpdate, Comparable<MovementTask> {
        private final OnMovementUpdate task;
        private final PriorityEnum priority;

        private MovementTask(OnMovementUpdate task, PriorityEnum priority) {
            Objects.requireNonNull(task);
            Objects.requireNonNull(priority);
            this.task = task;
            this.priority = priority;
        }

        @Override
        public boolean onUpdate(EntityPlayerSP localPlayer) {
            return task.onUpdate(localPlayer);
        }

        @Override
        public int compareTo(MovementTask o) {
            return priority.compareTo(o.priority);
        }

        @Override
        public int hashCode() {
            return task.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof MovementTask && Objects.equals(task, ((MovementTask) obj).task)) || (obj instanceof OnMovementUpdate && Objects.equals(task, obj));
        }
    }
}
