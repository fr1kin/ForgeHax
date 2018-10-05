package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;

public abstract class RenderPlayerEvent extends Event {

    private final RenderPlayer renderer;
    public  final EntityPlayer player;
    private final float partialRenderTick;
    private final double x;
    private final double y;
    private final double z;

    public RenderPlayerEvent(RenderPlayer renderer, AbstractClientPlayer player, float partialRenderTick, double x, double y, double z) {
        this.renderer = renderer;
        this.player = player;
        this.partialRenderTick = partialRenderTick;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EntityPlayer getEntity() {
        return this.player;
    }

    public RenderPlayer getRenderer() {
        return renderer;
    }

    public float getPartialRenderTick() {
        return partialRenderTick;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public static class Pre extends RenderPlayerEvent implements Cancelable {
        public Pre(RenderPlayer renderer, AbstractClientPlayer player, float tick, double x, double y, double z) {
            super(renderer, player, tick, x, y, z);
        }

    }

    // TODO:
    /*public static class Post extends RenderPlayerEvent {
        public Post(RenderPlayer renderer, AbstractClientPlayer player, float tick, double x, double y, double z) {
            super(renderer, player, tick, x, y, z);
        }

    }*/
}