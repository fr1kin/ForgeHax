package com.matt.forgehax.mods.services;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandStub;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Collection;

import static com.matt.forgehax.Helper.getModManager;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class BindEventService extends ServiceMod {
    public BindEventService() {
        super("BindEventService");
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        getModManager().getMods().forEach(mod -> {
            Collection<Command> cmds = mod.getCommandStub().getChildrenDeep();
            cmds.add(mod.getCommandStub());
            cmds.forEach(c -> {
                if(c instanceof CommandStub) {
                    CommandStub stub = (CommandStub) c;
                    if (stub.getBind() != null) {
                        if (stub.getBind().isPressed()) stub.onKeyPressed();
                        if (stub.getBind().isKeyDown()) stub.onKeyDown();
                    }
                }
            });
        });
    }
}
