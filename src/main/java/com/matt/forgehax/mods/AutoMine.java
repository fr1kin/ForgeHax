package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.LeftClickCounterUpdateEvent;
import com.matt.forgehax.asm.events.OnSendClickBlockToControllerEvent;
import com.matt.forgehax.asm.events.ShouldAllowUserInputEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

@RegisterMod
public class AutoMine extends ToggleMod {
    private boolean pressed = false;

    public AutoMine() {
        super(Category.PLAYER, "AutoMine", false, "Auto mine blocks");
    }

    @Override
    protected void onEnabled() {
        Bindings.attack.bind();
    }

    @Override
    protected void onDisabled() {
        Bindings.attack.setPressed(false);
        Bindings.attack.unbind();
        pressed = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(getLocalPlayer() == null || getWorld() == null)
            return;

        switch (event.phase) {
            case START: {
                RayTraceResult tr = MC.objectMouseOver;

                if (tr == null || tr.getBlockPos() == null)
                    return;

                IBlockState state = getWorld().getBlockState(tr.getBlockPos());

                if (tr.typeOfHit != RayTraceResult.Type.BLOCK || Material.AIR.equals(state.getMaterial()))
                    return;

                Bindings.attack.setPressed(true);
                pressed = true;
                break;
            }
            case END:
                pressed = false;
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(ShouldAllowUserInputEvent event) {
        if(getWorld() != null && getLocalPlayer() != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClickCouterUpdate(LeftClickCounterUpdateEvent event) {
        event.setValue(event.getCurrentValue());
    }

    @SubscribeEvent
    public void onBlockCounterUpdate(OnSendClickBlockToControllerEvent event) {
        if(pressed) event.setClicked(true);
    }
}
