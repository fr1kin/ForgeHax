package com.matt.forgehax.asm.events.ReplacementHooks;

import net.minecraft.world.World;

public class WorldEvent {
    private final World world;

    public WorldEvent(World world)
    {
        this.world = world;
    }

    public World getWorld()
    {
        return world;
    }

    public static class Load extends WorldEvent
    {
        public Load(World world) { super(world); }
    }

    public static class UnLoad extends WorldEvent
    {
        public UnLoad(World world) { super(world); }
    }
}
