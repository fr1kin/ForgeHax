package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.key.KeyBindingHandler;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiAfkMod extends ToggleMod {
    public final Setting<Integer> tick_interval = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("tick_interval")
            .description("Amount of ticks to wait before moving again")
            .defaultTo(200)
            .build();

    public final Setting<Double> move_distance = getCommandStub().builders().<Double>newSettingBuilder()
            .name("tick_interval")
            .description("Distance to move before stopping")
            .defaultTo(0.25D)
            .build();

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
    public void onDisabled() {
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
            if(distanceMoved >= move_distance.get()) {
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
            if(tickCounter >= tick_interval.get()) {
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
