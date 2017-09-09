package com.matt.forgehax.mods;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Created by Babbaj on 9/6/2017.
 *
 * The status of this mod being enabled or disabled doesnt mean anything
 * TODO: give this mod a default bind
 * TODO: hide enabled/disabled chat messsages
 */
@RegisterMod
public class GuiMod extends ToggleMod {
    public GuiMod() { super(Category.NONE, "GuiMod", false, "mod that opens and closes the gui"); }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void onEnabled() {
        if (MC.world != null) { // dont attempt to open the gui when the game is starting
            MC.displayGuiScreen(ClickGui.getInstance());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        disable();
    }

}
