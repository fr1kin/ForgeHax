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
import com.matt.forgehax.util.blocks.*;
import com.matt.forgehax.util.command.jopt.OptionHelper;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.jopt.SafeConverter;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * Created on 5/5/2017 by fr1kin
 */
@RegisterMod
public class Markers extends ToggleMod implements BlockModelRenderListener {
    // TODO: Bug when a render chunk is empty according to isEmptyLayer but actually contains tile entities with an invisible render layer type. This will cause them not to be rendered provided they are the only blocks within that region.

    private static final BlockOptions MARKER_OPTIONS = new BlockOptions(new File(Wrapper.getMod().getConfigFolder(), "markers.json"));

    private static final int VERTEX_BUFFER_COUNT = 100;
    private static final int VERTEX_BUFFER_SIZE = 0x200;

    public static BlockOptions getMarkerOptions() {
        return MARKER_OPTIONS;
    }

    private static TesselatorCache cache = new TesselatorCache(VERTEX_BUFFER_COUNT, VERTEX_BUFFER_SIZE);
    public static void setCache(TesselatorCache cache) {
        Markers.cache = cache;
    }

    private Renderers renderers = new Renderers();
    private Vec3d renderingOffset = new Vec3d(0, 0, 0);

    public Property clearBuffer;
    public Property antialiasing;
    public Property antialiasing_max;

    public Markers() {
        super("Markers", false, "Renders a box around a block");
    }

    private void setRenderers(Renderers renderers) {
        if(this.renderers != null) this.renderers.unregisterAll();
        this.renderers = renderers;
    }

