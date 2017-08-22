package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.blocks.BlockEntry;
import com.matt.forgehax.util.blocks.properties.BoundProperty;
import com.matt.forgehax.util.blocks.properties.ColorProperty;
import com.matt.forgehax.util.command.ExecuteData;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.options.BlockEntryProcessor;
import com.matt.forgehax.util.command.options.OptionBuilders;
import com.matt.forgehax.util.command.options.OptionProcessors;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * Created on 5/5/2017 by fr1kin
 */
@RegisterMod
public class Markers extends ToggleMod implements BlockModelRenderListener {
    // TODO: Bug when a render chunk is empty according to isEmptyLayer but actually contains tile entities with an invisible render layer type. This will cause them not to be rendered provided they are the only blocks within that region.

    // TODO:
    // there are two bugs currently
    // 1) isChunkEmpty stops chunk from being processed (easy fix - just add or in the statement, but this may slow down chunk loading)
    // 2) sometimes while one chunk is still being processed another thread will start processing it (this is why the exception
    //      RuntimeException("attempted to take a tessellator despite not needing one") is thrown). i still havent figured out a way to
    //      safely terminate the other process, but this will cause chunks to be skipped and old buffer data to be rendered.
    //      One solution is how schematica does it (essentially copies the render code), this would be faster but would be alot of work.

    private static final int VERTEX_BUFFER_COUNT = 100;
    private static final int VERTEX_BUFFER_SIZE = 0x200;

    private static TesselatorCache cache = new TesselatorCache(VERTEX_BUFFER_COUNT, VERTEX_BUFFER_SIZE);
    public static void setCache(TesselatorCache cache) {
        Markers.cache = cache;
    }

    private Renderers renderers = new Renderers();
    private Vec3d renderingOffset = new Vec3d(0, 0, 0);

    public final Options<BlockEntry> options = getCommandStub().builders().<BlockEntry>newOptionsBuilder()
            .name("options")
            .description("Marker block options")
            .supplier(Sets::newConcurrentHashSet)
            .factory(BlockEntry::new)
            .build();

    public final Setting<Boolean> clear_buffer = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("clear_buffer")
            .description("Clear the buffer instead of disabling depth")
            .defaultTo(false)
            .build();

    public final Setting<Boolean> anti_aliasing = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("antialiasing")
            .description("Enables antialiasing on lines")
            .defaultTo(false)
            .build();

    public final Setting<Integer> anti_aliasing_max = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("antialiasing_max")
            .description("Maximum number of render elements allowed in a render chunk until antialiasing is disabled")
            .defaultTo(0)
            .build();

    public Markers() {
        super("Markers", false, "Renders a box around a block");
    }

    private void setRenderers(Renderers renderers) {
        if(this.renderers != null) this.renderers.unregisterAll();
        this.renderers = renderers;
    }

    private void reloadRenderers() {
        if(MC.isCallingFromMinecraftThread()) {
            if (Helper.getWorld() != null) MC.renderGlobal.loadRenderers();
        } else MC.addScheduledTask(this::reloadRenderers);
    }

