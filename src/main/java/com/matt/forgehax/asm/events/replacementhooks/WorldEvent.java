package com.matt.forgehax.asm.events.replacementhooks;

import net.minecraft.client.multiplayer.WorldClient;
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

    public static class Unload extends WorldEvent
    {
        public Unload(World world) { super(world); }

        // TODO: fix verify class loading meme so this doesnt have to exist
        public Unload(WorldClient world) { super(world); }
    }
}
