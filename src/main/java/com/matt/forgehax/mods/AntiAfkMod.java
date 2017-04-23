package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.key.KeyBindingHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiAfkMod extends ToggleMod {
    public Property tickInterval;
    public Property moveDistance;

    private Vec3d startPos;
    private int moveDirection = 0;
    private boolean isAutoMoving = false;

    private int tickCounter = 0;

    public AntiAfkMod() {
        super("AntiAFK", false, "Moves automatically to prevent being kicked");
    }

    public KeyBindingHandler getMoveBinding() {
        switch (Math.floorMod(moveDirection, 4)) {
            case 0:
                return Bindings.forward;
            case 1:
                return Bindings.left;
            case 2:
                return Bindings.back;
            default:
            case 3:
                return Bindings.right;
        }
    }

    public void setMoving(boolean b) {
        isAutoMoving = b;
    }

    public boolean isMoving() {
        return isAutoMoving;
    }

    @Override
    public void loadConfig(Configuration configuration) {
        super.loadConfig(configuration);
        addSettings(
                tickInterval = configuration.get(getModName(), "interval", 200, "Amount of ticks to wait before moving again"),
                moveDistance = configuration.get(getModName(), "distance", 0.25, "Distance to move before stopping")
        );
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if(isMoving()) {
            getMoveBinding().setPressed(false);
            getMoveBinding().unbind();
            Bindings.sneak.setPressed(false);
            Bindings.sneak.unbind();
            setMoving(false);
            tickCounter = 0;
        }
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        tickCounter++;
        if(isMoving()) {
            // we are moving/starting to move
            Vec3d currentPos = event.getEntityLiving().getPositionVector();
            double distanceMoved = currentPos.distanceTo(startPos);
            if(distanceMoved >= moveDistance.getDouble()) {
                // moved the required distance, stop now
                // stop moving
                getMoveBinding().setPressed(false);
                getMoveBinding().unbind();
                // stop sneaking
                Bindings.sneak.setPressed(false);
                Bindings.sneak.unbind();
                // reset
                setMoving(false);
                tickCounter = 0;
            } else {
                // keep keys pressed
                if(!getMoveBinding().getBinding().isKeyDown())
                    getMoveBinding().setPressed(true);
                if(!Bindings.sneak.getBinding().isKeyDown())
                    Bindings.sneak.setPressed(true);
            }
        } else {
            if(tickCounter >= tickInterval.getInt()) {
                // select direction
                moveDirection++;
                // enable moving
                Bindings.sneak.bind();
                Bindings.sneak.setPressed(true);
                getMoveBinding().bind();
                getMoveBinding().setPressed(true);
                // set the start pos
                startPos = event.getEntityLiving().getPositionVector();
                setMoving(true);
            }
        }
    }
}
