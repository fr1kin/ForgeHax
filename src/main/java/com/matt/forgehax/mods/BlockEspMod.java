package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.google.common.collect.*;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.blocks.BlockEntry;
import com.matt.forgehax.util.blocks.BlockOptions;
import com.matt.forgehax.util.entity.EntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class BlockEspMod extends ToggleMod implements BlockModelRenderListener {
    private static TesselatorCache cache = new TesselatorCache(100, 0x20000);

    public static void setCache(TesselatorCache cache) {
        BlockEspMod.cache = cache;
    }

    private final ThreadLocal<GeometryTessellator> localTessellator = new ThreadLocal<>();

    private Renderers renderers = new Renderers();
    private Vec3d renderingOffset = new Vec3d(0, 0, 0);

    private final BlockOptions options = new BlockOptions(new File(Wrapper.getMod().getConfigFolder(), "block_esp_list.json"));

    public BlockEspMod() {
        super("BlockESP", false, "Renders a box around a block");
    }

    private void setRenderers(Renderers renderers) {
        if(this.renderers != null) this.renderers.unregisterAll();
        this.renderers = renderers;
    }

    @Override
    public void onEnabled() {
        options.read();
        Listeners.BLOCK_MODEL_RENDER_LISTENER.register(this);
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable();
    }

    @Override
    public void onDisabled() {
        setRenderers(null);
        setCache(null);
        Listeners.BLOCK_MODEL_RENDER_LISTENER.unregister(this);
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable();
    }

    /*
    @Override
    public String getDisplayText() {
        int cacheSize = cache != null ? cache.size() : 0;
        int cacheCapacity = cache != null ? cache.getCapacity() : 0;
        int renderersSize = renderers != null ? renderers.size() : 0;
        ViewFrustum frustum = FastReflection.ClassRenderGlobal.getViewFrustum(MC.renderGlobal);
        int mcRenderChunksSize = (frustum != null && frustum.renderChunks != null) ? frustum.renderChunks.length : 0;
        return String.format("%s [C:%d/%d,R:%d,mcR:%d]", getModName(), cacheSize, cacheCapacity, renderersSize, mcRenderChunksSize);
    }
    */

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        try {
            setRenderers(null);
            setCache(null);
        } catch (Exception e) {
            handleException(null, e);
        }
    }

    @SubscribeEvent
    public void onLoadRenderers(LoadRenderersEvent event) {
        try {
            setRenderers(new Renderers());
            setCache(new TesselatorCache(100, 0x20000));
            // allocate all space needed
            for (RenderChunk renderChunk : event.getViewFrustum().renderChunks) {
                renderers.register(renderChunk);
            }
        } catch (Exception e) {
            handleException(null, e);
        }
    }

    @SubscribeEvent
    public void onWorldRendererDeallocated(WorldRendererDeallocatedEvent event) {
        if(renderers == null) return;
        try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(info::freeTessellator));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onPreBuildChunk(BuildChunkEvent.Pre event) {
        if(renderers == null) return;
        try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(() -> {
                GeometryTessellator tess = info.takeTessellator();
                if (tess != null) {
                    tess.beginLines();
                    info.setBuilding(true);

                    BlockPos renderPos = event.getRenderChunk().getPosition();
                    tess.setTranslation(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());

                    // block render function wont have a RenderChunk instance passed through it
                    localTessellator.set(tess);
                }
            }));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onPostBuildChunk(BuildChunkEvent.Post event) {
        try {
            if(renderers != null) renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(() -> {
                GeometryTessellator tess = info.getTessellator();
                if (tess != null && info.isBuilding()) {
                    tess.getBuffer().finishDrawing();
                    info.setBuilding(false);
                    info.setUploaded(false);
                }
            }));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        } finally {
            localTessellator.remove();
        }
    }

    @Override
    public void onBlockModelRender(IBlockAccess access, IBakedModel model, IBlockState state, BlockPos pos, VertexBuffer buffer) {
        if(renderers == null) return;
        GeometryTessellator tess = null;
        try {
            BlockEntry blockEntry = options.getBlockEntry(state);
            if (blockEntry != null) {
                // get the tessellator created in onPreBuildChunk specific to the current thread
                tess = this.localTessellator.get();
                if (tess != null) {
                    Vec3d offset = state.getOffset(access, pos);
                    BlockPos realPos = pos.add(offset.xCoord, offset.yCoord, offset.zCoord);
                    AxisAlignedBB bb = state.getBoundingBox(access, realPos);
                    double x = realPos.getX(), y = realPos.getY(), z = realPos.getZ();
                    GeometryTessellator.drawLines(
                            tess.getBuffer(),
                            x + bb.minX, y + bb.minY, z + bb.minZ,
                            x + bb.maxX, y + bb.maxY, z + bb.maxZ,
                            GeometryMasks.Line.ALL,
                            blockEntry.getColorBuffer()
                    );
                }
            }
        } catch (Exception e) {
            if(tess != null) {
                localTessellator.remove(); // doesn't seem to cause any issues
            } else handleException(null, e);
        }
    }

    @SubscribeEvent
    public void onChunkUploaded(ChunkUploadedEvent event) {
        if(renderers == null) return;
        try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> {
                if (!info.isUploaded()) {
                    info.uploadVbo();
                    info.setUploaded(true);
                }
            });
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onChunkDeleted(DeleteGlResourcesEvent event) {
        if(renderers == null) return;
        try {
            renderers.unregister(event.getRenderChunk());
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onSetupTerrain(SetupTerrainEvent event) {
        // doesn't have to be here, I just conveniently had this hook
        renderingOffset = EntityUtils.getInterpolatedPos(event.getRenderEntity(), MC.getRenderPartialTicks());
    }

    @SubscribeEvent
    public void onRenderChunkAdded(AddRenderChunkEvent event) {
        if(renderers == null) return;
        try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.setRendering(true));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(renderers == null) return;
        try {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableDepth();

            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

            renderers.forEach((chk, info) -> {
                if (info.isVboPresent() && info.isRendering()) {
                    GlStateManager.pushMatrix();

                    BlockPos pos = chk.getPosition();
                    GlStateManager.translate(
                            (double) pos.getX() - renderingOffset.xCoord,
                            (double) pos.getY() - renderingOffset.yCoord,
                            (double) pos.getZ() - renderingOffset.zCoord
                    );

                    chk.multModelviewMatrix();

                    info.getVbo().bindBuffer();

                    GlStateManager.glVertexPointer(
                            DefaultVertexFormats.POSITION_3F.getElementCount(),
                            DefaultVertexFormats.POSITION_3F.getType().getGlConstant(),
                            DefaultVertexFormats.POSITION_3F.getSize() + DefaultVertexFormats.COLOR_4UB.getSize(),
                            0
                    );
                    GlStateManager.glColorPointer(
                            DefaultVertexFormats.COLOR_4UB.getElementCount(),
                            DefaultVertexFormats.COLOR_4UB.getType().getGlConstant(),
                            DefaultVertexFormats.POSITION_3F.getSize() + DefaultVertexFormats.COLOR_4UB.getSize(),
                            DefaultVertexFormats.POSITION_3F.getSize()
                    );

                    info.getVbo().drawArrays(GL11.GL_LINES);

                    GlStateManager.popMatrix();

                    info.setRendering(false);
                }
            });

            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);

            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        } catch (Exception e) {
            handleException(null, e);
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKey() == Keyboard.KEY_B) {
            debugStuff();
        }
    }

    private String getTessList(Collection<GeometryTessellator> list) {
        StringBuilder b = new StringBuilder();
        Iterator<GeometryTessellator> it = list.iterator();
        while(it.hasNext()) {
            GeometryTessellator t = it.next();
            b.append(t != null ? Integer.toHexString(t.hashCode()) : "null");
            if(it.hasNext()) b.append(",");
        }
        return b.toString();
    }

    private void debugStuff() {
        final StringBuilder builder = new StringBuilder();
        final Renderers r = renderers;
        final TesselatorCache tc = cache;
        builder.append("##### Debug output for BlockEsp #####\n");
        if(r != null) {
            builder.append(String.format("Renderers [%d] {\n", r.size()));
            r.forEach((chk, info) -> {
                builder.append('\t');
                builder.append(String.format("[%s] = {\n", Integer.toHexString(chk.hashCode())));
                builder.append(String.format("\t\ttessellator=%s\n", info.getTessellator() != null ? Integer.toHexString(info.getTessellator().hashCode()) : "null"));
                builder.append(String.format("\t\tvbo=%s\n", info.getVbo() != null ? Integer.toHexString(info.getVbo().hashCode()) : "null"));
                builder.append(String.format("\t\tisRendering=%s\n", info.isRendering()));
                builder.append(String.format("\t\tisBuilding=%s\n", info.isBuilding()));
                builder.append(String.format("\t\tisUploaded=%s\n", info.isUploaded()));
                builder.append(String.format("\t\tisLocked=%s\n", info.lock.isLocked()));
                /*
                builder.append(String.format("\t\tused[%d]=%s\n", info.usedTessellators.size(), getTessList(info.usedTessellators)));
                builder.append(String.format("\t\tfreed[%d]=%s\n", info.freedTessellators.size(), getTessList(info.freedTessellators)));
                String good = "n/a";
                if(info.tessellator == null) good = Boolean.toString(info.usedTessellators.size() == info.freedTessellators.size());
                builder.append(String.format("\t\tgood=%s\n", good));*/
                builder.append("\t}\n");
            });
            builder.append("}\n");
        }
        if(tc != null) {
            builder.append("Cache {\n");
            builder.append(String.format("\tQueue [%d/%d] {\n", tc.size(), tc.getCapacity()));
            tc.buffers.forEach(t -> {
                builder.append(String.format("\t\t%s\n", Integer.toHexString(t.hashCode())));
            });
            builder.append("\t}\n");
            List<GeometryTessellator> missing = tc.originals.stream()
                    .filter(t -> !tc.buffers.contains(t))
                    .collect(Collectors.toList());
            builder.append(String.format("\tMissing [%d/%d] {\n", missing.size(), tc.getCapacity()));
            missing.forEach(t -> {
                builder.append(String.format("\t\t%s\n", Integer.toHexString(t.hashCode())));
            });
            builder.append("\t}\n");
            builder.append("}\n");
        }
        try {
            Files.write(new File(Wrapper.getMod().getBaseDirectory(), "tess_dump.txt").toPath(), builder.toString().getBytes());
            Wrapper.getLog().info("Dumped log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Renderers {
        private final Map<RenderChunk, RenderInfo> data = Maps.newConcurrentMap();

        public RenderInfo register(final RenderChunk renderChunk) {
            return data.compute(renderChunk, (chk, info) -> {
                if (info != null) RenderInfo.shutdown(info);
                return new RenderInfo();
            });
        }

        public void unregister(RenderChunk renderChunk) {
            RenderInfo info = get(renderChunk);
            if (info != null) RenderInfo.shutdown(info);
            data.remove(renderChunk);
        }

        public void unregisterAll() {
            forEach((chk, info) -> unregister(chk));
        }

        public Collection<RenderInfo> removeAll() {
            Collection<RenderInfo> infos = data.values();
            data.clear();
            return infos;
        }

        public RenderInfo get(RenderChunk renderChunk) {
            return data.get(renderChunk);
        }

        public boolean has(RenderChunk renderChunk) {
            return get(renderChunk) != null;
        }

        public int size() {
            return data.size();
        }

        public void computeIfPresent(RenderChunk renderChunk, BiConsumer<RenderChunk, RenderInfo> biConsumer) {
            RenderInfo info = get(renderChunk);
            if (info != null) biConsumer.accept(renderChunk, info);
        }

        public void forEach(BiConsumer<RenderChunk, RenderInfo> action) {
            data.forEach(action);
        }
    }

    private static class RenderInfo {
        private final ReentrantLock lock = new ReentrantLock();

        private GeometryTessellator tessellator = null;
        private net.minecraft.client.renderer.vertex.VertexBuffer vbo = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.POSITION_COLOR);

        private boolean rendering = false;
        private boolean building = false;
        private boolean uploaded = false;

        //private List<GeometryTessellator> usedTessellators = Lists.newArrayList();
        //private List<GeometryTessellator> freedTessellators = Lists.newArrayList();

        public boolean isRendering() {
            return rendering;
        }

        public void setRendering(boolean rendering) {
            this.rendering = rendering;
        }

        public boolean isBuilding() {
            return building;
        }

        public void setBuilding(boolean building) {
            this.building = building;
        }

        public boolean isUploaded() {
            return uploaded;
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded = uploaded;
        }

        public GeometryTessellator getTessellator() {
            return tessellator;
        }

        public void setTessellator(GeometryTessellator tessellator) {
            //if(tessellator != null) usedTessellators.add(tessellator);
            this.tessellator = tessellator;
        }

        public GeometryTessellator takeTessellator() {
            if(this.tessellator == null) {
                this.tessellator = cache.take();
                return this.tessellator;
            } else throw new RuntimeException("attempted to take a tessellator despite not needing one");
        }

        public void freeTessellator() {
            final GeometryTessellator tess = tessellator;
            if(tess != null) {
                try {
                    // this would shouldn't happen but it could
                    if (isBuilding()) {
                        tess.getBuffer().finishDrawing();
                        building = false;
                    }
                    tess.setTranslation(0.D, 0.D, 0.D);
                } finally {
                    //freedTessellators.add(tess);
                    cache.free(tess);
                    tessellator = null;
                }
            }
        }

        public VertexBuffer getBuffer() {
            return getTessellator().getBuffer();
        }

        public net.minecraft.client.renderer.vertex.VertexBuffer getVbo() {
            return vbo;
        }

        public boolean isVboPresent() {
            return vbo != null;
        }

        public void deleteVbo() {
            if(vbo != null) {
                // delete VBO (cannot use the ID anymore, must reallocate)
                vbo.deleteGlBuffers();
                vbo = null;
            }
        }

        public void uploadVbo() {
            GeometryTessellator tess = tessellator;
            if(vbo != null && tess != null) {
                // allocate the vertex buffer
                tess.getBuffer().reset();
                vbo.bufferData(tess.getBuffer().getByteBuffer());
            }
        }

        public void compute(Runnable task) {
            lock.lock();
            try {
                task.run();
            } finally {
                lock.unlock();
            }
        }

        public static void shutdown(final RenderInfo info) {
            if(info == null) return;
            if(MC.isCallingFromMinecraftThread()) {
                try {
                    info.deleteVbo();
                } finally {
                    info.compute(info::freeTessellator);
                }
            } else MC.addScheduledTask(() -> shutdown(info));
        }
    }

    private static class TesselatorCache {
        private static final int DEFAULT_BUFFER_SIZE = 0x20000;

        private final BlockingQueue<GeometryTessellator> buffers;
        private final Set<GeometryTessellator> originals;
        private final int capacity;

        public TesselatorCache(int capacity, int bufferSize) {
            this.capacity = capacity;
            buffers = Queues.newArrayBlockingQueue(capacity);
            for(int i = 0; i < capacity; i++) buffers.offer(new GeometryTessellator(bufferSize));
            originals = Collections.unmodifiableSet(Sets.newHashSet(buffers));
        }

        public TesselatorCache(int capacity) {
            this(capacity, DEFAULT_BUFFER_SIZE);
        }

        public int getCapacity() {
            return capacity;
        }

        public int size() {
            return buffers.size();
        }

        public GeometryTessellator take() {
            try {
                return buffers.take();
            } catch (InterruptedException e) {
                MOD.printStackTrace(e);
                return null; // this shouldn't happen
            }
        }

        public void free(final GeometryTessellator tessellator) {
            try {
                if (originals.contains(tessellator)) buffers.add(tessellator);
            } catch(Exception e) {
                Wrapper.getLog().warn("Something went terrible wrong and now there is one less tessellator in the cache");
            }
        }
    }

    private static void handleException(RenderChunk renderChunk, Throwable throwable) {
        Wrapper.getLog().error(throwable.toString());
    }
}