    @Override
    public void onLoad() {
        options.builders().newCommandBuilder()
                .name("add")
                .description("Adds block to block esp")
                .options(OptionBuilders::rgba)
                .processor(OptionProcessors::rgba)
                .options(OptionBuilders::meta)
                .processor(OptionProcessors::meta)
                .options(OptionBuilders::id)
                .options(OptionBuilders::regex)
                .options(OptionBuilders::bounds)
                .processor(BlockEntryProcessor::buildCollection)
                .processor(BlockEntryProcessor::processBounds)
                .processor(BlockEntryProcessor::processColor)
                .processor(data -> {
                    data.requiredArguments(1);
                    data.requiresEntry("entries");

                    Collection<BlockEntry> entries = data.get("entries");

                    final boolean isColorPresent = data.get("isColorPresent", false);

                    final int colorBuffer = data.get("colorBuffer", Utils.Colors.WHITE);

                    entries.forEach(entry -> {
                        // check if there is an existing entry already in the list
                        // if so then append options to it
                        final BlockEntry existing = options.get(entry);
                        if(existing != null) {
                            // set color if a color was specified by the client
                            if(isColorPresent) existing.getWritableProperty(ColorProperty.class).set(colorBuffer);
                            // copy bounds from entry into the existing one
                            entry.getReadableProperty(BoundProperty.class)
                                    .getAll()
                                    .forEach(bound -> existing.getWritableProperty(BoundProperty.class).add(bound.getMin(), bound.getMax()));
                            data.markSuccess();
                        } else if(options.add(entry)) {
                            Helper.printMessage(String.format("Added block \"%s\"", entry.getPrettyName()));
                            data.markSuccess();
                        } else {
                            Helper.printMessage(String.format("Failed to add block \"%s\"", entry.getPrettyName()));
                            data.markFailed(ExecuteData.State.SUCCESS);
                        }
                    });
                })
                .success(cmd -> this.reloadRenderers())
                .build();
        options.builders().newCommandBuilder()
                .name("remove")
                .description("Removes block to block esp")
                .options(OptionBuilders::meta)
                .processor(OptionProcessors::meta)
                .options(OptionBuilders::id)
                .options(OptionBuilders::regex)
                .options(OptionBuilders::bounds)
                .processor(BlockEntryProcessor::buildCollection)
                .processor(BlockEntryProcessor::processBounds)
                .processor(data -> {
                    data.requiredArguments(1);
                    data.requiresEntry("entries");

                    Collection<BlockEntry> entries = data.get("entries");

                    final boolean isBoundPresent = data.has("bounds");

                    entries.forEach(entry -> {
                        final BlockEntry existing = options.get(entry);
                        if(existing != null) {
                            if(isBoundPresent) {
                                // copy bounds from entry into the existing one
                                entry.getReadableProperty(BoundProperty.class)
                                        .getAll()
                                        .forEach(bound -> existing.getWritableProperty(BoundProperty.class).remove(bound.getMin(), bound.getMax()));
                                data.markSuccess();
                            } else if(options.remove(existing)) {
                                Helper.printMessage(String.format("Removed block \"%s\"", entry.getPrettyName()));
                                data.markSuccess();
                            }
                        } else if(entries.size() <= 1) {
                            Helper.printMessage(String.format("Failed to remove block \"%s\"", entry.getPrettyName()));
                            data.markFailed(ExecuteData.State.SUCCESS);
                        }
                    });
                })
                .success(cmd -> this.reloadRenderers())
                .build();
    }

    @Override
    public void onUnload() {
        options.forEach(BlockEntry::cleanupProperties);
        options.serialize();
    }

    @Override
    public void onEnabled() {
        options.deserialize();
        Listeners.BLOCK_MODEL_RENDER_LISTENER.register(this);
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable();
        reloadRenderers();
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
    public String getDebugDisplayText() {
        int cacheSize = cache != null ? cache.size() : 0;
        int cacheCapacity = cache != null ? cache.getCapacity() : 0;
        int renderersSize = renderers != null ? renderers.size() : 0;
        ViewFrustum frustum = FastReflection.Fields.cuck
        int mcRenderChunksSize = (frustum != null && frustum.renderChunks != null) ? frustum.renderChunks.length : 0;
        return String.format("%s [C:%d/%d,R:%d,mcR:%d]", getModName(), cacheSize, cacheCapacity, renderersSize, mcRenderChunksSize);
    }*/

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Load event) {
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
            setCache(new TesselatorCache(VERTEX_BUFFER_COUNT, VERTEX_BUFFER_SIZE));
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
        if(renderers != null) try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(info::freeTessellator));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onPreBuildChunk(BuildChunkEvent.Pre event) {
        if(renderers != null) try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(() -> {
                GeometryTessellator tess = info.takeTessellator();
                if (tess != null) {
                    tess.beginLines();
                    info.resetRenderCount();
                    BlockPos renderPos = event.getRenderChunk().getPosition();
                    tess.setTranslation(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());
                }
            }));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent
    public void onPostBuildChunk(BuildChunkEvent.Post event) {
        if(renderers != null) try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.compute(() -> {
                GeometryTessellator tess = info.getTessellator();
                if (tess != null && info.isBuilding()) {
                    tess.getBuffer().finishDrawing();
                    info.setUploaded(false);
                }
            }));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @Override
    public void onBlockRenderInLoop(final RenderChunk renderChunk, final Block block, final IBlockState state, final BlockPos pos) {
        if(renderers != null) try {
            renderers.computeIfPresent(renderChunk, (chk, info) -> info.compute(() -> {
                GeometryTessellator tess = info.getTessellator();
                if (tess != null && FastReflection.Fields.BufferBuilder_isDrawing.get(tess.getBuffer(), false)) {
                    BlockEntry blockEntry = options.get(state);
                    if(blockEntry != null
                            && blockEntry.getReadableProperty(BoundProperty.class).isWithinBoundaries(pos.getY())) {
                        AxisAlignedBB bb = state.getSelectedBoundingBox(Helper.getWorld(), pos);
                        GeometryTessellator.drawLines(
                                tess.getBuffer(),
                                bb.minX, bb.minY, bb.minZ,
                                bb.maxX, bb.maxY, bb.maxZ,
                                GeometryMasks.Line.ALL,
                                blockEntry.getReadableProperty(ColorProperty.class).getAsBuffer()
                        );
                        info.incrementRenderCount();
                    }
                }
            }));
        } catch (Exception e) {
            handleException(renderChunk, e);
        }
    }

