package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.task.Task;
import com.matt.forgehax.util.task.TaskManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 6/15/2017 by fr1kin
 */
@RegisterMod
public class MoveViewManagerService extends ServiceMod {
    public MoveViewManagerService() {
        super("MoveViewManagerService");
    }

    private Task.TaskProcessing processing = null;

    @SubscribeEvent
    public void onMovementUpdatePre(LocalPlayerUpdateMovementEvent.Pre event) {
        //event.setCanceled(true);
        processing = TaskManager.getTop(Task.Type.LOOK);
        if(processing != null) processing.preProcessing();
    }

    @SubscribeEvent
    public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {
        if(processing != null) processing.postProcessing();
    }
}
