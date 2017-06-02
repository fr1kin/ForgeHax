package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.CommandBuilder;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created on 6/1/2017 by fr1kin
 */

/**
 * Mod that will be hidden and not show up in the mod list
 */
public class SilentMod extends BaseMod {
    public SilentMod(String name, String desc) {
        super(name, desc);
    }

    @Override
    public void startup() {}

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void toggle() {}

    @Override
    public void update() {}

    // Don't make a command
    @Nullable
    @Override
    protected final CommandBuilder onBuildingModCommand(CommandBuilder builder) {
        return null;
    }

    // Don't build a gui element
    @Override
    public final void onConfigBuildGui(List<IConfigElement> elements) {}
}
