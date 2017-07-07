package com.matt.forgehax.mods.services.tasks;

import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.task.ViewTask;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.util.task.manager.TaskManagers.getViewManager;

/**
 * Created on 6/15/2017 by fr1kin
 */
@RegisterMod
public class MoveViewManagerService extends ServiceMod {
    public MoveViewManagerService() {
        super("MoveViewManagerService");
    }

    private ViewTask currentViewTask = null;

    @SubscribeEvent
    public void onMovementUpdatePre(LocalPlayerUpdateMovementEvent.Pre event) {
        currentViewTask = getViewManager().getTopTask();

        if(currentViewTask != null) {
            currentViewTask.setViewAngle(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
            currentViewTask.onPreUpdate();

            getLocalPlayer().rotationPitch = currentViewTask.getPitch();
            getLocalPlayer().rotationYaw = currentViewTask.getYaw();
        }
    }

    @SubscribeEvent
    public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {
        if(currentViewTask != null) {
            currentViewTask.onPostUpdate();

            if(currentViewTask.shouldReset()) {
                getLocalPlayer().rotationPitch = currentViewTask.getOriginalPitch();
                getLocalPlayer().rotationYaw = currentViewTask.getOriginalYaw();
            }

            currentViewTask.finished();
        }
    }
}
