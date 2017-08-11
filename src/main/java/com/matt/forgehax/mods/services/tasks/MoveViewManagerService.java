package com.matt.forgehax.mods.services.tasks;

import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.task.ViewTask;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

    }

    @SubscribeEvent
    public void onMovementUpdatePost(LocalPlayerUpdateMovementEvent.Post event) {

    }
}
