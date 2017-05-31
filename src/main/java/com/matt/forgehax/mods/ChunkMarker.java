package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.google.common.collect.Maps;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/**
 * Created on 4/28/2017 by fr1kin
 */

@RegisterMod
public class ChunkMarker extends ToggleMod {
    public Property offsetY;

    public ChunkMarker() {
        super("ChunkMarker", false, "Marks all previously loaded chunks in the current session");
    }

    private final Map<ChunkCoords, Boolean> CHUNKS = Maps.newConcurrentMap();

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                offsetY = configuration.get(getModName(),
                        "offset_y",
                        0,
                        "Y ESP Offset"
                )
        );
    }

    @Override
    public void onEnabled() {
        CHUNKS.clear();
    }

    @Override
    public void onDisabled() {
        CHUNKS.clear();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        //CHUNKS.clear();
    }

    @SubscribeEvent
    public void onLoadChunk(ChunkEvent.Load event) {
        CHUNKS.put(new ChunkCoords(event.getChunk()), true);
    }

    @SubscribeEvent
    public void onUnloadChunk(ChunkEvent.Unload event) {
        CHUNKS.put(new ChunkCoords(event.getChunk()), false);
    }

    @SubscribeEvent
    public void onRender(RenderEvent event) {
        event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        CHUNKS.forEach((chunk, loaded) -> {
            GeometryTessellator.drawCuboid(event.getBuffer(), chunk.getStart(), chunk.getEnd(), GeometryMasks.Line.ALL, loaded ? Utils.Colors.GREEN : Utils.Colors.RED);
        });

        event.getTessellator().draw();
    }

    private static class ChunkCoords {
        private final BlockPos start;
        private final BlockPos end;

        public ChunkCoords(Chunk chunk) {
            ChunkPos pos = chunk.getPos();
            start = new BlockPos(pos.getXStart(), 0, pos.getZStart());
            end = new BlockPos(pos.getXEnd(), 0, pos.getZEnd());
        }

        public BlockPos getStart() {
            return start;
        }

        public BlockPos getEnd() {
            return end;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ChunkCoords)
                return start.equals(((ChunkCoords) obj).start);
            else return false;
        }
    }
}