    private void reloadRenderers() {
        if(MC.isCallingFromMinecraftThread()) {
            if (Wrapper.getWorld() != null) MC.renderGlobal.loadRenderers();
        } else MC.addScheduledTask(this::reloadRenderers);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                clearBuffer = configuration.get(getModName(),
                        "clearbuffer",
                        true,
                        "Will clear the depth buffer instead of disabling depth"
                ),
                antialiasing = configuration.get(getModName(),
                        "antialiasing",
                        false,
                        "Will enable anti aliasing on lines making them look smoother, but will hurt performance"
                ),
                antialiasing_max = configuration.get(getModName(),
                        "antialiasing_max",
                        0,
                        "Max number of elements allowed in a region before AA is disabled for that region (0 for no limit)"
                )
        );
    }

    @Override
    public void onLoad() {
        addCommand(new CommandBuilder()
                .setName("add")
                .setDescription("Adds block to block esp")
                .setOptionBuilder(parser -> {
                    parser.acceptsAll(Arrays.asList("red", "r"), "red")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("green", "g"), "green")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("blue", "b"), "blue")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("alpha", "a"), "alpha")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("meta", "m"), "blocks metadata id")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("id", "i"), "searches for block by id instead of name");
                    parser.acceptsAll(Arrays.asList("regex", "e"), "searches for blocks by using the argument as a regex expression");
                    parser.accepts("bounds", "Will only draw blocks from within the min-max bounds given")
                            .withRequiredArg();
                })
                .setProcessor(opts -> {
                    List<?> args = opts.nonOptionArguments();
                    if(args.size() > 0) {
                        boolean byId = opts.has("id");
                        String name = String.valueOf(args.get(0));
                        OptionHelper helper = new OptionHelper(opts);
                        final boolean wasGivenRGBA = opts.has("r") || opts.has("g") || opts.has("b") || opts.has("a");
                        int r = MathHelper.clamp(helper.getIntOrDefault("r", 255), 0, 255);
                        int g = MathHelper.clamp(helper.getIntOrDefault("g", 255), 0, 255);
                        int b = MathHelper.clamp(helper.getIntOrDefault("b", 255), 0, 255);
                        int a = MathHelper.clamp(helper.getIntOrDefault("a", 255), 0, 255);
                        int meta = helper.getIntOrDefault("m", 0);
                        try {
                            final Collection<AbstractBlockEntry> process = Sets.newHashSet();

                            if(opts.has("regex"))
                                process.addAll(BlockOptionHelper.getAllBlocksMatchingByUnlocalized(name));
                            else process.add(byId ? BlockEntry.createById(SafeConverter.toInteger(name, 0), meta) :
                                        BlockEntry.createByResource(name, meta));

                            if(opts.has("bounds")) opts.valuesOf("bounds").forEach(v -> {
                                String value = String.valueOf(v);
                                String[] mm = value.split("-");
                                if(mm.length > 1) {
                                    int min = SafeConverter.toInteger(mm[0]);
                                    int max = SafeConverter.toInteger(mm[1]);
                                    process.forEach(entry -> entry.getBounds().add(min, max));
                                } else {
                                    throw new IllegalArgumentException(String.format("Invalid argument \"%s\" given for bounds option. Should be formatted as min:max", value));
                                }
                            });

                            boolean invoke = false;
                            for(AbstractBlockEntry entry : process) {
                                entry.getColor().set(r, g, b, a);
                                AbstractBlockEntry existing = MARKER_OPTIONS.get(entry.getBlock(), entry.getMetadata());
                                if (existing != null) {
                                    // add new bounds
                                    entry.getBounds().getAll().forEach(bound -> existing.getBounds().add(bound.getMin(), bound.getMax()));
                                    if(wasGivenRGBA) existing.getColor().set(entry.getColor().getAsBuffer());
                                    invoke = true;
                                } else if(MARKER_OPTIONS.add(entry)) {
                                    Wrapper.printMessage(String.format("Added block \"%s\"", entry.getPrettyName()));
                                    // execute the callbacks
                                    invoke = true;
                                } else {
                                    Wrapper.printMessage(String.format("Could not add block \"%s\"", entry.getPrettyName()));
                                }
                            }
                            return invoke;
                        } catch (Exception e) {
                            Wrapper.printMessage(e.getMessage());
                        }
                    } else Wrapper.printMessage("Missing block name/id argument");
                    return false;
                })
                .addCallback(cmd -> {
                    MARKER_OPTIONS.serialize();
                    reloadRenderers();
                })
                .build()
        );
        addCommand(new CommandBuilder()
                .setName("remove")
                .setDescription("Removes block to block esp")
                .setOptionBuilder(parser -> {
                    parser.acceptsAll(Arrays.asList("meta", "m"), "blocks metadata id")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("id", "i"), "searches for block by id instead of name");
                    parser.acceptsAll(Arrays.asList("regex", "e"), "searches for blocks by using the argument as a regex expression");
                    parser.accepts("bounds", "Will only draw blocks from within the min-max bounds given")
                            .withRequiredArg();
                })
                .setProcessor(opts -> {
                    List<?> args = opts.nonOptionArguments();
                    if(args.size() > 0) {
                        boolean byId = opts.has("i");
                        String name = String.valueOf(args.get(0));
                        OptionHelper helper = new OptionHelper(opts);
                        int meta = helper.getIntOrDefault("m", 0);
                        boolean removingOptions = opts.has("bounds");
                        try {
                            Collection<AbstractBlockEntry> process = Sets.newHashSet();
                            if(opts.has("regex")) process.addAll(BlockOptionHelper.getAllBlocksMatchingByUnlocalized(name));
                            else process.add(byId ? BlockEntry.createById(SafeConverter.toInteger(name, 0), meta) :
                                        BlockEntry.createByResource(name, meta));

                            // not the proper use of atomics but it will get the job done
                            AtomicBoolean shouldCall = new AtomicBoolean(false);
                            for(AbstractBlockEntry e : process) {
                                final AbstractBlockEntry realEntry = MARKER_OPTIONS.get(e.getBlock(), e.getMetadata());
                                if(realEntry != null) {
                                    if(removingOptions) {
                                        // remove just the options listed
                                        if(opts.has("bounds")) opts.valuesOf("bounds").forEach(v -> {
                                            String value = String.valueOf(v);
                                            String[] mm = value.split("-");
                                            if(mm.length > 1) {
                                                int min = SafeConverter.toInteger(mm[0]);
                                                int max = SafeConverter.toInteger(mm[1]);
                                                realEntry.getBounds().remove(min, max);
                                            } else {
                                                throw new IllegalArgumentException(String.format("Invalid argument \"%s\" given for bounds option. Should be formatted as min:max", value));
                                            }
                                        });
                                    } else if (MARKER_OPTIONS.remove(realEntry)) {
                                        Wrapper.printMessage(String.format("Removed block \"%s\" from the block list", e.getPrettyName()));
                                        shouldCall.set(true);
                                    } else if (process.size() <= 1) { // don't print this message for all matching blocks
                                        Wrapper.printMessage(String.format("Could not find block \"%s\"", e.getPrettyName()));
                                    }
                                }
                            }
                            return shouldCall.get();
                        } catch (Exception e) {
                            Wrapper.printMessage(e.getMessage());
                        }
                    }
                    return false;
                })
                .addCallback(cmd -> {
                    MARKER_OPTIONS.serialize();
                    reloadRenderers();
                })
                .build()
        );
        addCommand(new CommandBuilder()
                .setName("info")
                .setDescription("Will show all the render info for the block (if it exists)")
                .setOptionBuilder(parser -> {
                    parser.acceptsAll(Arrays.asList("meta", "m"), "blocks metadata id")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("id", "i"), "searches for block by id instead of name");
                })
                .setProcessor(opts -> {
                    List<?> args = opts.nonOptionArguments();
                    if(args.size() > 0) {
                        boolean byId = opts.has("i");
                        String name = String.valueOf(args.get(0));
                        OptionHelper helper = new OptionHelper(opts);
                        int meta = helper.getIntOrDefault("m", 0);
                        try {
                            AbstractBlockEntry match = byId ? BlockEntry.createById(SafeConverter.toInteger(name, 0), meta) :
                                    BlockEntry.createByResource(name, meta);

                            AbstractBlockEntry find = MARKER_OPTIONS.get(match.getBlock(), match.getMetadata());
                            if(find != null) {
                                Wrapper.printMessage(find.toString());
                            } else {
                                Wrapper.printMessage(String.format("Could not find block \"%s\"", match.getPrettyName()));
                            }
                        } catch (Exception e) {
                            Wrapper.printMessage(e.getMessage());
                        }
                    } else Wrapper.printMessage("Missing block name/id argument");
                    return false;
                })
                .build()
        );
        addCommand(new CommandBuilder()
                .setName("list")
                .setDescription("Lists all the blocks in the block list")
                .setProcessor(opts -> {
                    final StringBuilder builder = new StringBuilder("Found: ");
                    MARKER_OPTIONS.forEach(entry -> {
                        builder.append(entry.getPrettyName());
                        builder.append(", ");
                    });
                    String finished = builder.toString();
                    if(finished.endsWith(", ")) finished = finished.substring(0, finished.length() - 2);
                    Wrapper.printMessageNaked(finished);
                    return true;
                })
                .build()
        );
    }

    @Override
    public void onUnload() {
        MARKER_OPTIONS.serialize();
    }

    @Override
    public void onEnabled() {
        MARKER_OPTIONS.deserialize();
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
                if (tess != null && FastReflection.Fields.VertexBuffer_isDrawing.get(tess.getBuffer(), false)) {
                    AbstractBlockEntry blockEntry = MARKER_OPTIONS.get(block, block.getMetaFromState(state));
                    if(blockEntry != null && blockEntry.getBounds().isWithinBoundaries(pos.getY())) {
                        AxisAlignedBB bb = state.getSelectedBoundingBox(Wrapper.getWorld(), pos);
                        GeometryTessellator.drawLines(
                                tess.getBuffer(),
                                bb.minX, bb.minY, bb.minZ,
                                bb.maxX, bb.maxY, bb.maxZ,
                                GeometryMasks.Line.ALL,
                                blockEntry.getColor().getAsBuffer()
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
            if(!clearBuffer.getBoolean())
                GlStateManager.disableDepth();
            else {
                GlStateManager.clearDepth(1.f);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            }

            final boolean aa_enabled = antialiasing.getBoolean();
            final int aa_max = antialiasing_max.getInt();

            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

            renderers.forEach((chk, info) -> {
                if (info.isVboPresent() && info.isRendering()) {
                    if(aa_enabled && (aa_max == 0 || info.getRenderCount() <= aa_max))
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);

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
            return tessellator != null && FastReflection.Fields.VertexBuffer_isDrawing.get(tessellator.getBuffer());
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
