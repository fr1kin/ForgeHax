package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static com.matt.forgehax.Wrapper.*;

@RegisterMod
public class AutoSprintMod extends ToggleMod {
    private boolean isBound = false;

    public static final String[] modes = new String[] {"ALWAYS", "LEGIT"};

    public Property mode;

    public AutoSprintMod() {
        super("AutoSprint", false, "Automatically sprints");
    }

    private void startSprinting() {
        switch (mode.getString()) {
            case "ALWAYS":
                if(!getLocalPlayer().isCollidedHorizontally)
                    getLocalPlayer().setSprinting(true);
                break;
            default:
            case "LEGIT":
                if (!isBound) {
                    Bindings.sprint.bind();
                    isBound = true;
                }
                if (!Bindings.sprint.getBinding().isKeyDown())
                    Bindings.sprint.setPressed(true);
                break;
        }
    }

    private void stopSprinting() {
        if(isBound) {
            Bindings.sprint.setPressed(false);
            Bindings.sprint.unbind();
            isBound = false;
        }
    }

    @Override
    public void onLoadConfiguration(Configuration configuration) {
        addSettings(
                mode = configuration.get(getModCategory().getName(),
                        "mode",
                        modes[0],
                        "Sprint mode (ALWAYS=setSprinting,LEGIT=simulate key press)",
                        modes
                )
        );
    }

    /**
     * Stop sprinting when the mod is disabled
     */
    @Override
    public void onDisabled() {
        stopSprinting();
    }

    /**
     * Start sprinting every update tick
     */
    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(event.getEntityLiving().moveForward > 0 &&
                !event.getEntityLiving().isCollidedHorizontally &&
                !event.getEntityLiving().isSneaking()) {
            startSprinting();
        }
    }
}