    @SubscribeEvent
    public void onChunkUploaded(ChunkUploadedEvent event) {
        if(renderers != null) try {
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
        if(renderers != null) try {
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
        if(renderers != null) try {
            renderers.computeIfPresent(event.getRenderChunk(), (chk, info) -> info.setRendering(true));
        } catch (Exception e) {
            handleException(event.getRenderChunk(), e);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(renderers != null) try {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            if(!clear_buffer.get())
                GlStateManager.disableDepth();
            else {
                GlStateManager.clearDepth(1.f);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            }

            final boolean aa_enabled = anti_aliasing.get();
            final int aa_max = anti_aliasing_max.get();

            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

            renderers.forEach((chk, info) -> {
                if (info.isVboPresent() && info.isRendering()) {
                    if(aa_enabled && (aa_max == 0 || info.getRenderCount() <= aa_max))
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);

                    GlStateManager.pushMatrix();

                    BlockPos pos = chk.getPosition();
                    GlStateManager.translate(
                            (double) pos.getX() - renderingOffset.x,
                            (double) pos.getY() - renderingOffset.y,
                            (double) pos.getZ() - renderingOffset.z
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

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);

                    info.setRendering(false);
                }
            });

            GL11.glDisable(GL11.GL_LINE_SMOOTH);

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
        private boolean uploaded = false;

        private int renderCount = 0;
        private int currentRenderCount = 0;

        public boolean isRendering() {
            return rendering;
        }

        public void setRendering(boolean rendering) {
            this.rendering = rendering;
        }

        public boolean isBuilding() {
            return tessellator != null && FastReflection.Fields.BufferBuilder_isDrawing.get(tessellator.getBuffer());
        }

        public boolean isUploaded() {
            return uploaded;
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded = uploaded;
        }

        public void incrementRenderCount() {
            renderCount++;
        }

        public void resetRenderCount() {
            renderCount = 0;
        }

        public int getRenderCount() {
            return renderCount;
        }

        public GeometryTessellator getTessellator() {
            return tessellator;
        }

        public void setTessellator(GeometryTessellator tessellator) {
            this.tessellator = tessellator;
        }

        public GeometryTessellator takeTessellator() {
            if(this.tessellator == null && cache != null) {
                this.tessellator = cache.take();
                return this.tessellator;
            } else throw new RuntimeException("attempted to take a tessellator despite not needing one");
        }

        public void freeTessellator() {
            final GeometryTessellator tess = tessellator;
            if(tess != null) {
                try {
                    // this would shouldn't happen but it could
                    if (isBuilding()) tess.getBuffer().finishDrawing();
                    tess.setTranslation(0.D, 0.D, 0.D);
                } finally {
                    if(cache != null) cache.free(tess);
                    tessellator = null;
                }
            }
        }

        public BufferBuilder getBuffer() {
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
                Helper.printStackTrace(e);
                return null; // this shouldn't happen
            }
        }

        public void free(final GeometryTessellator tessellator) {
            try {
                if (originals.contains(tessellator)) buffers.add(tessellator);
            } catch(Exception e) {
                Helper.getLog().warn("Something went terrible wrong and now there is one less tessellator in the cache");
            }
        }
    }

    private static void handleException(RenderChunk renderChunk, Throwable throwable) {
        Helper.getLog().error(throwable.toString());
    }
}
